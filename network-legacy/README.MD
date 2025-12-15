# AppMetrica SDK Network Legacy

**Required module**.
Maven: `io.appmetrica.analytics:analytics-network-legacy:${VERSION}`.

## Builds

### Assemble

`./gradlew :network-legacy:assembleRelease`

### Publish to MavenLocal

`./gradlew :network-legacy:publishReleasePublicationToMavenLocal`

### Tests

`./gradlew :network-legacy:testReleaseUnitTest :network-legacy:generateReleaseJacocoReport`

### Code style

`./gradlew :network-legacy:lint :network-legacy:ktlint :network-legacy:checkstyleRelease`

### Check AAR API

`./gradlew :network-legacy:aarCheck`

### Regenerate AAR API dump

`./gradlew :network-legacy:aarDump`
