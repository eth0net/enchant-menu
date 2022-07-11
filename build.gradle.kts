import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

// import com.modrinth.minotaur.dependencies.ModDependency

val javaVersion = JavaVersion.VERSION_17
val minecraftVersion: String by project
val yarnMappings: String by project
val loaderVersion: String by project
val fabricVersion: String by project
val fabricKotlinVersion: String by project
val modVersion: String by project
val mavenGroup: String by project
val modId: String by project
val modReleaseType: String by project
// val minotaurVersion: String by project
// val modrinthToken = System.getenv("MODRINTH_TOKEN")

plugins {
    id("fabric-loom")
    val kotlinVersion: String by System.getProperties()
    kotlin("jvm").version(kotlinVersion)
    // id("com.modrinth.minotaur").version("2.+")
}

base { archivesName.set(modId) }

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
        options.release.set(targetCompatibility.toInt())
    }

    withType<KotlinCompile> { kotlinOptions { jvmTarget = javaVersion.toString() } }

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

    // modrinth {
    //     print("Uploading to Modrinth...")
    //     token.set(modrinthToken)
    //     projectId.set(archivesBaseName)
    //     versionNumber.set(modVersion)
    //     versionType.set(modReleaseType)
    //     uploadFile.set(remapJar.get())
    //     additionalFiles.set(mutableListOf(remapSourcesJar.get()))
    //     gameVersions.add(minecraftVersion)
    //     dependencies {
    //         required.project("fabric-api")
    //         required.project("fabric-language-kotlin")
    //     }
    //     syncBodyFrom.set(rootProject.file("README.md").toString())
    //     debugMode.set(true)
    //     print("Done!")
    // }
}
