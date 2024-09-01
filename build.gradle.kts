plugins {
    // Apply the Java Gradle plugin development plugin to add support for developing Gradle plugins
    id("java-gradle-plugin")
    id("maven-publish")
    `kotlin-dsl`

//    id("net.darkhax.curseforgegradle") version "1.0.10"
//    id("com.modrinth.minotaur") version "2.+"
}

group = "at.petra-k.pkpcpbp"
version = "%s-pre-%s".format(properties["pluginVersion"], System.getenv("BUILD_NUMBER"))

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation(gradleApi())

    implementation("blue.endless:jankson:1.2.2")
    implementation("com.diluv.schoomp:Schoomp:1.2.6")

    implementation(group = "net.darkhax.curseforgegradle", name = "CurseForgeGradle", version = "1.0.10")
    implementation(group = "com.modrinth.minotaur", name = "Minotaur", version = "2.8.7")
    implementation("org.junit.jupiter:junit-jupiter:5.8.1")
}

gradlePlugin {
    // Define the plugin
    plugins {
        create("pkpcpbp") {
            id = "at.petra-k.pkpcpbp.PKPlugin"
            displayName = "P.K.P.C.P.B.P."
            description = "Petra Kat's Pretty Cool Publishing Boilerplate Plugin"
            implementationClass = "at.petrak.pkpcpbp.PKPlugin"
        }
        create("pkJson5") {
            id = "at.petra-k.pkpcpbp.PKJson5Plugin"
            displayName = "PK Json5"
            description = "Flattens/transpiles json5 to json. You can just use this plugin by itself hopefully"
            implementationClass = "at.petrak.pkpcpbp.PKJson5Plugin"
        }
        create("pkpcpbpSubproj") {
            id = "at.petra-k.pkpcpbp.PKSubprojPlugin"
            displayName = "PKPCPBP (Subprojs)"
            description = "Sets up PKPCPBP stuff for subprojects."
            implementationClass = "at.petrak.pkpcpbp.PKSubprojPlugin"
        }
    }
}

publishing {
    publications {
        create("mavenJava", MavenPublication::class.java) {
            groupId = project.group.toString()
            artifactId = "pkpcpb"
            version = project.version.toString()
            from(components.getByName("java"))
        }
    }

    repositories {
        maven("file:///" + System.getenv("local_maven"))
    }
}

tasks.named<Test>("test") {
    useJUnitPlatform()

    maxHeapSize = "1G"

    testLogging {
        events("passed")
    }
}