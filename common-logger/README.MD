# AppMetrica SDK Common Logger Module

**Required module**.
Maven: `io.appmetrica.analytics:analytics-common-logger:${VERSION}`.

## Builds

### Assemble

`./gradlew :common-logger:assembleRelease`

### Publish to MavenLocal

`./gradlew :common-logger:publishReleasePublicationToMavenLocal`

### Tests

`./gradlew :common-logger:testReleaseUnitTest :common-logger:generateReleaseJacocoReport`

### Code style

`./gradlew :common-logger:lint :common-logger:ktlint :common-logger:checkstyleRelease`

### Check AAR API

`./gradlew :common-logger:aarCheck`

### Regenerate AAR API dump

`./gradlew :common-logger:aarDump`
