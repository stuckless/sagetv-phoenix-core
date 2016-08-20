# sagetv-phoenix-core

Phoenix Core Services for SageTV

## Building Phoenix From Source
* Checkout the code
* Run `./gradlew configure`

If using IntelliJ
* Open `build.gradle` as project file

If using Eclipse
* Run `./gradlew cleanEclipse eclipse`
* Import project

## Regenaring Phoenis APIs
Run `./gradew generateApi`

## Packaging
Run `./gradlew clean dist`

## Uploading
Run `./gradlew bintrayUpload`

## JavaDoc
* http://stuckless.github.io/sagetv-phoenix-core/javadoc/
