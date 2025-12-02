# AppMetrica SDK Apphud v3 integration module

**Optional module**.
Maven: `io.appmetrica.analytics:analytics-apphud-v3:${VERSION}`.

## Builds

### Assemble

`./gradlew :apphud-v3:assembleRelease`

### Publish to MavenLocal

`./gradlew :apphud-v3:publishReleasePublicationToMavenLocal`

### Tests

`./gradlew :apphud-v3:testReleaseUnitTest :apphud-v3:generateReleaseJacocoReport`

### Code style

`./gradlew :apphud-v3:lint :apphud-v3:ktlint :apphud-v3:checkstyleRelease`

### Check AAR API

`./gradlew :apphud-v3:aarCheck`

### Regenerate AAR API dump

`./gradlew :apphud-v3:aarDump`
