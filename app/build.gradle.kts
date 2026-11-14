plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "id.datzon.glasslab"
    compileSdk = 36
    defaultConfig {
        applicationId = "id.datzon.glasslab"
        minSdk = 33
        targetSdk = 35
        versionCode = 2
        versionName = "0.2.0"
    }
    buildTypes { release { isMinifyEnabled = false } }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    testOptions { unitTests.isIncludeAndroidResources = true }
    buildFeatures { compose = true }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2025.10.01")
    implementation(composeBom)
    implementation("androidx.activity:activity-compose:1.11.0")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
    testImplementation("org.robolectric:robolectric:4.16")
    testImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}

// Package audited, non-secret implementation files so Export always matches this APK.
val exportSources by
    tasks.registering(Copy::class) {
        from("src/main/java/id/datzon/glasslab") {
            include(
                "GlassConfig.kt",
                "GlassEngine.kt",
                "LabMotion.kt",
                "NativePreview.kt",
                "LabIcons.kt",
            )
        }
        from(rootProject.projectDir) { include("THIRD_PARTY_NOTICES.md", "SOURCE_PROVENANCE.md") }
        into(layout.buildDirectory.dir("generated/glassExportAssets/export"))
    }

android.sourceSets["main"].assets.srcDir(layout.buildDirectory.dir("generated/glassExportAssets"))

tasks.named("preBuild").configure { dependsOn(exportSources) }

kotlin { compilerOptions { jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17) } }

// Compile the exact standalone Compose export as test source, catching missing imports/types.
val exportCompileDir = layout.buildDirectory.dir("generated/exportCompileCheck")
val generateExportCompileCheck by
    tasks.registering {
        val sourceDir = file("src/main/java/id/datzon/glasslab")
        inputs.files(
            file("$sourceDir/GlassConfig.kt"),
            file("$sourceDir/GlassEngine.kt"),
            file("$sourceDir/ExportTools.kt"),
        )
        outputs.dir(exportCompileDir)
        doLast {
            val pieces =
                listOf("GlassConfig.kt", "GlassEngine.kt").map { file("$sourceDir/$it").readText() }
            val imports =
                (pieces.flatMap { it.lines() }.filter { it.startsWith("import ") } +
                        listOf(
                            "import androidx.compose.foundation.layout.*",
                            "import androidx.compose.ui.unit.*",
                        ))
                    .distinct()
            val component =
                file("$sourceDir/ExportTools.kt")
                    .readText()
                    .substringAfter("private val COMPONENT_SOURCE")
                    .substringAfter("\"\"\"")
                    .substringBefore("\"\"\"")
            val output = exportCompileDir.get().file("ExportedGlass.kt").asFile
            output.parentFile.mkdirs()
            output.writeText(
                "package id.datzon.exportcompilecheck\n" +
                    imports.joinToString("\n") +
                    "\n" +
                    pieces.joinToString("\n") { text ->
                        text
                            .lines()
                            .filterNot { it.startsWith("package ") || it.startsWith("import ") }
                            .joinToString("\n")
                    } +
                    "\n" +
                    component
            )
        }
    }

android.sourceSets["test"].java.srcDir(exportCompileDir)

tasks
    .matching {
        it.name.contains("UnitTest") &&
            (it.name.startsWith("compile") || it.name.contains("lint", ignoreCase = true))
    }
    .configureEach { dependsOn(generateExportCompileCheck) }
