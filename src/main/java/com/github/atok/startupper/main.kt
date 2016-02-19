package com.github.atok.startupper

import spark.Spark
import java.io.File
import java.math.BigInteger
import java.nio.file.Files
import java.security.SecureRandom

fun getSecret(): String {
    val secretFile = File("auth.secret")
    if(secretFile.exists()) {
        return String(Files.readAllBytes(secretFile.toPath()), Charsets.UTF_8)
    }

    val random = SecureRandom()
    val secretValue = BigInteger(130, random).toString(32)
    Files.write(secretFile.toPath(), secretValue.toByteArray(Charsets.UTF_8))

    return secretValue
}

fun main(args: Array<String>) {
    val secret = getSecret()
    val app = App(secret)
    app.start()

    Spark.awaitInitialization()
    println("Secret: $secret")
}