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
      sinceBuild.set("231")
      untilBuild.set("253.*")
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
