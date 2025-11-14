package com.shadowforgedmmo.engine.util

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import java.io.File
import java.net.URL

private val objectMapper = ObjectMapper(YAMLFactory())

fun readYaml(file: File) = objectMapper.readTree(file)

fun readYaml(url: URL) = objectMapper.readTree(url)
