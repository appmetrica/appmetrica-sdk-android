# AppMetrica SDK Protocol Buffer JavaNano API

**This code is from https://android.googlesource.com/platform/external/protobuf/+/ad2472f14d85d1b2af0ada7610e8afa69c5b4992/javanano/**

**Required module**.
Maven: `io.appmetrica.analytics:analytics-proto:${VERSION}`.

## Builds

### Assemble

`./gradlew :proto:assembleRelease`

### Publish to MavenLocal

`./gradlew :proto:publishReleasePublicationToMavenLocal`

### Tests

`./gradlew :proto:testReleaseUnitTest :proto:generateReleaseJacocoReport`

### Code style

`./gradlew :proto:codequality`

### Check AAR API

`./gradlew :proto:aarCheck`

### Regenerate AAR API dump

`./gradlew :proto:aarDump`
