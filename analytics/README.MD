# AppMetrica Analytics module

**Required module**.
Maven: `io.appmetrica.analytics:analytics:${VERSION}`.

## Builds

### Assemble

`./gradlew :analytics:assembleRelease`

### Publish to MavenLocal

`./gradlew :analytics:publishReleasePublicationToMavenLocal`

### Tests

`./gradlew :analytics:testReleaseUnitTest :analytics:generateReleaseJacocoReport`

### Code style

`./gradlew :analytics:lint :analytics:ktlint :analytics:checkstyleRelease`

### Check AAR API

`./gradlew :analytics:aarCheck`

### Regenerate AAR API dump

`./gradlew :analytics:aarDump`
