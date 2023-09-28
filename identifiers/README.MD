# AppMetrica SDK Identifiers

**Optional module**.
Maven: `io.appmetrica.analytics:analytics-identifiers:${VERSION}`.

## Builds

### Assemble

`./gradlew :identifiers:assembleRelease`

### Publish to MavenLocal

`./gradlew :identifiers:publishReleasePublicationToMavenLocal`

### Tests

`./gradlew :identifiers:testReleaseUnitTest :identifiers:generateReleaseJacocoReport`

### Code style

`./gradlew :identifiers:lint :identifiers:ktlint :identifiers:checkstyleRelease`

### Check AAR API

`./gradlew :identifiers:aarCheck`

### Regenerate AAR API dump

`./gradlew :identifiers:aarDump`
