# AppMetrica SDK AppSetID

**Required module**.
Maven: `io.appmetrica.analytics:analytics-appsetid:${VERSION}`.

## Builds

### Assemble

`./gradlew :appsetid:assembleRelease`

### Publish to MavenLocal

`./gradlew :appsetid:publishReleasePublicationToMavenLocal`

### Tests

`./gradlew :appsetid:testReleaseUnitTest :appsetid:generateReleaseJacocoReport`

### Code style

`./gradlew :appsetid:codequality`

### Check AAR API

`./gradlew :appsetid:aarCheck`

### Regenerate AAR API dump

`./gradlew :appsetid:aarDump`
