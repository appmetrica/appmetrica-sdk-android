# AppMetrica SDK Google Play Services Billing V8 Library Wrapper

**Required module**.
Maven: `io.appmetrica.analytics:analytics-billing-v8:${VERSION}`.

## Builds

### Assemble

`./gradlew :billing-v8:assembleRelease`

### Publish to MavenLocal

`./gradlew :billing-v8:publishReleasePublicationToMavenLocal`

### Tests

`./gradlew :billing-v8:testReleaseUnitTest :billing-v8:generateReleaseJacocoReport`

### Code style

`./gradlew :billing-v8:lint :billing-v8:ktlint :billing-v8:checkstyleRelease`

### Check AAR API

`./gradlew :billing-v8:aarCheck`

### Regenerate AAR API dump

`./gradlew :billing-v8:aarDump`
