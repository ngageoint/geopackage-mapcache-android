# GeoPackage MapCache Android

#### MapCache Android ####

The [GeoPackage Libraries](http://ngageoint.github.io/GeoPackage/) were developed at the [National Geospatial-Intelligence Agency (NGA)](http://www.nga.mil/) in collaboration with [BIT Systems](http://www.bit-sys.com/). The government has "unlimited rights" and is releasing this software to increase the impact of government investments by providing developers with the opportunity to take things in new directions. The software use, modification, and distribution rights are stipulated within the [MIT license](http://choosealicense.com/licenses/mit/).

### Pull Requests ###
If you'd like to contribute to this project, please make a pull request. We'll review the pull request and discuss the changes. All pull request contributions to this project will be released under the MIT license.

Software source code previously released under an open source license and then modified by NGA staff is considered a "joint work" (see 17 USC § 101); it is partially copyrighted, partially public domain, and as a whole is protected by the copyrights of the non-government authors and must be released according to the terms of the original open source license.

### Android App ###

<a href='https://play.google.com/store/apps/details?id=mil.nga.mapcache'>
    <img src="https://github.com/ngageoint/GeoPackage/raw/master/docs/images/google-play-badge.png" height=50>
</a>

### About ###

[GeoPackage MapCache](http://ngageoint.github.io/geopackage-mapcache-android/) is a [GeoPackage Library](http://ngageoint.github.io/GeoPackage/) app for Android that utilizes and demonstrates the functionality in [GeoPackage Android](https://github.com/ngageoint/geopackage-android) and [GeoPackage Android Map](https://github.com/ngageoint/geopackage-android-map).  The GeoPackage SDKs are Android library implementations of the Open Geospatial Consortium [GeoPackage](http://www.geopackage.org/) [spec](http://www.geopackage.org/spec/). The app and SDK are listed as [OGC GeoPackage Implementations](http://www.geopackage.org/#implementations_nga) by the National Geospatial-Intelligence Agency.

The MapCache app provides read and write access to GeoPackage files, including features and tiles. Features are visualized and editable on the map. Tiles are visualized on the map and can be loaded from a tile provider or WMS url.

### Screenshots ###

GeoPackage with a tile layer

<img src="screenshots/screen1.png" width="30%">

List of GeoPackages with 2 enabled

<img src="screenshots/screen2.png" width="30%">

A GeoPackage with tiles and features enabled on the map

<img src="screenshots/screen3.png" width="30%">

Downloading tiles for a new layer

<img src="screenshots/screen4.png" width="30%">

A Tile layer detail view

<img src="screenshots/screen6.png" width="30%">

### Build ###

[![Build](https://github.com/ngageoint/geopackage-mapcache-android/workflows/Build/badge.svg)](https://github.com/ngageoint/geopackage-mapcache-android/actions?query=workflow%3ABuild)

Build this repository using Android Studio and/or Gradle. Run the "assembleRelease" task on the geopackage-mapcache-android Gradle script.

#### Map Key ####

When building this project locally, a Google API key is required to see map tiles:
 * Maps SDK for Android: [Get API Key](https://developers.google.com/maps/documentation/android-sdk/signup)

On the [Google Cloud Platform Console](https://cloud.google.com/console/google/maps-apis/overview), configure "Maps SDK for Android" credentials (replacing the example fingerprints).

| Package name           | SHA-1 certificate fingerprint                               |
| ---------------------- | ----------------------------------------------------------- |
| mil.nga.mapcache       | 12:34:56:78:90:AB:CD:EF:12:34:56:78:90:AB:CD:EF:AA:BB:CC:DD |
| mil.nga.mapcache.test  | 12:34:56:78:90:AB:CD:EF:12:34:56:78:90:AB:CD:EF:AA:BB:CC:DD |

In your `gradle.properties`, add your API Key (replacing the example keys)

```
RELEASE_MAPS_MAPCACHE_API_KEY=AIzaSyBdVl-cTICSwYKrZ95SuvNw7dbMuDt1KG0
DEBUG_MAPS_API_KEY=AIzaSyBdVl-cTICSwYKrZ95SuvNw7dbMuDt1KG0
```

### Legacy ###

The original MapCache app is minimally maintained on the [legacy branch](https://github.com/ngageoint/geopackage-mapcache-android/tree/legacy).

[Download the latest Legacy APK](https://ngageoint.github.io/geopackage-mapcache-android/legacy/mapcache.zip)
