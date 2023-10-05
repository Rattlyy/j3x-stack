package io.jooby.jte

import com.codahale.metrics.graphite.GraphiteSender
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.extensions.authentication
import it.rattly.j3x.dotenv
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import org.jetbrains.kotlin.com.google.common.collect.Lists
import java.io.IOException


internal class GraphiteHttpSender() : GraphiteSender {
    private val metrics: MutableList<GraphiteMetric> = Lists.newArrayList()

    @Throws(IllegalStateException::class, IOException::class)
    override fun connect() {
        // Just no op here
    }

    @Throws(IOException::class)
    override fun close() {
        // no op
    }

    @Throws(IOException::class)
    override fun send(name: String, value: String, timestamp: Long) {
        metrics.add(GraphiteMetric(name, 10, value.toDouble(), timestamp))
    }

    @Throws(IOException::class)
    override fun flush() = runBlocking {
        println(
            Fuel.post("${dotenv["GRAPHITE_URL"]}/metrics")
                .authentication().basic(dotenv["GRAPHITE_USER"], dotenv["GRAPHITE_PASS"])
                .header("Content-Type", "application/json")
                .body(Json.encodeToString(ListSerializer(GraphiteMetric.serializer()), metrics))
                .responseString()
        )

        metrics.clear()
    }

    override fun isConnected(): Boolean {
        // TODO Auto-generated method stub
        return false
    }

    override fun getFailures(): Int {
        // TODO Auto-generated method stub
        return 0
    }

    @Serializable
    private class GraphiteMetric(
        val name: String,
        val interval: Int,
        val value: Double,
        val time: Long
    )
}