plugins {
    id("buildlogic.java-application-conventions")
    id("com.gradleup.shadow") version "8.3.0"
    id("java")
}

repositories {
    mavenCentral()
    maven {
        name = "papermc"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
}

dependencies {
    implementation("org.apache.commons:commons-text")
    //implementation(project(":utilities"))
    compileOnly("com.velocitypowered:velocity-api:3.3.0-SNAPSHOT")
    annotationProcessor("com.velocitypowered:velocity-api:3.3.0-SNAPSHOT")

    implementation("net.dv8tion:JDA:5.0.2")

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
    archiveVersion.set("2.4.1")
    minimize()
}
