import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.net.URI

val javaVersion = JavaVersion.VERSION_17
val minecraftVersion: String by project
val yarnMappings: String by project
val loaderVersion: String by project
val fabricVersion: String by project
val fabricVersionId: String by project
val fabricKotlinVersion: String by project
val fabricKotlinVersionId: String by project
val completeConfigVersion: String by project
val completeConfigVersionId: String by project
val mavenGroup: String by project
val modId: String by project
val modVersion: String by project
val modVersionName: String by project
val modVersionType: String by project

val changelogFile = project.file("changelogs/$modVersion.md")
val changelogText = if (changelogFile.exists()) changelogFile.readText() else "No changelog provided."

plugins {
    id("fabric-loom")
    val kotlinVersion: String by System.getProperties()
    kotlin("jvm").version(kotlinVersion)
    id("com.modrinth.minotaur").version("2.+")
}

base { archivesName.set(modId) }

group = mavenGroup
version = modVersion

repositories {
    maven { url = URI("https://jitpack.io") }
    maven { url = URI("https://maven.terraformersmc.com/") }
    maven { url = URI("https://maven.shedaniel.me/") }
    maven { url = URI("https://maven.siphalor.de/") }
}

dependencies {
    minecraft("com.mojang", "minecraft", minecraftVersion)
    mappings("net.fabricmc", "yarn", yarnMappings, null, "v2")
    modImplementation("net.fabricmc", "fabric-loader", loaderVersion)
    modImplementation("net.fabricmc.fabric-api", "fabric-api", fabricVersion)
    modImplementation("net.fabricmc", "fabric-language-kotlin", fabricKotlinVersion)
    modImplementation("com.gitlab.Lortseam", "completeconfig", completeConfigVersion)
}

tasks {
    withType<JavaCompile> {
        options.encoding = "UTF-8"
        sourceCompatibility = javaVersion.toString()
        targetCompatibility = javaVersion.toString()
        options.release.set(targetCompatibility.toInt())
    }

    withType<KotlinCompile> { kotlinOptions { jvmTarget = javaVersion.toString() } }

    jar { from("LICENSE") { rename { "${it}_${base.archivesName}" } } }

    processResources {
        inputs.property("version", project.version)
        filesMatching("fabric.mod.json") { expand(mapOf("version" to project.version)) }
    }

    java {
        toolchain { languageVersion.set(JavaLanguageVersion.of(javaVersion.toString())) }
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
        withSourcesJar()
    }

    modrinth {
        token.set(System.getenv("MODRINTH_TOKEN"))
        gameVersions.set(listOf("1.18.2"))
        projectId.set(base.archivesName)
        versionName.set(modVersionName)
        versionType.set(modVersionType)
        changelog.set(changelogText)
        uploadFile.set(remapJar.get())
        additionalFiles.set(listOf(remapSourcesJar.get()))
        dependencies {
            required.version(fabricVersionId)
            required.version(fabricKotlinVersionId)
            optional.project("cloth-config")
            optional.version(completeConfigVersionId)
            optional.project("modmenu")
        }
//        syncBodyFrom.set(rootProject.file("README.md").toString())
    }
}
