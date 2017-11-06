# LoginModes

This app is a prototype for G+ sign in option.

Generate key:
Windows: 
keytool -list -v -keystore "%USERPROFILE%\.android\debug.keystore" -alias androiddebugkey -storepass android -keypass android

Linux or Mac:
keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android

Get configuration file from:
https://developers.google.com/identity/sign-in/android/start-integrating

Good to go!
