# AppMetrica SDK Google Play Services Billing V6 Library Wrapper

**Required module**.
Maven: `io.appmetrica.analytics:analytics-billing-v6:${VERSION}`.

## Builds

### Assemble

`./gradlew :billing-v6:assembleRelease`

### Publish to MavenLocal

`./gradlew :billing-v6:publishReleasePublicationToMavenLocal`

### Tests

`./gradlew :billing-v6:testReleaseUnitTest :billing-v6:generateReleaseJacocoReport`

### Code style

`./gradlew :billing-v6:lint :billing-v6:ktlint :billing-v6:checkstyleRelease`

### Check AAR API

`./gradlew :billing-v6:aarCheck`

### Regenerate AAR API dump

`./gradlew :billing-v6:aarDump`
