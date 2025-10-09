# AppMetrica SDK Billing collecting

**Optional module**.
Maven: `io.appmetrica.analytics:analytics-billing:${VERSION}`.

## Builds

### Assemble

`./gradlew :billing:assembleRelease`

### Publish to MavenLocal

`./gradlew :billing:publishReleasePublicationToMavenLocal`

### Tests

`./gradlew :billing:testReleaseUnitTest :billing:generateReleaseJacocoReport`

### Code style

`./gradlew :billing:lint :billing:ktlint :billing:checkstyleRelease`

### Check AAR API

`./gradlew :billing:aarCheck`

### Regenerate AAR API dump

`./gradlew :billing:aarDump`
