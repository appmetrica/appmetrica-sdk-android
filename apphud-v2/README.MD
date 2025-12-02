# AppMetrica SDK Apphud v2 integration module

**Optional module**.
Maven: `io.appmetrica.analytics:analytics-apphud-v2:${VERSION}`.

## Builds

### Assemble

`./gradlew :apphud-v2:assembleRelease`

### Publish to MavenLocal

`./gradlew :apphud-v2:publishReleasePublicationToMavenLocal`

### Tests

`./gradlew :apphud-v2:testReleaseUnitTest :apphud-v2:generateReleaseJacocoReport`

### Code style

`./gradlew :apphud-v2:lint :apphud-v2:ktlint :apphud-v2:checkstyleRelease`

### Check AAR API

`./gradlew :apphud-v2:aarCheck`

### Regenerate AAR API dump

`./gradlew :apphud-v2:aarDump`
