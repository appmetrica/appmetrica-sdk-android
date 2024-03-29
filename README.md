# [AppMetrica SDK](https://appmetrica.io)

AppMetrica is a free real-time ad tracking and mobile app analytics solution. AppMetrica covers the three key features for discovering your app's performance — ad tracking, usage analytics and crash analytics.
Detailed information and instructions for integration are available in the [documentation](https://appmetrica.io/docs/).

Classes in packages `${MODULE_NAMESPACE}.internal` are intended for interaction between modules and are not part of the public API. Also changing these classes does not correspond to [semver](https://semver.org/).

## Builds

### Assemble

`./gradlew :assembleRelease`

### Publish to MavenLocal

`./gradlew :publishReleasePublicationToMavenLocal`

### Tests

`./gradlew :testReleaseUnitTest :generateReleaseJacocoReport`

### Code style

`./gradlew lint ktlint checkstyle`

### Check AAR API

`./gradlew :aarCheck`

### Regenerate AAR API dump

`./gradlew :aarDump`
