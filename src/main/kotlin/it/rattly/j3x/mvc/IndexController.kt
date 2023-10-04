package it.rattly.j3x.mvc

import io.jooby.annotation.GET
import io.jooby.annotation.Path
import it.rattly.j3x.Register
import kotlinx.serialization.Serializable

@Register
@Path("/")
class IndexController {
    @GET
    fun index(): Response = Response(true)

    @Serializable
    data class Response(val ok: Boolean)
}