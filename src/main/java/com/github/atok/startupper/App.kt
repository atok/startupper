package com.github.atok.startupper

import com.google.gson.Gson
import spark.Spark.*
import javax.servlet.MultipartConfigElement

class App(val authSecret: String) {

    val tempLocation = "data/temp-uploads"

    val configRepository = ConfigRepository("data/config")
    val logRepository = LogRepository("data/logs")
    val jarRepository = JarRepository("data/jars")
    val processRunner = ProcessRunner(configRepository, logRepository, jarRepository, "data/workspaces")

    val gson = Gson()

    fun start() {
        val maxFileSize = 100000000L;       // the maximum size allowed for uploaded files
        val maxRequestSize = 100000000L;    // the maximum size allowed for multipart/form-data requests
        val fileSizeThreshold = 1024;       // the size threshold after which files will be written to disk

        processRunner.init()

        port(3366)
        ipAddress("0.0.0.0")

        before({req, resp ->
            val authHeader = req.headers("Authorization")
            if(authHeader == null || authHeader != authSecret) {
                halt(404)
            }
        })

        before { request, response ->
            println("${request.requestMethod()} ${request.uri()}")
        }

        get("/", {req, resp -> "Hello"})

        post("/jar/:id", "multipart/form-data", { request, response ->
            val id = request.params("id")

            val multipartConfigElement = MultipartConfigElement(tempLocation, maxFileSize, maxRequestSize, fileSizeThreshold)
            request.raw().setAttribute("org.eclipse.jetty.multipartConfig", multipartConfigElement);

            val uploadFilePart = request.raw().getPart("file")
            jarRepository.save(uploadFilePart, id)

            println("Got new file for ID $id")
            halt(200, "ok")
        })

        post("/jar/:id/run", { req, resp ->
            val id = req.params("id")

            processRunner.runNewest(id)
            halt(200, "ok")
        })

        post("/jar/:id/stop", { req, resp ->
            val id = req.params("id")
            processRunner.kill(id)
        })

        get("/jar/:id", { req, resp ->
            val id = req.params("id")
            gson.toJson(processRunner.status(id))
        })

        get("/jar/:id/log", { req, resp ->
            val id = req.params("id")
            logRepository.get(id)
        })

        get("/jar", { req, resp ->
            val list = jarRepository.list()
            gson.toJson(list)
        })

        post("/config/:id", { req, resp ->
            val id = req.params("id")
            configRepository.save(id, req.body())
            halt(200, "ok")
        })

        get("/config/:id", { req, resp ->
            val id = req.params("id")
            configRepository.get(id)
        })

        get("/config", { req, resp ->
            val list = configRepository.list()
            gson.toJson(list)
        })
    }
}
