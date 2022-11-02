# Twilio Verify SNA

[![ktlint](https://img.shields.io/badge/code%20style-%E2%9D%A4-FF4081.svg)](https://ktlint.github.io/)
[![License](https://img.shields.io/badge/License-Apache%202-blue.svg?logo=law)](https://github.com/twilio/twilio-verify-sna-android/blob/main/LICENSE)

## Table of Contents
- [About](#About)
- [Dependencies](#Dependencies)
- [Requirements](#Requirements)
- [Documentation](#Documentation)
- [Installation](#Installation)
- [Running the Sample App](#SampleApp)
- [Running the Sample Backend](#SampleBackend)
- [Using the Sample App](#UsingSampleApp)
- [Errors](#Errors)
- [Validate SNA URL](#ValidateSNAURL)
- [Contributing](#Contributing)
- [License](#License)

<a name='About'></a>
## About
Twilio Silent Network Auth will protect you against account takeovers (ATOs) that target your user's phone number by streamlining your methods for verifying mobile number possession. Instead of sending one-time passcodes (OTPs) that can be stolen or forcing users to implement complicated app-based solutions, Twilio Silent Network Auth will verify mobile number possession directly on the device by using its built-in connectivity to the mobile operators’ wireless network.

This SDK will help you with process the SNA URL's provided by our `Verify services` to silently validate a phone number.

See <a href="https://www.twilio.com/docs/verify/sna/tech-overview">Technical Overview</a>

See <a href="https://www.twilio.com/docs/verify/sna">Silent Network Auth Overview</a>

<a name='Dependencies'></a>
## Dependencies
None

<a name='Requirements'></a>
## Requirements
* Android Studio Dolphin | 2021.3.1 or higher
* Java 8
* Android SDK 21 or higher
* Gradle 7.2
* Kotlin 1.6

<a name='Documentation'></a>
## Documentation
Offical documentation will be added via Twilio docs once this project gets released.

<a name='Installation'></a>
## Installation
During the current phase of this project, only installation via Twilio Maven is supported. We have plans to support CocoaPods and Carthage once we release this product.

### Gradle
1. Add Twilio Maven Repository in project level Gradle:
```
allprojects {
    // ...
  repositories {
    // ...
    mavenCentral()
    maven {
      url "https://twilio.jfrog.io/artifactory/internal-releases"
      credentials {
        username "YOUR_ARTIFACTORY_USER"
        password "YOUR_ARTIFACTORY_PASSWORD"
      }
    }
  }
}
```

2. Add Verify SNA dependency implementation in module level Gradle:
```
implementation "com.twilio:twilio-verify-sna-android:X.Y.Z"
```

<a name='SampleApp'></a>
## Running the Sample App

<a name='SampleBackend'></a>
## Running the Sample Backend

<a name='UsingSampleApp'></a>
## Using the Sample App

<a name='Errors'></a>
## Errors

<a name='ValidateSNAURL'></a>
## Validate SNA URL

<a name='Contributing'></a>
## Contributing

<a name='License'></a>
## License
[Apache © Twilio Inc.](./LICENSE)
