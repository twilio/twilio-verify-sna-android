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
  alias(libs.plugins.androidApplication)
  alias(libs.plugins.kotlinAndroid)
  alias(sampleLibs.plugins.navigationSafeArgs)
}

android {
  namespace = "com.twilio.verify.sna.sample"
  compileSdk = 36

  defaultConfig {
    applicationId = "com.twilio.verify.sna.sample"
    minSdk = 23
    targetSdk = 36
    versionCode = VersionHelper.generateVersionCode(project)
    versionName = VersionHelper.generateVersionName(project)

    testInstrumentationRunner= "androidx.test.runner.AndroidJUnitRunner"
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
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }
  buildFeatures {
    viewBinding = true
    buildConfig = true
  }
  kotlinOptions {
    jvmTarget = "17"
  }
}

dependencies {
  implementation(sampleLibs.androidx.core.ktx)
  implementation(sampleLibs.material)
  implementation(sampleLibs.androidx.constraintlayout)
  testImplementation(libs.junit)
  androidTestImplementation(libs.androidx.junit)
  implementation(project(":verify_sna"))

  implementation(sampleLibs.androidx.navigation.fragment.ktx)
  implementation(sampleLibs.androidx.navigation.ui.ktx)

  implementation(sampleLibs.logging.interceptor)
  implementation(sampleLibs.retrofit)
  implementation(sampleLibs.converter.gson)
}
