package com.shadowforgedmmo.engine.util

import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import java.io.File
import java.net.URL
import kotlin.reflect.KClass

private val objectMapper = ObjectMapper(YAMLFactory()).enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)

fun <T : Any> readYaml(file: File, classOfT: KClass<T>) =
    objectMapper.readValue<T>(file, classOfT.java)

fun <T : Any> readYaml(url: URL, classOfT: KClass<T>) =
    objectMapper.readValue<T>(url, classOfT.java)
