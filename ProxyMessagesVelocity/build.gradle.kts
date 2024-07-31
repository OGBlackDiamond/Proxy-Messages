plugins {
    id("buildlogic.java-application-conventions")
}

repositories {
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
