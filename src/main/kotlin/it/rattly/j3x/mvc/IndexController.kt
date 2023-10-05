package it.rattly.j3x.mvc

import io.jooby.Context
import io.jooby.annotation.GET
import io.jooby.annotation.POST
import io.jooby.annotation.Path
import io.jooby.annotation.QueryParam
import io.jooby.kt.require
import it.rattly.j3x.Register
import it.rattly.j3x.jooq.tables.references.OKS
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import org.jooq.DSLContext
import org.jooq.impl.DSL

@Register
@Path("/")
class IndexController {
    @GET
    suspend fun index(ctx: Context) = buildJsonArray {
        withContext(Dispatchers.IO) {
            async { ctx.require(DSLContext::class).selectFrom(OKS).fetch() }.await()
                .map { Json.encodeToJsonElement(Oks(it.id!!, it.isOk!!)) }.forEach(::add)
        }
    }

    @POST
    suspend fun create(@QueryParam ok: Boolean, ctx: Context) = buildJsonObject {
        withContext(Dispatchers.IO) {
            put(
                "ok",
                async {
                    ctx.require(DSLContext::class).insertInto(OKS).values(DSL.defaultValue(), ok)
                        .execute()
                }.await() == 1
            )
        }
    }

}

@Serializable
data class Oks(val id: Int, val ok: Boolean)