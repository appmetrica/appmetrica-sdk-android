# AppMetrica SDK Ad Revenue Auto Collection

**Optional module**.
Maven: `io.appmetrica.analytics:analytics-ad-revenue:${VERSION}`.

## Builds

### Assemble

`./gradlew :ad-revenue:assembleRelease`

### Publish to MavenLocal

`./gradlew :ad-revenue:publishReleasePublicationToMavenLocal`

### Tests

`./gradlew :ad-revenue:testReleaseUnitTest :ad-revenue:generateReleaseJacocoReport`

### Code style

`./gradlew :ad-revenue:lint :ad-revenue:ktlint :ad-revenue:checkstyleRelease`

### Check AAR API

`./gradlew :ad-revenue:aarCheck`

### Regenerate AAR API dump

`./gradlew :ad-revenue:aarDump`
