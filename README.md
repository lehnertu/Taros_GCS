# Taros_GCS
Ground Control Station for the TAROS project

### How to get the USB serial library to work
- clone project from github https://github.com/mik3y/usb-serial-for-android.git
- copy the library project folder (usb-serial-for-android/usbSerialForAndroid) into my project
- include the library project in settings.cradle
    include ':app', ':usbSerialForAndroid'
- add dependency in app/build.cradle
    implementation project(':usbSerialForAndroid')
- edit src/main/manifest.xml to autp-detect serial devices
            <intent-filter>
                <action android:name="android.hardware.usb.action.USB_DEVICE_ATTACHED" />
            </intent-filter>
            <meta-data
                android:name="android.hardware.usb.action.USB_DEVICE_ATTACHED"
                android:resource="@xml/device_filter" />
- copy src/main/res/xml/device_filter.xml

