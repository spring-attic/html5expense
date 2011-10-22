Hydra
====

![Hydra!](http://github.com/filmaj/hydra/raw/master/img/icon128.png)

A beast with many heads. Keep one or more [PhoneGap](http://www.phonegap.com) app assets "in the cloud" on
[build.phonegap.com](http://build.phonegap.com), and use this shell to
store different apps and keep each app up-to-date with the latest
assets.

Requirements
----

You can build this project in one of two ways.

#### build.phonegap.com ####

Build it on on [build.phonegap.com](http://build.phonegap.com),
however your account must have plugin support enabled. Contact [sales@nitobi.com](mailto:sales@nitobi.com)
if you have questions about this.

#### Manually in PhoneGap ####

Alternatively, you can build this project yourself if you have
[PhoneGap](http://www.phonegap.com) installed locally, but you'll need
to move some files around.

Platforms currently supported: Android

Getting Started
----

#### via build.phonegap.com ####

1. Upload this project as an application to [build.phonegap.com](http://build.phonegap.com).
2. Install the generated application on your phone.

#### via local PhoneGap ####

1. Wrap the `hydra/` directory up in PhoneGap.
2. Take the native PhoneGap plugins located under `hydra/ext` and place
   them in the appropriate spot of your PhoneGap native app (you'll need
to be familiar with installing PhoneGap plugins).

#### then... ####

3. Run Hydra.
4. You will see a form for entering an application ID along with your [build.phonegap.com](http://build.phonegap.com) credentials. Do that!
5. Your app will download and get loaded into the current app.
6. Next time you run Hydra, you can load any app that you had Hydra pull down. Every time you do, Hydra will check whether a newer version exists on [build.phonegap.com](http://build.phonegap.com).

#### iOS ####

1. Include libz.dylib in your project
    - Xcode 4
        1. Select your target
        2. Select _Build Phases_ tab
        3. Expand _Link Binary with Libraries_
        4. Press _+_ at the bottom
        5. Search and add _libz.dylib_ (expand collapsed directories)
        6. (Optional) Move into the "Frameworks" group

2. In PhoneGap.plist, under "Plugins", add these new entries: 
    1. For the key, add "AppLoader", and for the value, add "AppLoader"
    2. For the key, add "com.nitobi.BinaryDownloader", and for the value, add "BinaryDownloader"
    3. For the key, add "com.nitobi.ZipUtil", and for the value, add "ZipUtil"

3. For PhoneGap 1.1, In PhoneGap.plist, under "ExternalHosts", add these new entries:              
     * build.phonegap.com
     * s3.amazonaws.com
     * (any other hosts that your downloaded app needs to connect to - of course you need to know this in advance - or you can use "*" to allow everything)

Why?
----

* Easier to manage application updates; a poor man's TestFlight.
* Theoretically more secure. However: be warned I don't know shit about
  security.

Contributors
----

* [Joe Bowser](http://www.github.com/infil00p) - originally put together
  the PhoneGap Android plugin
* [Fil Maj](http://www.github.com/filmaj)
* [Shazron Abdullah](http://www.github.com/shazron) - iOS
* [Brett Rudd](http://www.github.com/goya) - originally put together the
  PhoneGap BlackBerry plugin
