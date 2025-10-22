plugins {
    id("java")
}

group = "joamonca.reactinator"
version = "1.0-SNAPSHOT"
val jdaVersion = "6.1.0"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    implementation("org.jetbrains:annotations:24.1.0")

    implementation("net.dv8tion:JDA:$jdaVersion") { // replace $version with the latest version
        // Optionally disable audio natives to reduce jar size by excluding `opus-java` and `tink`
        exclude(module="opus-java") // required for encoding audio into opus, not needed if audio is already provided in opus encoding
        exclude(module="tink") // required for encrypting and decrypting audio
    }
}

tasks.test {
    useJUnitPlatform()
}