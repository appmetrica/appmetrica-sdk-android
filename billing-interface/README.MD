# AppMetrica SDK Billing API

**Required module**.
Maven: `io.appmetrica.analytics:analytics-billing-interface:${VERSION}`.

## Builds

### Assemble

`./gradlew :billing-interface:assembleRelease`

### Publish to MavenLocal

`./gradlew :billing-interface:publishReleasePublicationToMavenLocal`

### Tests

`./gradlew :billing-interface:testReleaseUnitTest :billing-interface:generateReleaseJacocoReport`

### Code style

`./gradlew :billing-interface:lint :billing-interface:ktlint :billing-interface:checkstyleRelease`

### Check AAR API

`./gradlew :billing-interface:aarCheck`

### Regenerate AAR API dump

`./gradlew :billing-interface:aarDump`
