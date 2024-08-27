# AppMetrica SDK Apphud integration module

**Optional module**.
Maven: `io.appmetrica.analytics:analytics-apphud:${VERSION}`.

## Builds

### Assemble

`./gradlew :apphud:assembleRelease`

### Publish to MavenLocal

`./gradlew :apphud:publishReleasePublicationToMavenLocal`

### Tests

`./gradlew :apphud:testReleaseUnitTest :apphud:generateReleaseJacocoReport`

### Code style

`./gradlew :apphud:lint :apphud:ktlint :apphud:checkstyleRelease`

### Check AAR API

`./gradlew :apphud:aarCheck`

### Regenerate AAR API dump

`./gradlew :apphud:aarDump`
