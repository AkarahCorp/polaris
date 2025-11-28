plugins {
    id("fabric-loom") version "1.11-SNAPSHOT"
    id("maven-publish")
}

version = project.property("mod_version")!!
group = project.property("maven_group")!!

base {
    archivesName.set(project.property("archives_base_name") as String)
}

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
    val loaderVersion = project.property("loader_version")!!
    val fabricVersion = project.property("fabric_version")!!

    minecraft("com.mojang:minecraft:$minecraftVersion")
    mappings(loom.officialMojangMappings())
    modImplementation("net.fabricmc:fabric-loader:$loaderVersion")
    modImplementation("net.fabricmc.fabric-api:fabric-api:$fabricVersion")
    modImplementation("com.github.luben:zstd-jni:1.5.7-4")
    modImplementation("org.java-websocket:Java-WebSocket:1.6.0")
    include("org.java-websocket:Java-WebSocket:1.6.0")
    include("com.github.luben:zstd-jni:1.5.7-4")
}

tasks.processResources {
    val minecraftVersion = project.property("minecraft_version")!!
    val loaderVersion = project.property("loader_version")!!

    inputs.property("version", version)
    inputs.property("minecraft_version", minecraftVersion)
    inputs.property("loader_version", loaderVersion)
    filteringCharset = "UTF-8"

    filesMatching("fabric.mod.json") {
        expand(
                mapOf(
                        "version" to version,
                        "minecraft_version" to minecraftVersion,
                        "loader_version" to loaderVersion
                )
        )
    }
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

tasks.jar {
    from("LICENSE") {
        rename { "${it}_${project.property("archives_base_name")}" }
    }
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
