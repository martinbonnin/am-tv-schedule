plugins {
    id("org.jetbrains.kotlin.jvm").version("1.6.21")
    id("org.jetbrains.kotlin.plugin.serialization").version("1.6.21")
}

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")
    implementation("com.squareup.okhttp3:okhttp:4.9.3")
    testImplementation("junit:junit:4.12")
}