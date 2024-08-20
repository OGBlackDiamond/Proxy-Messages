plugins {
    id("buildlogic.java-application-conventions")
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
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

application {
    // Define the main class for the application.
    mainClass = "dev.ogblackdiamond.proxymessages.ProxyMessages"
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = application.mainClass
    }
}
