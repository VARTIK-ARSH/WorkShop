// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
}
buildscript {
    repositories {
        google()
        mavenCentral()
        remove(jcenter())
    }
    dependencies {
        classpath(libs.realm.gradle.plugin)
    }
}
tasks.register<Delete>("clean") {
    delete(rootProject.buildDir)
}