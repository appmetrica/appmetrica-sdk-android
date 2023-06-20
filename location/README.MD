# AppMetrica SDK Location API Implementation

**Optional module**.
Maven: `io.appmetrica.analytics:analytics-location:${VERSION}`.

## Builds

### Assemble

`./gradlew :location:assembleRelease`

### Publish to MavenLocal

`./gradlew :location:publishReleasePublicationToMavenLocal`

### Tests

`./gradlew :location:testReleaseUnitTest :location:generateReleaseJacocoReport`

### Code style

`./gradlew :location:codequality`

### Check AAR API

`./gradlew :location:aarCheck`

### Regenerate AAR API dump

`./gradlew :location:aarDump`
