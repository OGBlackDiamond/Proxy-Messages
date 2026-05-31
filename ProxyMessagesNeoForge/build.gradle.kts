plugins {
    id("net.neoforged.gradle.userdev") version "7.0.97"
    id("java")
}

version = "4.0.0"
group = "dev.ogblackdiamond"

base {
    archivesName = "ProxyMessagesNeoForge"
}

runs {
    // Server-only mod — no client run config needed.
    create("server") {
        server()
        programArgument("--nogui")
        modSource(sourceSets.main.get())
    }
}

dependencies {
    // NeoForge 47.x targets Minecraft 1.20.1.
    implementation("net.neoforged:neoforge:47.1.106")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks.withType<ProcessResources>().configureEach {
    inputs.property("version", project.version)
    filesMatching("META-INF/mods.toml") {
        expand("version" to project.version)
    }
}
