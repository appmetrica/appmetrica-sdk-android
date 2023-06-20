# AppMetrica SDK Google Play Location Library Wrapper

**Required module**.
Maven: `io.appmetrica.analytics:analytics-gpllibrary:${VERSION}`.

## Builds

### Assemble

`./gradlew :gpllibrary:assembleRelease`

### Publish to MavenLocal

`./gradlew :gpllibrary:publishReleasePublicationToMavenLocal`

### Tests

`./gradlew :gpllibrary:testReleaseUnitTest :gpllibrary:generateReleaseJacocoReport`

### Code style

`./gradlew :gpllibrary:codequality`

### Check AAR API

`./gradlew :gpllibrary:aarCheck`

### Regenerate AAR API dump

`./gradlew :gpllibrary:aarDump`
