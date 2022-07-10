val javaVersion = JavaVersion.VERSION_17
val minecraftVersion: String by project
val yarnMappings: String by project
val loaderVersion: String by project
val fabricVersion: String by project
val fabricKotlinVersion: String by project
val modVersion: String by project
val mavenGroup: String by project
val archivesBaseName: String by project
val modReleaseType: String by project

plugins {
    id("fabric-loom")
    val kotlinVersion: String by System.getProperties()
    kotlin("jvm").version(kotlinVersion)
}

base {
    archivesName.set(archivesBaseName)
}

version = modVersion
group = mavenGroup

repositories {}

dependencies {
    minecraft("com.mojang", "minecraft", minecraftVersion)
    mappings("net.fabricmc", "yarn", yarnMappings, null, "v2")
    modImplementation("net.fabricmc", "fabric-loader", loaderVersion)
    modImplementation("net.fabricmc.fabric-api", "fabric-api", fabricVersion)
    modImplementation("net.fabricmc", "fabric-language-kotlin", fabricKotlinVersion)
}

tasks {
    withType<JavaCompile> {
        options.encoding = "UTF-8"
        sourceCompatibility = javaVersion.toString()
        targetCompatibility = javaVersion.toString()
        options.release.set(javaVersion.toString().toInt())
    }
    
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions { jvmTarget = javaVersion.toString() }
    }
    
    jar { from("LICENSE") { rename { "${it}_${base.archivesName}" } } }
    
    processResources {
        inputs.property("version", project.version)
        filesMatching("fabric.mod.json") { expand(mutableMapOf("version" to project.version)) }
    }
    
    java {
        toolchain { languageVersion.set(JavaLanguageVersion.of(javaVersion.toString())) }
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
        withSourcesJar()
    }
}
