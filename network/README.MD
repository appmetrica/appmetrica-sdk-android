# AppMetrica SDK Network

**Required module**.
Maven: `io.appmetrica.analytics:network:${VERSION}`.

## Builds

### Assemble

`./gradlew :network:assembleRelease`

### Publish to MavenLocal

`./gradlew :network:publishReleasePublicationToMavenLocal`

### Tests

`./gradlew :network:testReleaseUnitTest :network:generateReleaseJacocoReport`

### Code style

`./gradlew :network:codequality`

### Check AAR API

`./gradlew :network:aarCheck`

### Regenerate AAR API dump

`./gradlew :network:aarDump`
