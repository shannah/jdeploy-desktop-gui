# Jdeploy Desktop Gui

{{ appDescription }}

[Releases](https://github.com/shannah/jdeploy-desktop-gui/releases)

## About this Project

This project was generated using the Swing project template from [jDeploy](https://www.jdeploy.com), a tool to deploy Java desktop apps on Windows, Mac.

## DMG Releases

By default Mac releases use an installer.  You can optionally enable DMG releases also by setting the variable `JDEPLOY_CREATE_DMG=true` in your repository settings.
You will also need to define the following secrets:

* `MAC_DEVELOPER_ID` : Your email address for your Apple developer account.
* `MAC_DEVELOPER_CERTIFICATE_P12_BASE64` : A base64 encoding of your Mac developer certificate (p12)
*  `MAC_DEVELOPER_CERTIFICATE_PASSWORD` : The password you used for your Mac developer certificate p12, when you exported it from your keychain.
* `MAC_NOTARIZATION_PASSWORD` : The application-specific password that you set for notarization in your Apple developer account.
