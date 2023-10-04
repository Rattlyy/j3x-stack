package io.jooby.jte

import gg.jte.TemplateEngine
import io.jooby.MediaType
import io.jooby.kt.Kooby

fun initKte(jooby: Kooby) = jooby.apply {
    encoder(MediaType.html, object : JteTemplateEngine(require(TemplateEngine::class.java)) {
        override fun extensions() = listOf(".kte")
    })
}