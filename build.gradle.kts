import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import groovy.json.JsonSlurper

plugins {
    id("java-library")
    alias(libs.plugins.shadow)
    alias(libs.plugins.paperweight.userdev)
    alias(libs.plugins.run.paper)
}

repositories {
    mavenCentral()
}

dependencies {
    paperweight.paperDevBundle(libs.versions.paper.get())

    implementation(libs.bstats)
    implementation(libs.hikaricp)
}

configurations.runtimeClasspath {
    exclude(group = "org.slf4j", module = "slf4j-api")
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(libs.versions.java.compile.get().toInt())
}

val toolchainService = project.extensions.getByType(JavaToolchainService::class.java)

data class ServerTarget(
    val platform: String,
    val version: String,
    val javaVersion: Int = libs.versions.java.runtime.get().toInt()
)

val testMatrixFile = file("server-matrix.json")

val testMatrix: List<ServerTarget> = run {
    if (!testMatrixFile.exists()) {
        logger.warn("server-matrix.json not found, runs matrix is empty")
        emptyList()
    } else {
        @Suppress("UNCHECKED_CAST")
        val parsed = JsonSlurper().parse(testMatrixFile) as List<Map<String, Any?>>
        parsed.flatMap { platformEntry ->
            val platform = platformEntry["platform"] as String

            @Suppress("UNCHECKED_CAST")
            val versions = platformEntry["versions"] as List<Map<String, Any?>>

            versions.map { versionEntry ->
                ServerTarget(
                    platform = platform,
                    version = versionEntry["version"] as String,
                    javaVersion = (versionEntry["javaVersion"] as? Number)?.toInt()
                        ?: libs.versions.java.runtime.get().toInt()
                )
            }
        }
    }
}

fun httpGetJson(url: String): Any? {
    val client = HttpClient.newHttpClient()
    val request = HttpRequest.newBuilder(URI(url))
        .header("User-Agent", "plugin-ci")
        .build()

    val response = client.send(request, HttpResponse.BodyHandlers.ofString())

    if (response.statusCode() !in 200..299) {
        error("GET $url -> ${response.statusCode()}: ${response.body()}")
    }

    return JsonSlurper().parseText(response.body())
}

fun asBuildsList(parsed: Any?): List<Map<String, Any?>> {
    @Suppress("UNCHECKED_CAST")
    return when (parsed) {
        is List<*> -> parsed as List<Map<String, Any?>>
        is Map<*, *> -> {
            val key = listOf("builds", "data", "items", "results")
                .firstOrNull { parsed.containsKey(it) }
                ?: error("Didn't find a list of builds in the answer, keys: ${parsed.keys}")
            (parsed[key] as List<*>) as List<Map<String, Any?>>
        }
        else -> error("Unexpected JSON format: $parsed")
    }
}

fun resolveDownloadUrl(platform: String, version: String): String = when (platform) {
    "paper", "folia" -> {
        @Suppress("UNCHECKED_CAST")
        val builds = httpGetJson("https://fill.papermc.io/v3/projects/$platform/versions/$version/builds") as List<Map<String, Any?>>

        val build = builds.firstOrNull { it["channel"] == "STABLE" }
            ?: error("There are no builds for $platform $version")

        @Suppress("UNCHECKED_CAST")
        val downloads = build["downloads"] as Map<String, Any?>

        @Suppress("UNCHECKED_CAST")
        val server = downloads["server:default"] as Map<String, Any?>

        server["url"] as String
    }
    "purpur" -> "https://api.purpurmc.org/v2/purpur/$version/latest/download"
    "leaf" -> {
        val builds = asBuildsList(httpGetJson("https://api.leafmc.one/v2/projects/leaf/versions/$version/builds"))

        val latest = builds.maxByOrNull { (it["build"] as Number).toInt() }
            ?: error("There are no Leaf builds $version")

        val buildNum = (latest["build"] as Number).toInt()
        "https://api.leafmc.one/v2/projects/leaf/versions/$version/builds/$buildNum/downloads/leaf-$version-$buildNum.jar"
    }
    else -> error("Unknown platform: $platform")
}

fun commonServerJvmArgs(javaVersion: Int): List<String> {
    val args = mutableListOf(
        "-Xms2G",
        "-Xmx4G",
        "-Dcom.mojang.eula.agree=true",
        "-Dfile.encoding=UTF-8",
        "-Dstdout.encoding=UTF-8",
        "-Dstderr.encoding=UTF-8",
        "-Dsun.stdout.encoding=UTF-8",
        "-Dsun.stderr.encoding=UTF-8"
    )
    if (javaVersion >= 24) {
        args.add("--sun-misc-unsafe-memory-access=allow")
    }
    return args
}

val runServersDir = providers.provider {
    layout.projectDirectory.dir("run")
}

val shadowJar = tasks.named<ShadowJar>("shadowJar")

testMatrix.forEach { target ->
    val safeVersion = target.version.replace(Regex("[.\\-]"), "_")
    val id = "${target.platform}_$safeVersion"
    val serverDirProvider = runServersDir.map { it.dir(id) }

    val downloadServerJar = tasks.register("downloadServerJar_$id") {
        val outFile = serverDirProvider.map { it.file("server.jar") }
        outputs.file(outFile)
        doLast {
            val dest = outFile.get().asFile
            dest.parentFile.mkdirs()
            if (!dest.exists()) {
                val url = resolveDownloadUrl(target.platform, target.version)
                logger.lifecycle("Downloading ${target.platform} ${target.version}: $url")
                URI(url).toURL().openStream().use { input ->
                    dest.outputStream().use { output -> input.copyTo(output) }
                }
            }
        }
    }

    tasks.register<JavaExec>("runServer_$id") {
        group = "run server matrix"
        description = "Launches ${target.platform} ${target.version} with plugin"
        dependsOn(downloadServerJar, shadowJar)
        notCompatibleWithConfigurationCache("Interactive server process, nothing to cache")

        doFirst {
            val serverDir = serverDirProvider.get().asFile
            val pluginsDir = serverDir.resolve("plugins")
            pluginsDir.mkdirs()

            val builtJar = shadowJar.get().archiveFile.get().asFile
            builtJar.copyTo(pluginsDir.resolve(builtJar.name), overwrite = true)

            serverDir.resolve("eula.txt").writeText("eula=true\n")
        }

        workingDir = serverDirProvider.get().asFile
        standardInput = System.`in`

        javaLauncher.set(
            toolchainService.launcherFor {
                languageVersion.set(JavaLanguageVersion.of(target.javaVersion))
            }
        )

        classpath = files(serverDirProvider.map { it.file("server.jar") })

        jvmArgs(commonServerJvmArgs(target.javaVersion))
        args("--nogui")
    }
}

tasks.jar {
    archiveBaseName.set(project.name)
    archiveVersion.set(project.version.toString())

    from(rootProject.file("LICENSE"))
}

tasks.shadowJar {
    archiveBaseName.set(project.name)
    archiveVersion.set(project.version.toString())
    archiveClassifier.set("")

    from(rootProject.file("LICENSE"))

    enableAutoRelocation = true
    relocationPrefix = "${project.group}.libs"
}

tasks.register("runServerMatrix") {
    group = "run server matrix"
    dependsOn(testMatrix.map { "runServer_${it.platform}_${it.version.replace(Regex("[.\\-]"), "_")}" })
}

tasks.register<Delete>("cleanTestServers") {
    group = "run server matrix"
    description = "Removes all matrix test servers from run/, preserving run/default"
    delete(
        runServersDir.map { runDir ->
            fileTree(runDir) {
                exclude("default/**")
            }
        }
    )
}

tasks {
    runServer {
        minecraftVersion(libs.versions.minecraft.get())
        runDirectory.set(layout.projectDirectory.dir("run/default"))

        val javaVersion = libs.versions.java.runtime.get().toInt()

        javaLauncher.set(
            toolchainService.launcherFor {
                languageVersion.set(JavaLanguageVersion.of(javaVersion))
            }
        )

        jvmArgs(commonServerJvmArgs(javaVersion))
        args("--nogui")
    }

    processResources {
        val props = mapOf(
            "name" to project.name,
            "prefix" to project.property("prefix"),
            "description" to project.description,
            "version" to version,
            "apiVersion" to project.property("api.version"),
            "authors" to project.property("authors")
        )

        filesMatching("plugin.yml") {
            expand(props)
        }
    }
}
