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

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
  alias(libs.plugins.androidApplication).apply(false)
  alias(libs.plugins.androidLibrary).apply(false)
  alias(libs.plugins.kotlinAndroid).apply(false)
  alias(libs.plugins.ktlint)
  alias(libs.plugins.nexusPublish)
  alias(libs.plugins.kover)
}

buildscript {
  dependencies {
    classpath(libs.apkscale)
  }
}

allprojects {
  repositories {
    mavenCentral()
    google()
  }
}

tasks.register("artifactoryLibraryReleaseUpload", GradleBuild::class) {
  description = "Publish Verify SNA SDK to internal artifactory"
  group = "Publishing"
  tasks = listOf(":verify_sna:assembleRelease", ":verify_sna:artifactoryPublish")
  startParameter.projectProperties.putAll(
    gradle.startParameter.projectProperties + mapOf(
      "artifactory.username" to getProjectProperty("ARTIFACTORY_USER"),
      "artifactory.password" to getProjectProperty("ARTIFACTORY_PASSWORD"),
      "artifactory.repository" to "releases"
    )
  )
}

/*
 * Check for the property in the project followed by the property in the environment
 */
fun getProjectProperty(prop: String): String {
  val value =
    if (project.hasProperty(prop)) project.property(prop) as String? else System.getenv(prop)
  return value ?: ""
}

tasks.register("incrementVersion") {
  description = "Increment version in gradle.properties"
  group = "versioning"

  doLast {
    val verifySnaVersionName: String by project.extra
    val verifySnaVersionCode: String by project.extra
    val versionCode = verifySnaVersionName.toInt().plus(1)
    var versionName = verifySnaVersionCode
    if (project.hasProperty("version_number")) {
      versionName = project.property("version_number") as String
    }
    ant.withGroovyBuilder {
      "propertyfile"("file" to "gradle.properties") {
        "entry"("key" to "verifySnaVersionName", "value" to versionName)
        "entry"("key" to "verifySnaVersionCode", "value" to versionCode)
      }
    }
  }
}

val mavenPublishCredentials = mapOf(
  "signing.keyId" to getProjectProperty("SIGNING_KEY_ID"),
  "signing.password" to getProjectProperty("SIGNING_PASSWORD"),
  "signing.secretKeyRingFile" to getProjectProperty("SIGNING_SECRET_KEY_RING_FILE"),
  "sonatypeUsername" to getProjectProperty("SONATYPE_USERNAME"),
  "sonatypePassword" to getProjectProperty("SONATYPE_PASSWORD"),
  "sonatypeStagingProfileId" to getProjectProperty("SONATYPE_STAGING_PROFILE_ID")
)

nexusPublishing {
  repositories {
    sonatype {
      username = getProjectProperty("SONATYPE_USERNAME")
      password = getProjectProperty("SONATYPE_PASSWORD")
      stagingProfileId.set(getProjectProperty("SONATYPE_STAGING_PROFILE_ID"))
      nexusUrl.set(uri("https://ossrh-staging-api.central.sonatype.com/service/local/"))
      snapshotRepositoryUrl.set(uri("https://central.sonatype.com/repository/maven-snapshots/"))
    }
  }
  clientTimeout.set(java.time.Duration.ofSeconds(300))
  connectTimeout.set(java.time.Duration.ofSeconds(60))
}

tasks.register("sonatypeVerifySnaReleaseUpload", GradleBuild::class) {
  description = "Publish Verify SNA to MavenCentral"
  group = "Publishing"
  buildName = "TwilioVerifySna"
  tasks = listOf(
    ":verify_sna:assembleRelease",
    ":verify_sna:publishVerifySnaAndroidPublicationToSonatypeRepository",
    "closeAndReleaseSonatypeStagingRepository"
  )
  startParameter.projectProperties.plusAssign(
    gradle.startParameter.projectProperties + mavenPublishCredentials
  )
}

tasks.register("sonatypeVerifySnaStagingReleaseUpload", GradleBuild::class) {
  description = "Publish Verify SNA to Nexus staging repository"
  group = "Publishing"
  buildName = "TwilioVerifySna"
  tasks = listOf(
    ":verify_sna:assembleRelease",
    ":verify_sna:publishVerifySnaAndroidPublicationToSonatypeRepository",
    "closeSonatypeStagingRepository"
  )
  startParameter.projectProperties.plusAssign(
    gradle.startParameter.projectProperties + mavenPublishCredentials
  )
}

tasks.register("mavenLocalTwilioVerifyReleaseUpload", GradleBuild::class) {
  description = "Publish Verify SNA to maven local"
  group = "Publishing"
  buildName = "TwilioVerifySna"
  tasks = listOf(
    ":verify_sna:assembleRelease",
    ":verify_sna:publishVerifySnaAndroidPublicationToMavenLocal"
  )
  startParameter.projectProperties.plusAssign(
    gradle.startParameter.projectProperties + mavenPublishCredentials
  )
}
