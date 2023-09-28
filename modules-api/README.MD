# AppMetrica SDK Modules API

**Required module**.
Maven: `io.appmetrica.analytics:analytics-modules-api:${VERSION}`.

## Builds

### Assemble

`./gradlew :modules-api:assembleRelease`

### Publish to MavenLocal

`./gradlew :modules-api:publishReleasePublicationToMavenLocal`

### Tests

`./gradlew :modules-api:testReleaseUnitTest :modules-api:generateReleaseJacocoReport`

### Code style

`./gradlew :modules-api:lint :modules-api:ktlint :modules-api:checkstyleRelease`

### Check AAR API

`./gradlew :modules-api:aarCheck`

### Regenerate AAR API dump

`./gradlew :modules-api:aarDump`
