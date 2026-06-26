# AppMetrica SDK Product Flow

**Optional module**.
Maven: `io.appmetrica.analytics:analytics-product-flow:${VERSION}`.

## Builds

### Assemble

`./gradlew :product-flow:assembleRelease`

### Publish to MavenLocal

`./gradlew :product-flow:publishReleasePublicationToMavenLocal`

### Tests

`./gradlew :product-flow:testReleaseUnitTest :product-flow:generateReleaseJacocoReport`

### Code style

`./gradlew :product-flow:lint :product-flow:ktlint :product-flow:checkstyleRelease`

### Check AAR API

`./gradlew :product-flow:aarCheck`

### Regenerate AAR API dump

`./gradlew :product-flow:aarDump`
