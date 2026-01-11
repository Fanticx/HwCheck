plugins {
    java
    `java-library`
    id("io.github.goooler.shadow") version "8.1.8"
}

group = "ru.qWins"
version = "1.0.0"

repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://jitpack.io")
    maven("https://storehouse.okaeri.eu/repository/maven-releases/")
    maven("https://repo.panda-lang.org/releases")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    mavenCentral()
}

dependencies {
    compileOnly("com.destroystokyo.paper:paper-api:1.16.5-R0.1-SNAPSHOT")

    compileOnly("com.github.Fanticx:AnnotationPlugin:1.1.0")
    annotationProcessor("com.github.Fanticx:AnnotationPlugin:1.1.0")

    compileOnly("org.projectlombok:lombok:1.18.38")
    annotationProcessor("org.projectlombok:lombok:1.18.38")

    implementation("eu.okaeri:okaeri-configs-hjson:5.0.5")
    implementation("eu.okaeri:okaeri-configs-yaml-bukkit:5.0.5")
    implementation("eu.okaeri:okaeri-configs-serdes-commons:5.0.5")
    implementation("eu.okaeri:okaeri-configs-serdes-bukkit:5.0.5")

    implementation("dev.rollczi:litecommands-bukkit:3.9.7")
    compileOnly("me.clip:placeholderapi:2.11.6")

    testCompileOnly("org.projectlombok:lombok:1.18.38")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.38")
}
    val targetJavaVersion = 17
    java {
        val javaVersion = JavaVersion.toVersion(targetJavaVersion)
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
        if (JavaVersion.current() < javaVersion) {
            toolchain.languageVersion.set(JavaLanguageVersion.of(targetJavaVersion))
        }
    }

    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        if (targetJavaVersion >= 10 || JavaVersion.current().isJava10Compatible()) {
            options.release.set(targetJavaVersion)
        }
    }

    tasks.processResources {
        filteringCharset = "UTF-8"
    }

    tasks.jar {
        dependsOn(configurations.runtimeClasspath)
        archiveBaseName.set("HwCheck")

        duplicatesStrategy = DuplicatesStrategy.INCLUDE
        from(configurations.runtimeClasspath.get().files.map { if (it.isDirectory()) it else zipTree(it) })
    }

    tasks.shadowJar {
        archiveBaseName.set("HwCheck")
        archiveClassifier.set("")
    }
