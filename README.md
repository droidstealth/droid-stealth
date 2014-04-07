Android Stealth
===============

This application provides a platform for secure hidden storage of user data on an Android phone.
The application is hidden on the phone, only to be found with the correct PIN access code, and data managed by the application is encrypted and hidden from other applications on the phone.
However even if you don't want that data readily available for everyone that gets hands on your phone there are many cases where you still want to share that data.
Which is why it incorporates several sharing features, both for people who have the app and those who don't.
Yet it very much remains tricky thing to balance between the security and sharing aspects of this project.

Furthermore, the application can be shared directly between two Android phones via a WiFi hotspot, BlueTooth, and Android Beam (when available).
The goal is to implement the tools to make offline sharing and distribution an easy thing.
Both of the app and the data stored inside the app.

Project Vision
--------------

The application aims to provide a secure platform for the user to store sensitive data.
The user, and only the user may unlock, view and share the data managed (encrypted and hidden) by the application.

Near-future goals
-----------------

Create a user-friendly data management experience.
That is, provide an intuitive, smooth flow around locking and hiding data from other applications on the phone, or other users of the phone.
 Several core features for the hiding include:

* Allowing users to 'morph' the application by renaming it and choosing a custom icon when sharing it with others.
To deal with the fact that the app will always be in the installed apps list.
* Keep the app out of the running processes list when the user is not using it.
* And many more minor things like not being in the app drawer, not being part of the recent items list, etc.

 In addition to the above:

* A 'safety net' for the user, by providing notifications; for example when files have been left unlocked.
* Direct phone-to-phone sharing of the application itself.
* Direct phone-to-phone sharing of data stored within the application by user request.

Caveats
----------------

Currently the use of this application has several issues that may result in data loss.
This sections aims to list all of them as a warning both for use and as a reminder of what to work on.

* The applications encryption keys are tied to the installation.
* Uninstalling the app deletes the data managed by the app.
* Force-quitting the app may make the the app inaccessible, as well as the encrypted data, as it makes it stop listening for the right phone calls.
* Any crashes of the app may do the same as the above.
* This is unclear but very specific situations where the app is running and the phone really needs memory that it may force-quit the app and cause the above.

Far-future goals
----------------
Connecting the application to other (non-internet) means of communicating data.

Suggestions for this README? Create an issue.
