# AppMetrica SDK Screenshot Listener Functionality

**Optional module**.
Maven: `io.appmetrica.analytics:analytics-screenshot:${VERSION}`.

## Builds

### Assemble

`./gradlew :screenshot:assembleRelease`

### Publish to MavenLocal

`./gradlew :screenshot:publishReleasePublicationToMavenLocal`

### Tests

`./gradlew :screenshot:testReleaseUnitTest :screenshot:generateReleaseJacocoReport`

### Code style

`./gradlew :screenshot:lint :screenshot:ktlint :screenshot:checkstyleRelease`

### Check AAR API

`./gradlew :screenshot:aarCheck`

### Regenerate AAR API dump

`./gradlew :screenshot:aarDump`
