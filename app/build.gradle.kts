import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose)
}

kotlin {
    jvm("desktop") {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    jvmToolchain(17)

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.materialIconsExtended)
                implementation(compose.components.resources)
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.ktor.client.core)
                implementation(libs.androidx.lifecycle.viewmodel)
                implementation(libs.androidx.lifecycle.runtime.compose)
            }
        }
        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(libs.kotlinx.coroutines.swing)
                implementation(libs.ktor.client.okhttp)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }
    }
}

compose.desktop {
    application {
        mainClass = "net.pedromalta.ipodfeeder.MainKt"
        javaHome = "/Library/Java/JavaVirtualMachines/zulu-17.jdk/Contents/Home"

        nativeDistributions {
            packageName = "iPodFeeder"
            targetFormats(TargetFormat.Dmg)
            appResourcesRootDir.set(project.layout.projectDirectory.dir("src/desktopMain/resources"))
            macOS {
                iconFile.set(project.file("src/desktopMain/resources/icon.png"))
            }
            windows {
                iconFile.set(project.file("src/desktopMain/resources/icon.png"))
            }
            linux {
                iconFile.set(project.file("src/desktopMain/resources/icon.png"))
            }
        }
    }
}