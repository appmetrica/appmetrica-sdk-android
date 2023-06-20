# AppMetrica SDK Location API

**Required module**.
Maven: `io.appmetrica.analytics:analytics-location-api:${VERSION}`.

## Builds

### Assemble

`./gradlew :location-api:assembleRelease`

### Publish to MavenLocal

`./gradlew :location-api:publishReleasePublicationToMavenLocal`

### Tests

`./gradlew :location-api:testReleaseUnitTest :location-api:generateReleaseJacocoReport`

### Code style

`./gradlew :location-api:codequality`

### Check AAR API

`./gradlew :location-api:aarCheck`

### Regenerate AAR API dump

`./gradlew :location-api:aarDump`
