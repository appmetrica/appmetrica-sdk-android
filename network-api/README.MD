# AppMetrica SDK Network API

**Required module**.
Maven: `io.appmetrica.analytics:analytics-network-api:${VERSION}`.

## Builds

### Assemble

`./gradlew :network-api:assembleRelease`

### Publish to MavenLocal

`./gradlew :network-api:publishReleasePublicationToMavenLocal`

### Tests

`./gradlew :network-api:testReleaseUnitTest :network-api:generateReleaseJacocoReport`

### Code style

`./gradlew :network-api:lint :network-api:ktlint :network-api:checkstyleRelease`

### Check AAR API

`./gradlew :network-api:aarCheck`

### Regenerate AAR API dump

`./gradlew :network-api:aarDump`
