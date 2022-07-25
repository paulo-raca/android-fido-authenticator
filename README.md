Android FIDO Authenticator
===========================

FIDO2 authenticators are still a novelty: They aren't all that cheap, can only store a very small number of persisted keys (Necessary for single-factor), have minimalistic user interfaces and most of the time aren't hardened by biometrics/pins.

Android has all the hardware goodies to be an excellent FIDO2 authenticator.

This project aims to implement CTAP2 over NFC, BLE and Bluetooth-HID.

Project Status
--------------

### Protocols
- [x] U2F
- [ ] FIDO 2

### Transports
- [x] NFC: Working
- [ ] BLE: Android [actively prevents the implementation of a FIDO over BLE for non-system apps](https://android.googlesource.com/platform/packages/apps/Bluetooth/+/6f7f9bbf46acaaf266537256da4d0345909ea1c4/src/com/android/bluetooth/gatt/GattService.java#3217) 😫
- [ ] HID: It is possible to implement a [HID transport over Bluetooth](https://developer.android.com/reference/android/bluetooth/BluetoothHidDevice).

### User interface
- [ ] Show details of the site/user being enrolled / verified
- [ ] Perform user confirmation before enrollment / verification

References
----------
- [Client to Authenticator Protocol (CTAP)](https://fidoalliance.org/specs/fido-v2.1-ps-20210615/fido-client-to-authenticator-protocol-v2.1-ps-20210615.html)
- [FIDO U2F Raw Message Formats](https://fidoalliance.org/specs/fido-u2f-v1.2-ps-20170411/fido-u2f-raw-message-formats-v1.2-ps-20170411.html)
- [Awesome WebAuthn and Passkey](https://github.com/herrjemand/awesome-webauthn): Links to interesting FIDO-related resources
- [WearAuthn](https://github.com/fmeum/WearAuthn): Nice Authenticator for WearOS, and very similar to this project!
- [WebAuthn4J](https://github.com/webauthn4j/webauthn4j): The project focuses on the server side, but implements a complete U2F Authenticator as part of the test suite. 
