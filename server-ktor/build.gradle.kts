plugins {
    application
    id("org.jetbrains.kotlin.jvm") version (rootProject.ext["kotlinVersion"] as String)
    id("io.ktor.plugin") version (rootProject.ext["ktorVersion"] as String)
}


application {
    mainClass.set("com.example.server.ApplicationKt")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=true")
}


repositories { mavenCentral() }


dependencies {
    val ktor = rootProject.ext["ktorVersion"] as String
    implementation("io.ktor:ktor-server-netty:$ktor")
    implementation("io.ktor:ktor-server-core:$ktor")
    implementation("io.ktor:ktor-server-content-negotiation:$ktor")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktor")
    implementation("io.ktor:ktor-server-cors:$ktor")
    implementation("io.ktor:ktor-server-call-logging:$ktor")
    testImplementation("io.ktor:ktor-server-tests-jvm:$ktor")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:${rootProject.ext["kotlinVersion"]}")
}


kotlin {
    jvmToolchain(17)
}