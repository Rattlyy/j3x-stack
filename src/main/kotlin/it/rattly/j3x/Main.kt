package it.rattly.j3x

import io.jooby.ExecutionMode
import io.jooby.jte.JteModule
import io.jooby.jte.KotlinSerializationModule
import io.jooby.jte.initKte
import io.jooby.kt.runApp
import io.jooby.whoops.WhoopsModule
import org.reflections.Reflections
import java.nio.file.Path


private val reflections = Reflections(Register::class.java.packageName)

fun main(args: Array<String>) = runApp(args, ExecutionMode.EVENT_LOOP) {
    install(JteModule(Path.of("src", "main", "jte")))
    install(KotlinSerializationModule())
    install(WhoopsModule())

    // OPTIONAL: Logto/Any OIDC compatible auth stuff
    // install(Pac4jModule().client("/app/*") {
    //     OidcClient(OidcConfiguration().apply {
    //         clientId = dotenv["LOGTO_CLIENT_ID"]
    //         secret = dotenv["LOGTO_SECRET"]
    //         discoveryURI = dotenv["LOGTO_OIDC_CONFIG"]
    //     })
    // })

    reflections.getTypesAnnotatedWith(Register::class.java).forEach {
        mvc(it.getDeclaredConstructor().newInstance())
    }

    initKte(this) // fixme https://github.com/jooby-project/jooby/issues/3125
}

annotation class Register