// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    extra.apply {
        set("kotlin_version", "1.5.10")
        set("hilt_version", "2.37")
    }

    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:4.2.1")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${project.extra.get("kotlin_version")}")
        classpath("com.google.dagger:hilt-android-gradle-plugin:${project.extra.get("hilt_version")}")
        classpath("com.diffplug.spotless:spotless-plugin-gradle:5.14.0")

        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle.kts files
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
        jcenter() // Warning: this repository is going to shut down soon
    }
}

tasks.register("clean", Delete::class) { delete(rootProject.buildDir) }
