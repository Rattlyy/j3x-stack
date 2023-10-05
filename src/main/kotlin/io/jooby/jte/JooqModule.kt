package io.jooby.jte

import com.codahale.metrics.MetricRegistry
import com.codahale.metrics.health.HealthCheckRegistry
import com.zaxxer.hikari.HikariConfig
import io.jooby.Extension
import io.jooby.Jooby
import io.jooby.hikari.HikariModule
import it.rattly.j3x.dotenv
import jakarta.inject.Provider
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import org.jooq.impl.DefaultConfiguration
import javax.sql.DataSource
import kotlin.collections.set

class JooqModule : Extension {
    override fun install(jooby: Jooby) {
        jooby.install(HikariModule(
            HikariConfig().apply {
                username = dotenv["DB_USER"]
                password = dotenv["DB_PASSWORD"]
                jdbcUrl = dotenv["DB_URL"]
                driverClassName = dotenv["DB_DRIVER"]

                dataSourceProperties["cachePrepStmts"] = "true"
                dataSourceProperties["prepStmtCacheSize"] = "250"
                dataSourceProperties["prepStmtCacheSqlLimit"] = "2048"

                poolName = "HikariPool"
                maximumPoolSize = 20
            }
        )
            .metricRegistry(jooby.services.getOrNull(MetricRegistry::class.java))
            .healthCheckRegistry(jooby.services.getOrNull(HealthCheckRegistry::class.java))
        )

        jooby.services.put(DSLContext::class.java, Provider {
            DSL.using(DefaultConfiguration().apply {
                set(SQLDialect.POSTGRES)
                set(jooby.services[DataSource::class.java])
            })
        })
    }
}
