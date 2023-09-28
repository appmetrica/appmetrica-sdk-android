# AppMetrica SDK Core API module

**Required module**.
Maven: `io.appmetrica.analytics:analytics-core-api:${VERSION}`.

## Builds

### Assemble

`./gradlew :core-api:assembleRelease`

### Publish to MavenLocal

`./gradlew :core-api:publishReleasePublicationToMavenLocal`

### Tests

`./gradlew :core-api:testReleaseUnitTest :core-api:generateReleaseJacocoReport`

### Code style

`./gradlew :core-api:lint :core-api:ktlint :core-api:checkstyleRelease`

### Check AAR API

`./gradlew :core-api:aarCheck`

### Regenerate AAR API dump

`./gradlew :core-api:aarDump`
