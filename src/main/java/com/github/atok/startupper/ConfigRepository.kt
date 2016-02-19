package com.github.atok.startupper

import spark.Spark
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardOpenOption

class ConfigRepository(val configLocation: String) {

    fun list(): List<String> {
        return File(configLocation).listFiles().map { it.name }
    }

    fun get(id: String): String {
        val path = Paths.get(configLocation, id)
        if(!path.toFile().exists()) {
            Spark.halt(404)
        }

        return String(Files.readAllBytes(path), Charsets.UTF_8)
    }

    fun save(id: String, body: String) {
        val path = Paths.get(configLocation, id)
        val parentFile = path.parent.toFile()

        if(!parentFile.exists()){
            parentFile.mkdirs()
        }

        Files.write(
                Paths.get(configLocation, id),
                body.toByteArray(Charsets.UTF_8),
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING
        )
    }

    fun locationForId(id: String): Path {
        return Paths.get(configLocation, id)
    }
}
