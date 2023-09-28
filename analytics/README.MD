# AppMetrica Analytics module

**Required module**.
Maven: `io.appmetrica.analytics:analytics:${VERSION}`.

## Builds

### Assemble

`./gradlew :analytics:assembleBinaryProdRelease`

### Publish to MavenLocal

`./gradlew :analytics:publishBinaryProdReleasePublicationToMavenLocal`

### Tests

`./gradlew :analytics:testBinaryProdReleaseUnitTest :analytics:generateBinaryProdReleaseJacocoReport`

### Code style

`./gradlew :analytics:lint :analytics:ktlint :analytics:checkstyleBinaryProdRelease`

### Check AAR API

`./gradlew :analytics:aarCheck`

### Regenerate AAR API dump

`./gradlew :analytics:aarDump`
