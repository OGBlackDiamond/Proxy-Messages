plugins {
    id("buildlogic.java-application-conventions")
    id("com.gradleup.shadow") version "9.0.2"
    id("java")
}

repositories {
    mavenCentral()
    maven {
        name = "papermc"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
    maven{
        name = "4.2.0-GeyserMC-SNAPSHOT"
        url = uri("https://repo.opencollab.dev/maven-snapshots")
    }
}

dependencies {
    implementation("org.apache.commons:commons-text")

    compileOnly("com.velocitypowered:velocity-api:3.4.0-SNAPSHOT")
    annotationProcessor("com.velocitypowered:velocity-api:3.4.0-SNAPSHOT")

    //implementation("org.spongepowered:configurate-yaml:4.3.0-SNAPSHOT")
    implementation("org.spongepowered:configurate-yaml:4.2.0-GeyserMC-SNAPSHOT")


    implementation("net.dv8tion:JDA:5.6.1")

    implementation("jakarta.xml.bind:jakarta.xml.bind-api:4.0.0")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

application {
    // Define the main class for the application.
    mainClass = "dev.ogblackdiamond.proxymessages.ProxyMessages"
}


/*
tasks.jar {
    manifest {
        attributes["Main-Class"] = application.mainClass
    }
}
*/


tasks.shadowJar {
    archiveBaseName.set("ProxyMessagesVelocity")
    archiveClassifier.set("")
    archiveVersion.set("4.0.0")
    minimize()

    relocate("org.spongepowered.configurate", "dev.ogblackdiamond.libs.configurate")
    relocate("org.spongepowered.configurate-yaml", "dev.ogblackdiamond.libs.configurate-yaml")
}
