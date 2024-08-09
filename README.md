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
**Important note:**

We are working on having the Twilio Verify SNA SDK artifact available in a repository manager soon.

Currently, it's possible to include the aar of the SDK including it as external package following the next steps:
1. Clone the repo and open it in Android Studio.
2. Generate the SDK aar file by building the project. The built file is located in:
```
/verify_sna/build/outputs/aar/
```
3. Put the aar file in `libs` folder of your app project (create the folder if it doesn't exist).
4. On your app, in the project-level `build.gradle` file:
```
allprojects {
    // ...
  repositories {
    // ...
    flatDir {
        dirs 'libs'
    }
  }
}
```
5. In the app-level `build.gradle` file add:
```
dependencies {
    // ...
    implementation (name:'aar-name', ext:'aar')
}
```

<a name='SampleApp'></a>
## Running the Sample App
For running the demo you will need to create a sample backend, this backend will communicate with the `Twilio Verify Services` and provide to the client a `SNA URL` that this SDK will use to validate across the carrier cellular network.

**Important note:**

The demo app needs to run on a real device with a working SIM-CARD and internet connection (provided by the sim-card, not Wi-Fi).

Currently it's not possible to test the functionality using a virtual device.

**To run the sample app:**

1. Clone the repo.
2. Open it in Android Studio.
3. Select `sample` in the run/debug configuration selector.
4. Connect or prepare your test device with developer options enabled.
5. Click Run button.

<a name='SampleBackend'></a>
## Running the Sample Backend

* Configure a [Verify Service](https://console.twilio.com/us1/develop/verify/services).
* Go to: [Verify SNA Sample Backend](https://www.twilio.com/code-exchange/verify-sna) 
* Use the `Quick Deploy to Twilio` option
  * You should log in to your Twilio account.
  * Enter the `Account Sid`, `Auth Token`, `Verify Service Sid`, `Sync Service Sid` and `Sync Map Sid` you created above.
    * Create `Sync Map Sid` by clicking on the Service > Maps tab and click the `Create new Sync Map` button in the top right. Once created, copy the Sid.
  * Deploy the application.
  * Press `Go to live application`.
  * You will see the start page. You can check for SNA transactions there, using the `Fetch transactions` button.
  * Copy the url and remove `index.html`, e.g. `verify-sna-xxxx.twil.io`. This will be the `sample backend URL` to use in the sample app.

<a name='UsingSampleApp'></a>
## Using the Sample App
**To validate a phone number:**

- Set the phone number.
   - Available carriers during this phase:
      - US: Verizon, TMO.
      - UK: EE, Vodafone, O2 and ThreeUK.
      - CA: Bell, Rogers and Telus.
- Set the country code (only US during pilot stage).
- Set your [sample backend URL](#running-the-sample-backend).
- Submit the form by using the CTA button.

**Expected behavior:**
The app will ask the network carrier if the provided phone number is the same used on the network request, if the phone number is correct, then the app will redirect to a success screen.

<a name='Errors'></a>
## Errors
| Name                        | Description                                          | Technical cause                                                                                                                  |
|-----------------------------|------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------|
| InvalidUrlException         | Invalid url, please check the format.                | Unable to convert the url string to an URL struct.                                                                               |
| CellularNetworkNotAvailable | Cellular network not available                       | Cellular network not available, check if the device has cellular internet connection or you are not using a simulator or tablet. |
| NoResultFromUrl             | Unable to get a valid result from the requested URL. | Unable to get a redirection path or a result path from the url, probably the SNA URL is corrupted (or maybe expired)             |
| NetworkRequestException     | Error processing the network request.                | An error was thrown in the network layer, check the inner exception property for more details.                                   |
| RunInMainThreadException    | The SDK detected it was running in the main thread.  | The SDK is performing long-time tasks that can freeze or stop the app, it's required to run the SDK in a background thread.      |
| UnexpectedException         | An unknown error was thrown.                         | Check the inner exception for more details.                                                                                      |

<a name='Contributing'></a>
## Contributing
This project welcomes contributions. Please check out our [Contributing guide](./CONTRIBUTING.md) to learn more on how to get started.

<a name='License'></a>
## License
[Apache © Twilio Inc.](./LICENSE)
