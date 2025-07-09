plugins {
    kotlin("jvm") version "2.2.0-RC3"
    id("com.gradleup.shadow") version "8.3.0"
    id("xyz.jpenilla.run-paper") version "2.3.1"
}

group = "com.skyxserver.my.id"
version = "1.0.1"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/") {
        name = "papermc-repo"
    }
    maven("https://oss.sonatype.org/content/groups/public/") {
        name = "sonatype"
    }
    maven("https://repo.bstats.org/content/repositories/releases") {
        name = "bstats"
    }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21-R0.1-SNAPSHOT")
    compileOnly("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.bstats:bstats-bukkit:3.1.0")
    implementation("com.googlecode.json-simple:json-simple:1.1.1")
    implementation("io.github.oshai:kotlin-logging:7.0.7")
    compileOnly("ch.qos.logback:logback-classic:1.5.18")
}

tasks {
    runServer {
        // Configure the Minecraft version for our task.
        // This is the only required configuration besides applying the plugin.
        // Your plugin's jar (or shadowJar if present) will be used automatically.
        minecraftVersion("1.21")
    }
}

val targetJavaVersion = 21
kotlin {
    jvmToolchain(targetJavaVersion)
}

tasks.build {
    dependsOn("shadowJar")
}

tasks.jar {
    archiveFileName.set("${project.name}.jar")
}

tasks.shadowJar {
    archiveFileName.set("${project.name}-all.jar")

    relocate("org.bstats", "com.skyxserver.my.id.novoidx.lib.bstats")
    relocate("io.github.oshai", "com.skyxserver.my.id.novoidx.lib.kotlinlogging")

    exclude("kotlin/**")
    exclude("META-INF/kotlin*")
    exclude("META-INF/versions/9/module-info.class")

}

tasks.processResources {
    val props = mapOf("version" to version)
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") {
        expand(props)
    }
}
