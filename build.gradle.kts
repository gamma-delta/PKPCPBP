import org.jetbrains.kotlin.gradle.plugin.mpp.pm20.archivesName

plugins {
    // Apply the Java Gradle plugin development plugin to add support for developing Gradle plugins
    id("java-gradle-plugin")
    id("maven-publish")
    `kotlin-dsl`
}

group = "at.petra-k.pkpcpbp"
version = "%s-pre-%s".format(properties["pluginVersion"], System.getenv("BUILD_NUMBER"))

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
}

dependencies {
    implementation(gradleApi())

    implementation("blue.endless:jankson:1.2.2")
    implementation("com.diluv.schoomp:Schoomp:1.2.6")
}

gradlePlugin {
    // Define the plugin
    plugins {
        create("pkpcpbp") {
            id = "at.petra-k.PKPlugin"
            displayName = "P.K.P.C.P.B.P."
            description = "Petra Kat's Pretty Cool Publishing Boilerplate Plugin"
            implementationClass = "at.petrak.pkpcpbp.PKPlugin"
        }
        create("pkpcpbpSubproj") {
            id = "at.petra-k.PKSubprojPlugin"
            displayName = "PKPCPBP (Subprojs)"
            description = "Subprojects PKPCPBP"
            implementationClass = "at.petrak.pkpcpbp.PKSubprojPlugin"
        }
    }
}

publishing {
    publications {
        create("mavenJava", MavenPublication::class.java) {
            groupId = project.group.toString()
            artifactId = project.archivesName
            version = project.version.toString()
            from(components.getByName("java"))
        }
    }

    repositories {
        maven("file:///" + System.getenv("local_maven"))
    }
}
