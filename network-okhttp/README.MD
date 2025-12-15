# AppMetrica SDK Network OkHttp

**Required module**.
Maven: `io.appmetrica.analytics:analytics-network-okhttp:${VERSION}`.

## Builds

### Assemble

`./gradlew :network-okhttp:assembleRelease`

### Publish to MavenLocal

`./gradlew :network-okhttp:publishReleasePublicationToMavenLocal`

### Tests

`./gradlew :network-okhttp:testReleaseUnitTest :network-okhttp:generateReleaseJacocoReport`

### Code style

`./gradlew :network-okhttp:lint :network-okhttp:ktlint :network-okhttp:checkstyleRelease`

### Check AAR API

`./gradlew :network-okhttp:aarCheck`

### Regenerate AAR API dump

`./gradlew :network-okhttp:aarDump`
