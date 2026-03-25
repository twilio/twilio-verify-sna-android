import org.jfrog.gradle.plugin.artifactory.dsl.ArtifactoryPluginConvention

/*
 * Copyright (c) 2022 Twilio Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

plugins {
  alias(libs.plugins.androidLibrary)
  alias(libs.plugins.kotlinAndroid)
  alias(libs.plugins.jfrogFactory)
  alias(libs.plugins.mavenPublish)
  alias(libs.plugins.apkscale)
  signing
  alias(libs.plugins.kover)
}

val verifySnaVersionName: String by rootProject.extra
val pomGroup: String by project
val pomArtifactId: String by project

android {
  namespace = "com.twilio.verify_sna"
  compileSdk = 36

  defaultConfig {
    minSdk = 21
    targetSdk = 36

    testInstrumentationRunner= "androidx.test.runner.AndroidJUnitRunner"
    consumerProguardFiles("consumer-rules.pro")
    version = verifySnaVersionName
  }

  buildTypes {
    release {
      isMinifyEnabled = false
      proguardFiles(
        getDefaultProguardFile("proguard-android-optimize.txt"),
        "proguard-rules.pro"
      )
    }
  }
}

kotlin {
  jvmToolchain(17)
}

val sourcesJar by tasks.registering(Jar::class) {
  archiveClassifier.set("sources")
  from(android.sourceSets.getByName("main").java.srcDirs)
}

signing {
  sign(publishing.publications)
}

publishing {
  publications {
    create<MavenPublication>("verifySnaAndroid") {
      groupId = pomGroup
      artifactId = pomArtifactId
      version = verifySnaVersionName
      afterEvaluate {
        from(components["release"])
      }
      //artifact(sourcesJar)
      tasks.named("generateMetadataFileForVerifySnaAndroidPublication") {
        dependsOn(sourcesJar)
      }
      pom {
        name.set("twilio-verify-sna-android")
        description.set("Twilio Verify SNA SDK for Android")
        url.set("https://github.com/twilio/twilio-verify-sna-android")
        licenses {
          license {
            name.set("Apache License, Version 2.0")
            url.set("https://github.com/twilio/twilio-verify-sna-android/blob/main/LICENSE")
          }
        }
        developers {
          developer {
            id.set("Twilio")
            name.set("Twilio")
          }
        }
        scm {
          connection.set("scm:git:github.com/twilio/twilio-verify-sna-android.git")
          developerConnection.set("scm:git:ssh://github.com/twilio/twilio-verify-sna-android.git")
          url.set("https://github.com/twilio/twilio-verify-sna-android/tree/main")
        }
      }
    }
  }
}

configure<ArtifactoryPluginConvention> {
  publish {
    contextUrl = "https://twilio.jfrog.io/artifactory"
    repository {
      repoKey = findProperty("artifactoryRepository").toString()
      username = findProperty("artifactoryUsername").toString()
      password = findProperty("artifactoryPassword").toString()
    }
  }
}

apkscale {
  abis = setOf("x86", "x86_64", "armeabi-v7a", "arm64-v8a")
}

tasks.register("generateSizeReport") {
  dependsOn("assembleRelease", "measureSize")
  description = "Calculate Verify SNA SDK Size Impact"
  group = "Reporting"

  doLast {
    var sizeReport =
      "### Size impact\n" +
        "\n" +
        "| ABI             | APK Size Impact |\n" +
        "| --------------- | --------------- |\n"
    val apkscaleOutputFile = file("$buildDir/apkscale/build/outputs/reports/apkscale.json")
    val jsonSlurper = groovy.json.JsonSlurper()
    val apkscaleOutput = jsonSlurper.parseText(apkscaleOutputFile.readText()) as List<*>
    val releaseOutput = apkscaleOutput[0] as Map<*, *>
    val sizes = releaseOutput["size"] as Map<String, String>
    sizes.forEach { (arch, sizeImpact) ->
      sizeReport += "| ${arch.padEnd(16)}| ${sizeImpact.padEnd(16)}|\n"
    }
    val sizeReportDir = "$buildDir/outputs/sizeReport"
    mkdir(sizeReportDir)
    val targetFile = file("$sizeReportDir/VerifySNASDKSizeReport.txt")
    targetFile.createNewFile()
    targetFile.writeText(sizeReport)
  }
}

dependencies {
  implementation(libs.okhttp)
  implementation(libs.core.ktx)
  testImplementation(libs.junit)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.espresso.core)
  testImplementation(libs.mockk)
  testImplementation(libs.robolectric)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.truth)
}
