# AppMetrica SDK Certificates

**Optional module**.
Maven: `io.appmetrica.analytics:analytics-certificates:${VERSION}`.

Provides an optional additional TLS trust path for AppMetrica SDK network
requests. Platform trust is checked first.
Excluding it keeps the SDK on the platform TLS policy.

**Certificate Transparency is not verified** by this module.

## Builds

### Assemble

`./gradlew :certificates:assembleRelease`

### Publish to MavenLocal

`./gradlew :certificates:publishReleasePublicationToMavenLocal`

### Check AAR API

`./gradlew :certificates:aarCheck`

### Regenerate AAR API dump

`./gradlew :certificates:aarDump`
