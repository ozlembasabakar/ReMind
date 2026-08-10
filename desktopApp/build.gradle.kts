import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinxSerialization)
}

dependencies {
    implementation(project(":shared"))

    implementation(compose.desktop.currentOs)
    implementation(libs.kotlinx.coroutinesSwing)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.google.auth.oauth2)

    implementation(libs.compose.uiToolingPreview)
}

sourceSets {
    main {
        resources.srcDirs("src/desktopMain/resources")
    }
}

tasks.withType<ProcessResources> {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

tasks.register<JavaExec>("importWords") {
    group = "application"
    description = "Import words from words.json into Cloud Firestore"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.ozlembasabakar.remind.importer.WordsImporterKt")
    workingDir = rootDir
}

compose.desktop {
    application {
        mainClass = "com.ozlembasabakar.remind.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "com.ozlembasabakar.remind"
            packageVersion = "1.0.0"

            macOS {
                iconFile.set(project.file("src/desktopMain/resources/icon.icns"))
            }
            windows {
                iconFile.set(project.file("src/desktopMain/resources/icon.ico"))
            }
            linux {
                iconFile.set(project.file("src/desktopMain/resources/icon.png"))
            }
        }
    }
}