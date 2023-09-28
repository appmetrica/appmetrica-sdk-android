# AppMetrica SDK Remote Permissions Functionality

**Optional module**.
Maven: `io.appmetrica.analytics:analytics-remote-permissions:${VERSION}`.

## Builds

### Assemble

`./gradlew :remote-permissions:assembleRelease`

### Publish to MavenLocal

`./gradlew :remote-permissions:publishReleasePublicationToMavenLocal`

### Tests

`./gradlew :remote-permissions:testReleaseUnitTest :remote-permissions:generateReleaseJacocoReport`

### Code style

`./gradlew :remote-permissions:lint :remote-permissions:ktlint :remote-permissions:checkstyleRelease`

### Check AAR API

`./gradlew :remote-permissions:aarCheck`

### Regenerate AAR API dump

`./gradlew :remote-permissions:aarDump`
