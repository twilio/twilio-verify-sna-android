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
  alias(libs.plugins.kover)
}

android {
  namespace = "com.twilio.verify_sna"
  compileSdk = 36

  defaultConfig {
    minSdk = 21
    targetSdk = 36

    testInstrumentationRunner= "androidx.test.runner.AndroidJUnitRunner"
    consumerProguardFiles("consumer-rules.pro")
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

val javadoc by tasks.registering(Javadoc::class) {
  // Exclude files that are in the internal package
  exclude("**/internal/**")
  source(android.sourceSets.getByName("main").java.srcDirs)
  classpath += project.files(android.bootClasspath.joinToString(File.pathSeparator))
  isFailOnError = false
}

val javadocJar by tasks.registering(Jar::class) {
  dependsOn(javadoc)
  archiveClassifier.set("javadoc")
  from(javadoc.get().destinationDir)
}

publishing {
  publications {
    create<MavenPublication>("verifySnaAndroid") {
      groupId = "com.twilio"
      artifactId = "twilio-verify-sna-android"
      version = VersionHelper.generateVersionName(project)
      artifact("${layout.buildDirectory.get()}/outputs/aar/${project.name}-release.aar")
      artifact(javadocJar)
      artifact(sourcesJar)
      pom.withXml {
        val dependenciesNode = asNode().appendNode("dependencies")
        configurations.implementation.get().allDependencies.forEach {
          val dependencyNode = dependenciesNode.appendNode("dependency")
          dependencyNode.appendNode("groupId", it.group)
          dependencyNode.appendNode("artifactId", it.name)
          dependencyNode.appendNode("version", it.version)
          dependencyNode.appendNode("scope", "compile")
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

task("generateSizeReport") {
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
    val targetFile = file("$sizeReportDir/AndroidSDKSizeReport.txt")
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
