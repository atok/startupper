package com.github.atok.startupper

import spark.Spark
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import java.util.*

class ProcessRunner(
        val configRepository: ConfigRepository,
        val logRepository: LogRepository,
        val jarRepository: JarRepository,
        val workspaceLocation: String) {

    val startedJars = HashMap<String, Process>()

    private fun workspaceLocation(id: String): Path {
        return Paths.get(workspaceLocation, id)
    }

    fun run(jar: Path, id: String) {
        kill(id)

        val workspace = Paths.get(workspaceLocation, id)
        workspace.toFile().mkdirs()

        val configFileLocation = configRepository.locationForId(id)
        val logFileLocation = logRepository.locationForId(id)

        val process = start(jar, configFileLocation, workspaceLocation(id), logFileLocation)
        startedJars[id] = process
    }

    fun runNewest(id: String) {
        val newest = jarRepository.newest(id)

        if(newest == null) {
            Spark.halt(400, "Jar not found")
            return
        }

        run(newest, id)
    }

    fun kill(id: String): Boolean {
        val old = startedJars[id]
        if(old != null) {
            if(old.isAlive) {
                println("Killing ID:$id")
                old.destroy()
                startedJars.remove(id)
                return true
            }
        }
        return false
    }

    fun start(jar: Path, config: Path, workDir: Path, logPath: Path): Process {
        val params = listOf("java", "-jar", "${jar.toAbsolutePath()}", "${config.toAbsolutePath()}")
        val processBuilder = ProcessBuilder(params)
        val env = processBuilder.environment()
        env.put("PATH", System.getenv("PATH"))

        processBuilder.directory(workDir.toFile())

        val log = logPath.toFile()
        if(!logPath.parent.toFile().exists()) {
            logPath.parent.toFile().mkdirs()
        }

        Files.write(log.toPath(), "!!START: ${params.joinToString(" ")}\n".toByteArray(Charsets.UTF_8), StandardOpenOption.APPEND, StandardOpenOption.CREATE)

        processBuilder.redirectErrorStream(true)
        processBuilder.redirectOutput(ProcessBuilder.Redirect.appendTo(log))

        val process = processBuilder.start()

        return process
    }

    fun init() {
        File(workspaceLocation).mkdirs()
    }

    data class ProcessStatus(val running: Boolean)

    fun status(id: String): ProcessStatus {
        val started = startedJars[id]
        return ProcessStatus(started != null && started.isAlive)
    }

}
