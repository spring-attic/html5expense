# HTML5 EXPENSE CLIENT #

Expense reporting reference app demonstrating HTML5 and cross-platform mobile

## Environment Setup ##

### iOS ###

Building iOS projects with PhoneGap requires Apple OS X.

#### Xcode ####

Install Xcode from the [Mac App Store](https://itunes.apple.com/us/app/xcode/id448457090?mt=12).

#### PhoneGap ####

The PhoneGap installer will create a new Xcode project template, which you can use to create new iOS PhoneGap projects.  The HTML5 Expense client was created with this template.

1. Download version 1.3.0 or newer of [PhoneGap](https://www.phonegap.com/).
2. Unzip the PhoneGap package.
3. From the iOS directory, open the PhoneGap-1.3.0.dmg disk image.
4. Double click the PhoneGap-1.3.0.pkg from within the disk image contents to install.

### Android ###

Building Android projects is supported on Windows, OS X, and Linux.

#### Eclipse ####

Download and install [Eclipse](https://www.eclipse.org/downloads/). The [SpringSource Tool Suite](https://www.springsource.com/landing/best-development-tool-enterprise-java) also works quite nicely.

#### Android SDK ####

Download and install the [Android SDK](https://developer.android.com/sdk/index.html).

#### Android Development Tools (ADT) Plugin for Eclipse ####

Install the [ADT Plugin](https://developer.android.com/sdk/eclipse-adt.html#installing) in Eclipse.

#### PhoneGap ####

PhoneGap does not provide an installer for Android projects, however when you create a new project you simply deploy the PhoneGap JAR and JavaScript files into your new project.  The HTML5 Expense Android client project directory already has these items included.

## Project Structure ##

The 'shared/www' directory contains the HTML, CSS, and JavaScript that is being used within the iOS and Android Phonegap projects.  A symbolic link (symlink) is used within each project to point back to this folder.  In the iOS project, the symlink is 'ios/www'.  In the Android project it is located at 'android/assets/www'.  Symlinks are supported in Mac OS X, Linux, Windows Vista, and Windows 7.  This PhoneGap [wiki page](https://github.com/phonegap/phonegap/wiki) describes this solution for shared web resources across device types.

## Open the Client Projects ##

This section describes how to open the iOS and Android projects within Xcode and Eclipse respectively.

### iOS ###

After you have completed the environment setup for Xcode, you can open the iOS client project.

#### Open Project in Xcode ####

1. Open Xcode.
2. Select File -> Open, or click the Open Other button on the welcome screen.
3. In the Open dialog, browse to the html5expense/client/ios directory and click Open.

The project should now build successfully

### Android ###

After you have completed the environment setup for Eclipse, you can open the Android client project.

#### Import Project into Eclipse ####

Follow these steps to import the Android client project into Eclipse.

1. From the File menu, select New -> Project...
2. From the New Project dialog, select Android -> Android Project, and click Next. NOTE: if Android is not available, the ADT Plugin for Eclipse is not installed properly.
3. From the New Android Project dialog, enter html5expense in the Project name field.
4. Within the Contents section, select the Create project from existing source radio button.
5. In the Open dialog, browse to the html5expense/client/android directory and click Open.
6. All the remaining fields in the New Android Project dialog should now be populated.
7. Click Finish.

#### Configure Build Path ####

That completes the steps to import the project.  Note the html5expense project listed in the Eclipse Package Explorer on the left side of the screen.  Also note the small red X over the project.  Html5expense will not build without another small modification.  Follow these steps to complete the project setup in Eclipse.

1. Right click (Command click on OS X) the html5expense project in Eclipse.
2. Navigate to Build Path -> Configure Build Path.
3. Select the Libraries tab.
4. Click Add JARs...
5. In the JAR Selection dialog, navigate to html5expense/libs/phonegap-1.3.0.jar and click OK.  The PhoneGap jar should now be listed in the "JARs and class folders on the build path" section.
6. Click OK.

The project should now build successfully, and the red X should disappear from the project listing.

