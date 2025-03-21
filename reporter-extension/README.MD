# AppMetrica SDK Reporter Extension Module

**Optional module**.
Maven: `io.appmetrica.analytics:analytics-reporter-extension:${VERSION}`.

## Builds

### Assemble

`./gradlew :reporter-extension:assembleRelease`

### Publish to MavenLocal

`./gradlew :reporter-extension:publishReleasePublicationToMavenLocal`

### Tests

`./gradlew :reporter-extension:testReleaseUnitTest :reporter-extension:generateReleaseJacocoReport`

### Code style

`./gradlew :reporter-extension:lint :reporter-extension:ktlint :reporter-extension:checkstyleRelease`

### Check AAR API

`./gradlew :reporter-extension:aarCheck`

### Regenerate AAR API dump

`./gradlew :reporter-extension:aarDump`
