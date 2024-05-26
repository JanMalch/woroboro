// Top-level build file where you can add configuration options common to all sub-projects/modules.
@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    alias(libs.plugins.com.android.application) apply false
    alias(libs.plugins.org.jetbrains.kotlin.android) apply false
    alias(libs.plugins.com.google.dagger.hilt.android) apply false
    alias(libs.plugins.com.google.devtools.ksp) apply false
    alias(libs.plugins.spotless)
}
true // Needed to make the Suppress annotation work for the plugins block
buildscript {
    dependencies {
        classpath(libs.google.oss.licenses.plugin) {
            exclude(group = "com.google.protobuf")
        }
    }
}

configure<com.diffplug.gradle.spotless.SpotlessExtension> {
    ratchetFrom = "origin/main"

    kotlin {
        target("**/*.kt")
        ktfmt(libs.versions.ktfmt.get()).kotlinlangStyle()
    }
}

task<Exec>("gitConfigApply") {
    executable = "git"
    args("config", "--local", "include.path", "../.gitconfig")
}
