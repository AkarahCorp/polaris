plugins {
    id("fabric-loom") version "1.11-SNAPSHOT"
    id("maven-publish")
}

version = project.property("mod_version")!!
group = project.property("maven_group")!!

repositories {
    maven {
        name = "sonatype-oss-snapshots1"
        url = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
        mavenContent { snapshotsOnly() }
    }
    mavenCentral()
}

loom {
    accessWidenerPath = file("src/main/resources/polaris.accesswidener")
}

dependencies {
    val minecraftVersion = project.property("minecraft_version")!!

    minecraft("com.mojang:minecraft:$minecraftVersion")
    mappings(loom.officialMojangMappings())
    compileOnly("com.github.luben:zstd-jni:1.5.7-4")
    compileOnly("org.java-websocket:Java-WebSocket:1.6.0")
}

val targetJavaVersion = 24

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    if (targetJavaVersion >= 10 || JavaVersion.current().isJava10Compatible) {
        options.release.set(targetJavaVersion)
    }
}

java {
    val javaVersion = JavaVersion.toVersion(targetJavaVersion)
    if (JavaVersion.current() < javaVersion) {
        toolchain.languageVersion.set(JavaLanguageVersion.of(targetJavaVersion))
    }
    withSourcesJar()
}

tasks.register("buildDocs", JavaExec::class) {
    group = "polarisCustomTasks"
    mainClass = "dev.akarah.polaris.GenerateDocs"
    classpath = java.sourceSets["main"].runtimeClasspath
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifactId = project.property("archives_base_name") as String
            from(components["java"])
        }
    }

    repositories {
        // Add publishing repositories here
    }
}
