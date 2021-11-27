plugins {
    kotlin("jvm") version Kotlin.version
    id("fabric-loom") version Fabric.Loom.version
    `maven-publish`
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
    withSourcesJar()
}

base {
    archivesBaseName = Constants.modid
}

group = Constants.group
version = Constants.version

repositories {
    maven(url = "https://maven.fabricmc.net/") {
        name = "Fabric"
    }
    maven(url = "https://kotlin.bintray.com/kotlinx") {
        name = "Kotlinx"
    }
    mavenCentral()
}

dependencies {
    minecraft(group = "com.mojang", name = "minecraft", version = Minecraft.version)
    mappings(group = "net.fabricmc", name = "yarn", version = Fabric.Yarn.version)

    modImplementation(group = "net.fabricmc.fabric-api", name = "fabric-api", version = Fabric.API.version)
    modImplementation(group = "net.fabricmc", name = "fabric-loader", version = Fabric.Loader.version)
    modImplementation(group = "net.fabricmc", name = "fabric-language-kotlin", version = Fabric.LanguageKotlin.version)
}

tasks.getByName<ProcessResources>("processResources") {
    filesMatching("fabric.mod.json") {
        expand(
            mutableMapOf(
                "modid" to Constants.modid,
                "version" to Constants.version,
                "kotlinVersion" to Kotlin.version,
                "fabricApiVersion" to Fabric.API.version
            )
        )
    }
}
