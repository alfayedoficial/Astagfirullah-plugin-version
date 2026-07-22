plugins {
   id("java")
   id("org.jetbrains.kotlin.jvm") version "1.9.20"
   id("org.jetbrains.intellij") version "1.17.3" // Make sure this version is compatible
}


group = "com.alfayedoficial"
version = "2.0.1"

repositories {
   mavenCentral()
   maven { url = uri("https://jitpack.io") }
}

dependencies {
   // Gson for JSON parsing (API responses)
   implementation("com.google.code.gson:gson:2.10.1")

   // Note: Kotlin stdlib and coroutines are provided by IntelliJ Platform

   // Test dependencies
   testImplementation("org.junit.jupiter:junit-jupiter:5.10.1")
   testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.1")
   testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.1")

   // Mockito with Kotlin extensions
   testImplementation("org.mockito:mockito-core:5.8.0")
   testImplementation("org.mockito:mockito-junit-jupiter:5.8.0")
   testImplementation("org.mockito.kotlin:mockito-kotlin:5.2.1")

   // Kotlin coroutines test
   testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
}

intellij {
   version.set("2023.2.6")
   type.set("IC") // Target IDE Platform
   plugins.set(listOf("java", "Kotlin"))
}

tasks {
   // Use Java 17 for all versions to ensure compatibility
   withType<JavaCompile> {
      sourceCompatibility = "17"
      targetCompatibility = "17"
   }
   withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
      kotlinOptions.jvmTarget = "17"
   }

   patchPluginXml {
      // Single source of truth for the version: `project.version` above.
      // plugin.xml no longer hardcodes <version>, so the two can't drift.
      version.set(project.version.toString())
      // Was "231", but the plugin is built against IntelliJ 2023.2 (branch 232), so
      // `verifyPluginConfiguration` flagged it: 231 < 232. Advertising 2023.1 support we
      // never compiled against is a false compatibility claim, so the floor now matches
      // the platform we actually build on.
      sinceBuild.set("232")
      // 2026.2 => branch 262. Branch number = last two digits of the year + release number.
      // Do NOT omit untilBuild: an absent value opts the plugin into every future,
      // unreleased build and into new IDEs we have never verified against.
      untilBuild.set("262.*")
   }

   // The compatibility range declared above is only a CLAIM until the Plugin Verifier
   // proves it. One IDE per declared branch, so a removed API is caught before release.
   runPluginVerifier {
      ideVersions.set(
         listOf(
            "IC-2023.2.6", // sinceBuild floor (232)
            "IC-2023.3.8",
            "IC-2024.1.7",
            "IC-2024.2.5",
            "IC-2024.3.5",
            "IC-2025.1",
            "IC-2025.2",
            "IC-2025.3",
            "IC-2026.1",
            "IC-2026.2", // untilBuild ceiling (262)
         )
      )
   }

   signPlugin {
      certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
      privateKey.set(System.getenv("PRIVATE_KEY"))
      password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
   }

   publishPlugin {
      token.set(System.getenv("PUBLISH_TOKEN"))
   }

   // Add task to build the plugin distribution
   buildPlugin {
      dependsOn("patchPluginXml")
   }

   // Configure test task
   test {
      useJUnitPlatform()
      testLogging {
         events("passed", "skipped", "failed")
         showStandardStreams = true
         showExceptions = true
         showCauses = true
         showStackTraces = true
      }
   }
}

kotlin {
   jvmToolchain {
      languageVersion.set(JavaLanguageVersion.of(17))
   }
}
