plugins {
    kotlin("multiplatform") version "2.3.10" apply false
    kotlin("plugin.compose") version "2.3.10" apply false
    kotlin("plugin.serialization") version "2.3.10" apply false
    id("com.android.kotlin.multiplatform.library") version "8.12.0" apply false
    id("com.google.devtools.ksp") version "2.3.10-1.2.8" apply false
    id("org.jetbrains.compose") version "1.11.0" apply false
    id("org.jlleitschuh.gradle.ktlint") version "14.0.1" apply false
}
