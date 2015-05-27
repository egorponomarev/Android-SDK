
To make use of the Feed.fm player library in Eclipse, do the following:

1. Download a copy of the library at:

https://developer.feed.fm/FeedFM-Android-SDK-eclipse.zip

unzip that folder and you will have a directory named 'FeedFM-Android-SDK'.

2. From your existing project in Eclipse, select 'File -> Import', then
select 'Existing Android Code Into Workspace' from the 'Android' folder.
Select the 'FeedFM-Android-SDK' directory you unzipped in step one. Select
'finish'. The Feed library should now be in your Eclipse workspace.

3. Now, you need to configure the project so it depends on your 'appcompat_v7'
and have the SDK export its own libraries. Select the 'FeedFM-Android-SDK'
from the 'Package Explorer', then right click and select
'Build Path -> Configure Build Path'.

Under the 'Projects' tab, click 'Add...' and add 'appcompat_v7' to the
project.

Under the 'Order and Export' tab, make sure the following are __unchecked__:
'Android Dependencies', 'appcompat_v7'. Make sure that the 'Android Private Libraries' is __checked__.

Click 'OK' to save your changes.

4. Now you need to make the SDK available to your project. Click on your
project in the 'Package Explorer', then right click and select
'Properties'.

In the configuration dialog, select 'Java Build Path' on the left, then
the 'Projects' tab on the right. Select 'Add...' and then select the
checkbox next to 'FeedFM-Android-SDK' and click 'OK'.

Now, back in the configuration dialog, select 'Android' on the left. Under the
'Library' section, select 'Add...', then 'FeedFM-Android-SDK', then 'OK'. 

Select 'OK' to make sure all your changes are saved.

5. Your application needs to declare a service that the SDK will start, and
you need to make sure some permissions are granted.

Open the AndroidManifest.xml for your app.

In the 'manifest', add the following permissions:

  <uses-permission android:name="android.permission.INTERNET"/>
  <uses-permission android:name="android.permission.WAKE_LOCK"/>
  <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>

Within the 'application' declaration, add the following service:

<service android:name="fm.feed.android.playersdk.service.PlayerService"
        android:label="Feed FM Player Service"/>

6. You show now be ready to use the player from your app! Refer to the
introduction here:

https://github.com/feedfm/Android-SDK/blob/master/README.md

to build a minimal app that plays music.


