package com.github.atok.startupper

import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import javax.servlet.http.Part

class JarRepository(val jarLocation: String) {
    fun list(): List<String> {
        return File(jarLocation).listFiles().filter { it.isDirectory }.map { it.name }
    }

    fun save(uploadFilePart: Part, id: String) {
        val saveAs = Paths.get(jarLocation, id, timestamp() + ".jar")
        saveAs.parent.toFile().mkdirs()

        uploadFilePart.inputStream.use {
            Files.copy(it, saveAs)
            uploadFilePart.delete()
        }
    }

    private fun timestamp(): String {
        return (System.currentTimeMillis() / 1000).toString()
    }

    fun newest(id: String): Path? {
        val candidates = Paths.get(jarLocation, id).toFile().listFiles()
        return  candidates?.sortedBy { it.name }?.lastOrNull()?.toPath()
    }
}
