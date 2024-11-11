plugins {
    `maven-publish`
    alias(libs.plugins.shadow)
    alias(libs.plugins.publishdata)
    java
    id("xyz.jpenilla.run-paper") version "2.3.1"
    id("net.minecrell.plugin-yml.paper") version "0.6.0"
}

group = "net.onelitefeather"
version = "0.0.1"

dependencies {
    compileOnly(libs.paper)
    implementation(libs.cloud.core)
    implementation(libs.cloud.paper)
}
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks {
    compileJava {
        options.release.set(21)
        options.encoding = "UTF-8"
    }
    runServer {
        minecraftVersion("1.21.1")
    }
    shadowJar {
        mergeServiceFiles()
    }
}

paper {
    main = "net.onelitefeather.gameruletemplate.GameRuleTemplate"
    apiVersion = "1.21"
    authors = listOf("TheMeinerLP", "OneLiteFeatherNET")
    description = "A plugin that allows to copy and paste gamerules"
}


publishData {
    addBuildData()
    val projectId: String by project
    val gitlabUrl: String by project
    useGitlabReposForProject(projectId, gitlabUrl)
    publishTask("shadowJar")
}

publishing {
    publications.create<MavenPublication>("maven") {
        // configure the publication as defined previously.
        publishData.configurePublication(this)
        version = publishData.getVersion(false)
    }

    repositories {
        maven {
            credentials(HttpHeaderCredentials::class) {
                name = "Job-Token"
                value = System.getenv("CI_JOB_TOKEN")
            }
            authentication {
                create("header", HttpHeaderAuthentication::class)
            }


            name = "Gitlab"
            // Get the detected repository from the publishing data
            url = uri(publishData.getRepository())
        }
    }
}

