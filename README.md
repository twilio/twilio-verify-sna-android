# Twilio Verify SNA

[![ktlint](https://img.shields.io/badge/code%20style-%E2%9D%A4-FF4081.svg)](https://ktlint.github.io/)
[![License](https://img.shields.io/badge/License-Apache%202-blue.svg?logo=law)](https://github.com/twilio/twilio-verify-android/blob/main/LICENSE)

## Project Structure
The project is structured as follows:
```
/root
├─ sample
└─ verify_sna
```
- `sample`: Contains an app to demo the SNA SDK working.
- `verify_sna`: Contains the Twilio Verify SNA SDK core.

## Generate a version of Verify SNA SDK for internal testing
- To generate a new version of the SDK use the Gradle task `artifactoryLibraryReleaseUpload`, it will generate a build using the version values `versionMajor`, `versionMinor` and `versionPatch`. Create those values in the Gradle local properties file:
```
versionMajor=
versionMinor=
versionPatch=
```
- Run the task:
```
./gradlew artifactoryLibraryReleaseUpload 
```

## Import the Verify SNA SDK using Gradle
- To import the Verify SNA SDK in an existing app, Maven Central repository is required. Add the following to the project level `build.gradle` file:
```
allprojects {
    // ...
  repositories {
    // ...
    mavenCentral()
    maven {
      url "https://twilio.jfrog.io/artifactory/internal-releases"
      credentials {
        username <your_artifactory_user>
        password <your_password>
      }
    }
  }
}
```
- Add the dependency to module level `build.gradle` file:
```
implementation "com.twilio:twilio-verify-sna-android:<version>"
```
