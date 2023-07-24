# [AppMetrica SDK](https://appmetrica.yandex.com)

AppMetrica is a free real-time ad tracking and mobile app analytics solution. AppMetrica covers the three key features for discovering your app's performance — ad tracking, usage analytics and crash analytics.
Detailed information and instructions for integration are available in the [documentation](https://appmetrica.yandex.com/docs/).

## Builds

### Assemble

`./gradlew :assembleRelease`

### Publish to MavenLocal

`./gradlew :publishReleasePublicationToMavenLocal`

### Tests

`./gradlew :testReleaseUnitTest :generateReleaseJacocoReport`

### Code style

`./gradlew :codequality`

### Check AAR API

`./gradlew :aarCheck`

### Regenerate AAR API dump

`./gradlew :aarDump`
