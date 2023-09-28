# AppMetrica NDK Crashes API

**Required module**.
Maven: `io.appmetrica.analytics:ndkcrashes-api:${VERSION}`.

Minimum supported version of ndkcrashes is 3.0.0.

## Builds

### Assemble

`./gradlew :ndkcrashes-api:assembleRelease`

### Publish to MavenLocal

`./gradlew :ndkcrashes-api:publishReleasePublicationToMavenLocal`

### Tests

`./gradlew :ndkcrashes-api:testReleaseUnitTest :ndkcrashes-api:generateReleaseJacocoReport`

### Code style

`./gradlew :ndkcrashes-api:lint :ndkcrashes-api:ktlint :ndkcrashes-api:checkstyleRelease`

### Check AAR API

`./gradlew :ndkcrashes-api:aarCheck`

### Regenerate AAR API dump

`./gradlew :ndkcrashes-api:aarDump`
