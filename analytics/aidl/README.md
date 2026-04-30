# Third-party AIDL interfaces

This directory contains AIDL interface definitions copied from third-party SDKs
(e.g. RuStore) that AppMetrica communicates with over Binder IPC **without**
taking a compile-time dependency on those SDKs.

## Why keep them here?

Android's `aidl` tool generates Java code whose `DESCRIPTOR` string (used by
`Binder.enforceInterface`) is derived from the **package declared in the `.aidl`
file**, not from the Java package of the generated class. To pass the
`enforceInterface` check on the remote side, our generated code must carry the
**original third-party package as `DESCRIPTOR`**, while the Java `package`
statement can differ to avoid classpath conflicts.

The files here preserve the original package declarations. The Gradle task
`generateThirdPartyAidl` generates Java with the correct `DESCRIPTOR` and then
rewrites only the Java `package` declaration to our internal package.

## Directory layout

```
aidl/
└── <group>/          # one subdirectory per third-party SDK / feature area
    ├── config.yaml   # generation config for this group (see below)
    └── *.aidl        # interface files with the original third-party package
```

## config.yaml format

Each group has its own `config.yaml` with two fields:

```yaml
# The package declared in the .aidl files — used verbatim as the Binder DESCRIPTOR.
originalPackage: com.example.sdk

# The Java package written into the generated .java files.
targetPackage: io.appmetrica.analytics.impl.myfeature.aidl
```

## How to add a new interface group

1. Create a subdirectory under `aidl/` (e.g. `aidl/mysdkgroup/`).
2. Place the `.aidl` files inside, keeping the **original third-party `package` declaration**.
3. Add a `config.yaml` in that subdirectory (see format above).
4. Register the Gradle task in the module's `build.gradle.kts`:
   ```kotlin
   import io.appmetrica.analytics.gradle.aidl.GenerateThirdPartyAidlTask

   project.tasks.register<GenerateThirdPartyAidlTask>("generateThirdPartyAidl")
   ```
5. Run the generation task (see below).
6. Commit the `.aidl` sources, `config.yaml`, and the generated `.java` files.

## Regenerating the Java files

```bash
./gradlew :analytics:generateThirdPartyAidl
```

The task:

1. Scans `aidl/` for all subdirectories containing a `config.yaml`.
2. Reads `originalPackage` and `targetPackage` from each group's `config.yaml`.
3. Mirrors the `.aidl` files into a temporary directory with the package path structure that `aidl` requires.
4. Invokes the `aidl` compiler (resolved via the Android Gradle Plugin SDK components).
5. Rewrites the `package` declaration in each generated Java file from the original
   third-party package to the target internal package. The `DESCRIPTOR` string literal
   is preserved unchanged.
6. Prepends a header that identifies the file as generated and explains how to regenerate it.
7. Writes the result to `src/main/java/<target/package/path>/`.

> **Note:** The generated `.java` files are committed to the repository so that
> the project can be built without running the generation task. Run the task and
> commit the updated files whenever the source `.aidl` files change.
