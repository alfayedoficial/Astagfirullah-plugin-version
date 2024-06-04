plugins {
   id("java")
   id("org.jetbrains.kotlin.jvm") version "1.9.20"
   id("org.jetbrains.intellij") version "1.17.3" // Make sure this version is compatible
}

group = "com.alfayedoficial"
version = "1.0.3"

repositories {
   mavenCentral()
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
   withType<JavaCompile> {
      sourceCompatibility = "17"
      targetCompatibility = "17"
   }
   withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
      kotlinOptions.jvmTarget = "17"
   }

   patchPluginXml {
      sinceBuild.set("231")
      untilBuild.set("241.*")
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
