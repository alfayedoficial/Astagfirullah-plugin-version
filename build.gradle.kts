plugins {
   id("java")
   id("org.jetbrains.kotlin.jvm") version "1.9.20"
   id("org.jetbrains.intellij") version "1.17.3" // Make sure this version is compatible
}


group = "com.alfayedoficial"
version = "1.1.3"

repositories {
   mavenCentral()
   maven { url = uri("https://jitpack.io") }
}

dependencies {
   implementation("org.jetbrains.kotlin:kotlin-stdlib:1.9.20")
   implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.1")
   implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.7.1")
}

intellij {
   version.set("2023.2.6")
   type.set("IC") // Target IDE Platform
   plugins.set(listOf("java", "Kotlin"))
}

tasks {
   // Use Java 17 for versions up to 241
   withType<JavaCompile> {
      if (intellij.version.get().startsWith("242")) {
         sourceCompatibility = "21"
         targetCompatibility = "21"
      } else {
         sourceCompatibility = "17"
         targetCompatibility = "17"
      }
   }
   withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
      if (intellij.version.get().startsWith("242")) {
         kotlinOptions.jvmTarget = "21"
      } else {
         kotlinOptions.jvmTarget = "17"
      }
   }

   patchPluginXml {
      sinceBuild.set("231")
      untilBuild.set("242.*")
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
      if (intellij.version.get().startsWith("242")) {
         languageVersion.set(JavaLanguageVersion.of(21))
      } else {
         languageVersion.set(JavaLanguageVersion.of(17))
      }
   }
}
