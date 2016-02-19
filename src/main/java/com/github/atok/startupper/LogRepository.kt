package com.github.atok.startupper

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class LogRepository(val logsLocation: String) {

    fun get(id: String): String {
        return String(Files.readAllBytes(Paths.get(logsLocation, id)), Charsets.UTF_8)
    }

    fun locationForId(id: String): Path {
        return Paths.get(logsLocation, id)
    }

}
