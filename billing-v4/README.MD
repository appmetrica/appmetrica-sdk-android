# AppMetrica SDK Google Play Services Billing V4 Library Wrapper

**Required module**.
Maven: `io.appmetrica.analytics:aanalytics-billing-v4:${VERSION}`.

## Builds

### Assemble

`./gradlew :billing-v4:assembleRelease`

### Publish to MavenLocal

`./gradlew :billing-v4:publishReleasePublicationToMavenLocal`

### Tests

`./gradlew :billing-v4:testReleaseUnitTest :billing-v4:generateReleaseJacocoReport`

### Code style

`./gradlew :billing-v4:lint :billing-v4:ktlint :billing-v4:checkstyleRelease`

### Check AAR API

`./gradlew :billing-v4:aarCheck`

### Regenerate AAR API dump

`./gradlew :billing-v4:aarDump`
