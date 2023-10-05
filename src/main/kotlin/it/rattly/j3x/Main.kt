package it.rattly.j3x

import com.codahale.metrics.MetricFilter
import com.codahale.metrics.graphite.GraphiteReporter
import com.codahale.metrics.health.jvm.ThreadDeadlockHealthCheck
import com.codahale.metrics.jvm.FileDescriptorRatioGauge
import com.codahale.metrics.jvm.GarbageCollectorMetricSet
import com.codahale.metrics.jvm.MemoryUsageGaugeSet
import com.codahale.metrics.jvm.ThreadStatesGaugeSet
import io.github.cdimascio.dotenv.Dotenv
import io.jooby.ExecutionMode
import io.jooby.jte.*
import io.jooby.kt.runApp
import io.jooby.metrics.MetricsModule
import io.jooby.whoops.WhoopsModule
import kotlinx.serialization.KSerializer
import org.reflections.Reflections
import java.nio.file.Path
import java.util.concurrent.TimeUnit
import kotlin.reflect.full.functions


val dotenv: Dotenv = Dotenv.load()
private val reflections = Reflections(Register::class.java.packageName)

fun main(args: Array<String>) = runApp(args, ExecutionMode.DEFAULT) {
    install(JteModule(Path.of("src", "main", "jte")))
    install(KotlinSerializationModule())
    install(WhoopsModule())
    install(JooqModule())
    install(
        MetricsModule()
            .threadDump()
            .ping()
            .healthCheck("deadlock", ThreadDeadlockHealthCheck())
            .metric("memory", MemoryUsageGaugeSet())
            .metric("threads", ThreadStatesGaugeSet())
            .metric("gc", GarbageCollectorMetricSet())
            .metric("fs", FileDescriptorRatioGauge())
            .reporter { registry ->
                GraphiteReporter.forRegistry(registry)
                    .prefixedWith("jooby")
                    .convertRatesTo(TimeUnit.SECONDS)
                    .convertDurationsTo(TimeUnit.MILLISECONDS)
                    .filter(MetricFilter.ALL)
                    .build(GraphiteHttpSender()).also { it.start(10, TimeUnit.SECONDS) }
            }
    )

    reflections.getTypesAnnotatedWith(Register::class.java).forEach {
        if (it.kotlin.functions.any { it.isSuspend }) {
            coroutine { mvc(it.getDeclaredConstructor().newInstance()) }
        } else mvc(it.getDeclaredConstructor().newInstance())
    }

    initKte(this) // fixme https://github.com/jooby-project/jooby/issues/3125
}

annotation class Register
interface ProvideSerialize {
    fun serializer(): KSerializer<*>
}