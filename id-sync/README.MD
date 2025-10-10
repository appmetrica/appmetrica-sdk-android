# AppMetrica SDK Identifiers Synchronization

**Optional module**.
Maven: `io.appmetrica.analytics:analytics-id-sync:${VERSION}`.

## Builds

### Assemble

`./gradlew :id-sync:assembleRelease`

### Publish to MavenLocal

`./gradlew :id-sync:publishReleasePublicationToMavenLocal`

### Tests

`./gradlew :id-sync:testReleaseUnitTest :id-sync:generateReleaseJacocoReport`

### Code style

`./gradlew :id-sync:lint :id-sync:ktlint :id-sync:checkstyleRelease`

### Check AAR API

`./gradlew :id-sync:aarCheck`

### Regenerate AAR API dump

`./gradlew :id-sync:aarDump`
