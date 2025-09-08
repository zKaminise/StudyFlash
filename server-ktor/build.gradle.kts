plugins {
    application

    // sem versão aqui (evita conflito com o que já está no classpath do projeto Android)
    kotlin("jvm")

    // precisa de versão aqui
    kotlin("plugin.serialization") version "1.9.24"

    // versão do plugin do Ktor
    id("io.ktor.plugin") version "2.3.12"
}

kotlin {
    // use JDK 17
    jvmToolchain(17)
}

application {
    mainClass.set("com.example.server.ApplicationKt")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=true")
}

dependencies {
    implementation("io.ktor:ktor-server-netty:2.3.12")
    implementation("io.ktor:ktor-server-core:2.3.12")
    implementation("io.ktor:ktor-server-content-negotiation:2.3.12")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.12")
    implementation("io.ktor:ktor-server-cors:2.3.12")
    implementation("io.ktor:ktor-server-call-logging:2.3.12")
    implementation("io.ktor:ktor-server-status-pages:2.3.12")
    implementation("ch.qos.logback:logback-classic:1.5.6")

    testImplementation(kotlin("test"))
}
