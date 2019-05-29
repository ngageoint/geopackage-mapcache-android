# Change Log
All notable changes to this project will be documented in this file.
Adheres to [Semantic Versioning](http://semver.org/).

---

## 1.24 (TBD)

* geopackage-android-map version 3.3.0
* Rename and Copy table options
* gradle plugin updated to 3.4.0
* gradle version 5.1.1

## [1.23](https://github.com/ngageoint/geopackage-mapcache-android/releases/tag/1.23) (04-02-2019)

* geopackage-android-map version 3.2.0
* Feature Style support
* Hex Color support
* Upgrade to AndroidX support libraries
* gradle plugin updated to 3.3.2
* gradle version 4.10.1

## [1.22](https://github.com/ngageoint/geopackage-mapcache-android/releases/tag/1.22) (10-04-2018)

* geopackage-android-map version 3.1.0
* min SDK version updated to 16
* compile SDK version 28
* appcompat v7:28.0.0
* Android 9 "Pie" compatibility changes
* File Utils fix for raw file path download document URIs
* Feature Index Manager connection closures
* GeoPackage Cache utilization
* gradle plugin updated to 3.2.0
* gradle version 4.6

## [1.21](https://github.com/ngageoint/geopackage-mapcache-android/releases/tag/1.21) (07-16-2018)

* geopackage-android-map version 3.0.1
* gradle plugin updated to 3.1.3
* android maven gradle plugin updated to 2.1
* google repository update
* compile SDK version 27
* appcompat v7:27.1.1
* multidex version 1.0.3
* gradle version 4.4

## [1.20](https://github.com/ngageoint/geopackage-mapcache-android/releases/tag/1.20) (05-17-2018)

* geopackage-android-map version updated to 3.0.0
* Feature Overlays turn on a single composite overlay with linked tiles and features

## [1.19](https://github.com/ngageoint/geopackage-mapcache-android/releases/tag/1.19) (03-20-2018)

* Tile Scaling support for displaying missing tiles using nearby zoom levels
* Zoom to tiles using the intersection between the Contents and Tile Matrix Set bounds
* geopackage-android-map version updated to 2.0.2

## [1.18](https://github.com/ngageoint/geopackage-mapcache-android/releases/tag/1.18) (02-14-2018)

* geopackage-android-map version updated to 2.0.1
* Open GeoPackages in read mode when not performing write operations
* Expand contents bounding box when creating and editing features
* Unsupported sqlite function, module, and trigger handling
* Turn off Android auto backup

## [1.17](https://github.com/ngageoint/geopackage-mapcache-android/releases/tag/1.17) (11-21-2017)

* geopackage-android-map version updated to 2.0.0
* GeoPackage share fix for external GeoPackages
* Geometry simplifications for displayed map features based upon zoom level
* Only display and maintain features in the current map views
* Maintain active feature indices when editing map features
* Queryable map features (previously only available for feature tiles)
* Automatically select active feature table when editing features
* Update geometry envelopes when editing features
* Increase default max map features & max points per tile to 5000, max features per tile to 2000
* Updated preloaded GeoPackage url example files
* Updated various now deprecated Android library calls
* gradle plugin updated to 2.3.3
* android maven gradle plugin updated to 2.0
* maven google dependency
* compile SDK version 26
* build tools version updated to 26.0.1
* target SDK version updated to 26
* appcompat v7:26.0.2
* multidex version 1.0.2

## [1.16](https://github.com/ngageoint/geopackage-mapcache-android/releases/tag/1.16) (07-13-2017)

* geopackage-android-map version updated to 1.4.1
* Manifest MultiDex fix for pre Lollipop Android versions
* Improved handling of unknown Contents bounding boxes
* Open GeoPackage from URI creates missing names from last path section
* Prevent app crash from invalid or unsupported geometries
* Bounding of degree projected boxes before Web Mercator transformations

## [1.15](https://github.com/ngageoint/geopackage-mapcache-android/releases/tag/1.15) (06-27-2017)

* geopackage-android-map version updated to 1.4.0

## [1.14](https://github.com/ngageoint/geopackage-mapcache-android/releases/tag/1.14) (06-13-2017)

* geopackage-android-map version updated to 1.3.2
* min SDK lowered from 17 to 14
* build tools version updated to 25.0.3
* gradle plugin updated to 2.3.2
* GeoPackage sample data and tile server updates
* Zoom fix for GeoPackage bounds larger than native map bounds

## [1.13](https://github.com/ngageoint/geopackage-mapcache-android/releases/tag/1.13) (02-02-2017)

* Updated Android, Gradle, & Maven build & SDK libraries
* geopackage-android dependency replaced with geopackage-android-map, version 1.3.1
* getMap call replaced with getMapAsync
* MapFeatureTiles creations changed to DefaultFeatureTiles (fixes Geometries drawn over the International Date Line)

## [1.12](https://github.com/ngageoint/geopackage-mapcache-android/releases/tag/1.12) (06-23-2016)

* geopackage-android version updated to 1.3.0
* EPSG field and default settings for loading tiles from a URL
* Preloaded tile URL updates

## [1.11](https://github.com/ngageoint/geopackage-mapcache-android/releases/tag/1.11) (05-10-2016)

* geopackage-android version updated to 1.2.9
* Data Column names in place of the column name when available
* Natural Earth Rivers GeoPackage URL

## [1.10](https://github.com/ngageoint/geopackage-mapcache-android/releases/tag/1.10) (04-19-2016)

* geopackage-android version updated to 1.2.8

## [1.9](https://github.com/ngageoint/geopackage-mapcache-android/releases/tag/1.9) (04-18-2016)

* geopackage-android version updated to 1.2.7
* Gradle and tools library upgrades

## [1.8](https://github.com/ngageoint/geopackage-mapcache-android/releases/tag/1.8) (02-19-2016)

* Table linking improvements when displaying a tile table linked to a feature table
* Ignore drawing Feature Overlay tiles that exist in linked tile tables
* geopackage-android version updated to 1.2.6

## [1.7](https://github.com/ngageoint/geopackage-mapcache-android/releases/tag/1.7) (02-02-2016)

* geopackage-android version updated to 1.2.5
* Table linking between feature and tile tables

## [1.6](https://github.com/ngageoint/geopackage-mapcache-android/releases/tag/1.6) (01-25-2016)

* Android Marshmallow support (SDK version 23)
* Request user location and external storage permissions
* geopackage-android version updated to 1.2.4

## [1.5](https://github.com/ngageoint/geopackage-mapcache-android/releases/tag/1.5) (01-15-2016)

* geopackage-android version updated to 1.2.3

## [1.4](https://github.com/ngageoint/geopackage-mapcache-android/releases/tag/1.4) (12-14-2015)

* geopackage-android version updated to 1.2.1

## [1.3](https://github.com/ngageoint/geopackage-mapcache-android/releases/tag/1.3) (11-24-2015)

* geopackage-android version updated to 1.2.0
* FeatureTiles (now abstract) creations changed to MapFeatureTiles

## [1.2](https://github.com/ngageoint/geopackage-mapcache-android/releases/tag/1.2) (11-20-2015)

* geopackage-android version updated to 1.1.1
* Open .gpkg and .gpkx file extensions from file system and other apps - [Issue #9](https://github.com/ngageoint/geopackage-mapcache-android/issues/9)
* Recover from corrupt database files

## [1.1](https://github.com/ngageoint/geopackage-mapcache-android/releases/tag/1.1) (10-08-2015)

* Add NGA Table Index Extension support and combine with existing metadata indexing, index to either or both
* Max features per tile support for feature overlays and feature tile generation
* Feature Overlay Query support to provide map click feedback when clicking on feature tile geometries

## [1.0](https://github.com/ngageoint/geopackage-mapcache-android/releases/tag/1.0) (09-15-2015)

* Initial Release
