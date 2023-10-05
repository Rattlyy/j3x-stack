package io.jooby.jte

import io.jooby.*
import it.rattly.j3x.ProvideSerialize
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.serializer
import java.lang.reflect.Type
import java.nio.charset.StandardCharsets

class KotlinSerializationModule : Extension, MessageDecoder, MessageEncoder {
    private val json: Json

    constructor(json: Json) {
        this.json = json
    }

    constructor() {
        json = Json {
            prettyPrint = true
            isLenient = true
            allowStructuredMapKeys = true
            allowSpecialFloatingPointValues = true
            ignoreUnknownKeys = true
        }
    }

    override fun install(application: Jooby) {
        application.encoder(this)
    }

    @OptIn(ExperimentalSerializationApi::class)
    override fun decode(ctx: Context, type: Type) =
        json.decodeFromStream(serializer(type), ctx.body().stream())

    override fun encode(ctx: Context, value: Any): ByteArray {
        ctx.setDefaultResponseType(MediaType.json)
        val serializer = when (value) {
            is ProvideSerialize -> value.serializer()
            value::class.typeParameters.isNotEmpty() ->
                throw IllegalArgumentException("Cannot serialize types that have type parameters")

            else -> serializer(value::class.java)
        } as KSerializer<Any>

        return json.encodeToString(serializer, value).toByteArray(StandardCharsets.UTF_8)
    }
}