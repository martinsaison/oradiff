plugins {
    kotlin("jvm") version "1.8.21"
    application
}

group = "com.khodev"
version = "1.0.0"

repositories {
    mavenCentral()
}

java.sourceCompatibility = JavaVersion.VERSION_1_8


dependencies {
    implementation("org.jdom:jdom2:2.0.6.1")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.15.2")
    implementation("commons-cli:commons-cli:1.3")
    implementation("com.oracle.database.jdbc:ojdbc8:23.2.0.0")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

application {
    mainClass.set("com.khodev.oradiff.Cli")
}


