package com.shadowforgedmmo.engine.runtime

class RuntimeEnvironment(
    val apiUrl: String,
    val resourcePackUrl: String,
    val resourcePackHash: String
)

fun createRuntimeEnvironment() = RuntimeEnvironment(
    System.getenv("API_URL"),
    System.getenv("RESOURCE_PACK_URL"),
    System.getenv("RESOURCE_PACK_HASH")
)
