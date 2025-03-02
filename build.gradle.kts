plugins {
    kotlin("jvm") version "2.0.21"
    application
}

group = "com.groupscholar"
version = "0.1.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-cli:0.3.6")
    implementation("org.jetbrains.exposed:exposed-core:0.55.0")
    implementation("org.jetbrains.exposed:exposed-dao:0.55.0")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.55.0")
    implementation("org.jetbrains.exposed:exposed-java-time:0.55.0")
    implementation("org.jetbrains.exposed:exposed-java-time:0.55.0")
    implementation("org.postgresql:postgresql:42.7.4")
    implementation("org.slf4j:slf4j-simple:2.0.13")

    testImplementation(kotlin("test"))
    testImplementation("com.h2database:h2:2.3.232")
}

application {
    mainClass.set("com.groupscholar.impactstories.AppKt")
}

kotlin {
    jvmToolchain(21)
}
