package com.joengelke.shoppinglistapp.frontend.utils

import kotlinx.serialization.json.Json

object JsonHelper {
    val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
}