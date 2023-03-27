import org.jetbrains.kotlin.konan.properties.Properties
import java.io.FileInputStream

plugins {
    // Apply the Java Gradle plugin development plugin to add support for developing Gradle plugins
    `java-gradle-plugin`

    // Apply the Kotlin JVM plugin to add support for Kotlin.
    id("org.jetbrains.kotlin.jvm") version "1.6.21"

    `maven-publish`
    signing
}

ext["signing.keyId"] = ""
ext["signing.password"] = ""
ext["signing.secretKeyRingFile"] = ""
ext["ossrhUsername"] = ""
ext["ossrhPassword"] = ""
ext["publishVersion"] = ""
ext["releaseUrl"] = ""
ext["snapshotUrl"] = ""
ext["groupId"] = ""
ext["artifactId"] = ""

val file = project.rootProject.file("publish.settings.gradle.kts")
if (file.exists()) {
    val p = Properties()
    FileInputStream(file).use {
        p.load(it)
        p.forEach { name, value ->
            ext[name.toString()] = value.toString()
        }
    }
} else {
    println("Props file not found")
}

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
}

dependencies {
    // Align versions of all Kotlin components
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))

    // Use the Kotlin JDK 8 standard library.
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    // Use the Kotlin test library.
    testImplementation("org.jetbrains.kotlin:kotlin-test")

    // Use the Kotlin JUnit integration.
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
}

gradlePlugin {
    // Define the plugin
    plugins.create("jycDependencyAnalyzer") {
        id = "happy.jyc.android_dependency_analyzer"
        implementationClass = "happy.jyc.android_dependency_analyzer.AndroidDependencyAnalyzerPlugin"
    }
}

// Add a source set for the functional test suite
val functionalTestSourceSet = sourceSets.create("functionalTest") {
}

configurations["functionalTestImplementation"].extendsFrom(configurations["testImplementation"])

// Add a task to run the functional tests
val functionalTest by tasks.registering(Test::class) {
    testClassesDirs = functionalTestSourceSet.output.classesDirs
    classpath = functionalTestSourceSet.runtimeClasspath
}

gradlePlugin.testSourceSets(functionalTestSourceSet)

tasks.named<Task>("check") {
    // Run the functional tests as part of `check`
    dependsOn(functionalTest)
}


java {
    withJavadocJar()
    withSourcesJar()
}

publishing {
    publications {
        create<MavenPublication>("analyzerPlugin") {
            groupId = project.ext["groupId"].toString()
            artifactId = project.ext["artifactId"].toString()
            version = project.ext["publishVersion"].toString()

            from(components["java"])
            versionMapping{
                usage("java-api") {
                    fromResolutionOf("runtimeClasspath")
                }
                usage("java-runtime") {
                    fromResolutionResult()
                }
            }
            pom {
                name.set("Android Dependency Size Analyzer")
                description.set("a analyzer to analyze android dependency")
                url.set("https://github.com/ChinaCoolder/AndroidDependencySizeAnalyzer")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("jiayichi")
                        name.set("JiaYiChi")
                        email.set("jiayichi.me@foxmail.com")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/ChinaCoolder/AndroidDependencySizeAnalyzer.git")
                    developerConnection.set("scm:git:ssh://github.com/ChinaCoolder/AndroidDependencySizeAnalyzer.git")
                    url.set("https://github.com/ChinaCoolder/AndroidDependencySizeAnalyzer")
                }
            }
        }
    }
    repositories {
        maven {
            name = "MavenCentral"
            val releasesRepoUrl = uri(project.ext["releaseUrl"].toString())
            val snapshotsRepoUrl = uri(project.ext["snapshotUrl"].toString())
            url = if (project.ext["publishVersion"].toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl
            credentials {
                username = project.ext["ossrhUsername"].toString()
                password = project.ext["ossrhPassword"].toString()
            }
        }
    }
}

signing {
    sign(publishing.publications["analyzerPlugin"])
}

tasks.javadoc {
    if (JavaVersion.current().isJava9Compatible) {
        (options as StandardJavadocDocletOptions).addBooleanOption("html5", true)
    }
}
