plugins {
    id("java")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:6.0.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}

tasks.compileJava {
    options.release.set(21)
}

tasks.jar {
    archiveFileName.set("CartoonSurvivors-Server.jar")
    manifest {
        attributes["Main-Class"] = "Main"
    }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    val runtimeClasspath = configurations.runtimeClasspath
    dependsOn(runtimeClasspath)
    from({
        runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) }
    })
    exclude("META-INF/INDEX.LIST", "META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA")
}
