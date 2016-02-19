package com.github.atok.startupper

import com.mashape.unirest.http.Unirest
import java.io.File

class StartupperClient(val secret: String, val host: String) {
    fun deploy(id: String, file: File, configId: String) {
        uploadJar(file, id)
        start(id, configId)
    }

    fun start(id: String, configId: String) {
        val response = Unirest.post("$host/jar/$id/run?configId=$configId")
                .header("authorization", secret)
                .asString();

        println("Start: ${response.body}")
    }

    fun uploadJar(file: File, id: String) {
        if(!file.exists()) throw IllegalArgumentException("File not found: $file")

        val response = Unirest.post("$host/jar/$id")
                .header("authorization", secret)
                .field("file", file)
                .asString()

        println("Upload: ${response.body}")
    }
}

