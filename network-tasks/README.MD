# AppMetrica SDK Network Tasks

**Required module**.
Maven: `io.appmetrica.analytics:analytics-network-tasks:${VERSION}`.

## Builds

### Assemble

`./gradlew :network-tasks:assembleRelease`

### Publish to MavenLocal

`./gradlew :network-tasks:publishReleasePublicationToMavenLocal`

### Tests

`./gradlew :network-tasks:testReleaseUnitTest :network-tasks:generateReleaseJacocoReport`

### Code style

`./gradlew :network-tasks:codequality`

### Check AAR API

`./gradlew :network-tasks:aarCheck`

### Regenerate AAR API dump

`./gradlew :network-tasks:aarDump`
