# AppMetrica SDK Logger Module

**Required module**.
Maven: `io.appmetrica.analytics:analytics-logger:${VERSION}`.

## Builds

### Assemble

`./gradlew :logger:assembleRelease`

### Publish to MavenLocal

`./gradlew :logger:publishReleasePublicationToMavenLocal`

### Tests

`./gradlew :logger:testReleaseUnitTest :logger:generateReleaseJacocoReport`

### Code style

`./gradlew :logger:lint :logger:ktlint :logger:checkstyleRelease`

### Check AAR API

`./gradlew :logger:aarCheck`

### Regenerate AAR API dump

`./gradlew :logger:aarDump`
