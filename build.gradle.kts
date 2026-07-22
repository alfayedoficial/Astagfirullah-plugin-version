import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
   id("java")
   // 2.4.0 is required: IntelliJ 2026.2 ships Kotlin metadata 2.4.0, which an older
   // compiler refuses to read ("binary version of its metadata is 2.4.0").
   id("org.jetbrains.kotlin.jvm") version "2.4.0"
   // IntelliJ Platform Gradle Plugin 2.x, replacing the deprecated org.jetbrains.intellij
   // 1.x. 1.x is no longer developed, cannot build against 2024.2+, and resolved verifier
   // IDEs through a feed that stops at 2025.3 -- so it could not verify the 2026.x
   // releases this plugin declares support for. 2.18.1 requires Gradle 9.
   id("org.jetbrains.intellij.platform") version "2.18.1"
}


group = "com.alfayedoficial"
version = "2.1.0"

repositories {
   mavenCentral()
   maven { url = uri("https://jitpack.io") }
   intellijPlatform {
      defaultRepositories()
   }
}

dependencies {
   intellijPlatform {
      // Build against the newest platform we declare support for.
      //
      // useInstaller = false is REQUIRED. The default resolves the OS installer (a .dmg
      // on macOS ARM) and those URLs 404, surfacing as a misleading
      // "Could not find idea:ideaIC:<version>". The multi-OS Maven archive
      // com.jetbrains.intellij.idea:ideaIC does exist for 2025.3, 2026.1 and 2026.2.
      create("IC", "2026.2") {
         useInstaller = false
      }
      // NOTE: testFramework(TestFrameworkType.Platform) is deliberately NOT declared.
      // These are plain JUnit unit tests, not BasePlatformTestCase tests. Adding the
      // platform test framework registers com.intellij.tests.JUnit5TestSessionListener,
      // which cannot instantiate outside a real IDE test fixture and kills the whole
      // test executor before a single test runs. The 1.x setup never had it either.
   }

   // Gson for JSON parsing (API responses)
   implementation("com.google.code.gson:gson:2.10.1")

   // Note: Kotlin stdlib and coroutines are provided by IntelliJ Platform

   // Test dependencies.
   // Pinned to 5.10.1 previously, which crashed the test executor with
   // NoSuchMethodError on NamespacedHierarchicalStore$CloseAction.closeAutoCloseables():
   // the IntelliJ 2026.2 test framework ships a newer JUnit Platform, and the launcher
   // and engine must come from the same generation. The BOM keeps them aligned, and the
   // launcher is declared explicitly as Gradle 9 no longer supplies it implicitly.
   testImplementation(platform("org.junit:junit-bom:5.13.4"))
   testImplementation("org.junit.jupiter:junit-jupiter")
   testImplementation("org.junit.jupiter:junit-jupiter-api")
   testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
   testRuntimeOnly("org.junit.platform:junit-platform-launcher")

   // Mockito with Kotlin extensions.
   // 5.8.0 predates full JDK 21 support and its byte-buddy could not mock under the
   // Java 21 toolchain this migration requires -- failures jumped 108 -> 211, entirely
   // MockitoException. Bumped to restore parity.
   testImplementation("org.mockito:mockito-core:5.18.0")
   testImplementation("org.mockito:mockito-junit-jupiter:5.18.0")
   testImplementation("org.mockito.kotlin:mockito-kotlin:5.4.0")

   // Kotlin coroutines test
   testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
}

// Stamp the project version into a resource the plugin can read at runtime.
//
// The version must not be hardcoded in Kotlin (it drifted across three files before), but
// every IntelliJ API for reading your own plugin descriptor -- PluginManagerCore.getPlugin,
// PluginManager.getPluginByClass, PluginManager.getPlugin -- is @ApiStatus.Internal or
// deprecated, and the Plugin Verifier fails the build on INTERNAL_API_USAGES against
// 2026.2. Generating a resource keeps the single source of truth and touches no platform
// API at all.
val generateVersionResource by tasks.registering {
   val versionValue = project.version.toString()
   val outputDir = layout.buildDirectory.dir("generated/versionResource")
   outputs.dir(outputDir)
   inputs.property("version", versionValue)
   doLast {
      outputDir.get().asFile.apply { mkdirs() }
         .resolve("astagfirullah-version.properties")
         .writeText("version=$versionValue\n")
   }
}

sourceSets {
   main {
      resources.srcDir(generateVersionResource)
   }
}

intellijPlatform {
   pluginConfiguration {
      // Single source of truth for the version: `project.version` above. plugin.xml does
      // not hardcode <version>, so the two cannot drift.
      version = project.version.toString()

      ideaVersion {
         // 242 = IntelliJ 2024.2, the first release requiring Java 21. The 2.x plugin
         // compiles to Java 21 bytecode, which will not load on older IDEs -- so the
         // floor moves up from 232 as part of this migration.
         sinceBuild = "242"
         // 262 = IntelliJ 2026.2. Branch number = last two digits of the year + release
         // number. Do NOT omit untilBuild: an absent value opts the plugin into every
         // future, unreleased build and into IDEs never verified against.
         untilBuild = "262.*"
      }
   }

   // The declared range is only a CLAIM until the Verifier proves it. One IDE per
   // declared branch; CI runs these one-per-job via a matrix, because verifying them all
   // on a single hosted runner exhausted its ~14 GB disk.
   pluginVerification {
      ides {
         // CI overrides this with -PpluginVerifierIdeVersions=<one version> and runs one
         // IDE per matrix job. Verifying them all on a single hosted runner exhausted its
         // ~14 GB disk ("No space left on device") -- each IDE is well over a gigabyte.
         // A matrix also gives each IDE a fresh disk and pins a failure to one IDE.
         val requested = providers.gradleProperty("pluginVerifierIdeVersions").orNull
            ?.split(",")?.map(String::trim)?.filter(String::isNotEmpty)

         val versions = requested ?: listOf(
            "2024.2.5", // sinceBuild floor (242)
            "2024.3.5",
            "2025.1",
            "2025.2",
            "2025.3",
            "2026.1",
            "2026.2", // untilBuild ceiling (262)
         )
         versions.forEach { create("IC", it) }
      }
   }

   signing {
      certificateChain = providers.environmentVariable("CERTIFICATE_CHAIN")
      privateKey = providers.environmentVariable("PRIVATE_KEY")
      password = providers.environmentVariable("PRIVATE_KEY_PASSWORD")
   }

   publishing {
      // Environment / CI secret only. A publish token was previously committed to
      // gradle.properties; it is never to live in a tracked file.
      token = providers.environmentVariable("PUBLISH_TOKEN")
   }
}

tasks {
   // 2024.2+ requires Java 21.
   withType<JavaCompile> {
      sourceCompatibility = "21"
      targetCompatibility = "21"
   }
   withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
      compilerOptions.jvmTarget.set(JvmTarget.JVM_21)
   }

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
      languageVersion.set(JavaLanguageVersion.of(21))
   }
}
