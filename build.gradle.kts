plugins {
    id("java")
    application
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "joamonca.reactinator"
version = "1.1.0"
val jdaVersion = "6.1.0"
val jsonVersion = "20250517"
val mysqlVersion = "9.4.0"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    implementation("org.jetbrains:annotations:24.1.0")

    // https://mvnrepository.com/artifact/org.json/json
    implementation("org.json:json:$jsonVersion")

    // https://mvnrepository.com/artifact/com.mysql/mysql-connector-j
    implementation("com.mysql:mysql-connector-j:${mysqlVersion}")

    implementation("net.dv8tion:JDA:$jdaVersion") {
        exclude(module = "opus-java")
        exclude(module = "tink")
    }
}

tasks.test {
    useJUnitPlatform()
}

// Main class discovered in your repo:
// src/main/java/joamonca/reactinator/Main.java
application {
    mainClass.set("joamonca.reactinator.Main")
}

// Configure the fat jar
tasks.named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
    archiveClassifier.set("all")
    // Helps when libraries use service files (safe for most cases)
    mergeServiceFiles()
    // Ensure the jar is runnable with `java -jar`
    manifest {
        attributes("Main-Class" to application.mainClass.get())
    }
}