# LoginModes

This app is a prototype for G+, Facebook sign in options.

Generate key:
Windows: 
keytool -list -v -keystore "%USERPROFILE%\.android\debug.keystore" -alias androiddebugkey -storepass android -keypass android

Linux or Mac:
keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android

Get configuration file from:
https://developers.google.com/identity/sign-in/android/start-integrating

Facebook link for configurations:
https://developers.facebook.com/docs/facebook-login/android/

Profile tracker options:
https://developers.facebook.com/docs/facebook-login/android/accesstokens

Good to go!
