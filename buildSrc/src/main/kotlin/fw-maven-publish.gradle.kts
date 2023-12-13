plugins {
    id("maven-publish")
    signing
}

afterEvaluate {
    publishing {
        publications {
            val publication = create<MavenPublication>("release") {
                configurePublication()
                configurePom()
            }
            signing.sign(publication)
        }

        repositories {
            maven {
                configureUrl()
                configureCredentials()
            }
        }
    }

    setProperty("signing.keyId", System.getenv("MOHSENOID_SIGNING_KEY_ID"))
    setProperty("signing.password", System.getenv("MOHSENOID_SIGNING_PASSWORD"))
}

fun MavenPublication.configurePublication() {
    groupId = project.ext.properties["PUBLICATION_GROUP_ID"].toString()
    artifactId = project.ext.properties["PUBLICATION_ARTIFACT_ID"].toString()
    version = project.ext.properties["PUBLICATION_VERSION"].toString()

    from(components["release"])
}

fun MavenPublication.configurePom() {
    pom {
        name.set(project.name)
        packaging = "aar"
        description.set("Android Picture-in-Picture - ${project.path}")
        url.set("https://mohsenoid.com")

        configureLicense()

        configureDeveloper()

        configureScm()
    }
}

fun MavenPom.configureLicense() {
    licenses {
        license {
            name.set("The Apache License, Version 2.0")
            url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
        }
    }
}

fun MavenPom.configureDeveloper() {
    developers {
        developer {
            id.set("mohsenoid")
            name.set("Mohsen Mirhoseini")
            email.set("contact@mohsenoid.com")
        }
    }
}

fun MavenPom.configureScm() {
    scm {
        url.set("https://github.com/mohsenoid/Android-PiP")
        connection.set("scm:git:git@github.com:mohsenoid/Android-PiP.git")
        developerConnection.set("scm:git:git@github.com:mohsenoid/Android-PiP.git")
    }
}

fun MavenArtifactRepository.configureUrl() {
    val releasesRepoUrl = "https://oss.sonatype.org/service/local/staging/deploy/maven2"
    val snapshotsRepoUrl = "https://oss.sonatype.org/content/repositories/snapshots/"

    val isSnapshot = project.ext.properties["PUBLICATION_VERSION"].toString().endsWith("SNAPSHOT", true)

    val repoUrl = if (isSnapshot) snapshotsRepoUrl else releasesRepoUrl

    setUrl(repoUrl)
}

fun MavenArtifactRepository.configureCredentials() {
    credentials {
        username = System.getenv("MOHSENOID_OSSRH_USERNAME")
        password = System.getenv("MOHSENOID_OSSRH_PASSWORD")
    }
}
