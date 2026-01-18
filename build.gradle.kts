plugins {
   id("java")
   id("org.jetbrains.kotlin.jvm") version "1.9.20"
   id("org.jetbrains.intellij") version "1.17.3" // Make sure this version is compatible
}


group = "com.alfayedoficial"
version = "2.0.0"

repositories {
   mavenCentral()
   maven { url = uri("https://jitpack.io") }
}

dependencies {
   // Gson for JSON parsing (API responses)
   implementation("com.google.code.gson:gson:2.10.1")

   // Note: Kotlin stdlib and coroutines are provided by IntelliJ Platform
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
}

kotlin {
   jvmToolchain {
      languageVersion.set(JavaLanguageVersion.of(17))
   }
}
