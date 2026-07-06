# Building From Source

Install a JDK that can run Gradle. JDK 17 or newer is recommended for the Gradle runtime.

The mod itself is compiled for Java 8 bytecode.

Clone the repository:

```powershell
git clone <repo-url>
cd mcsr-easy-mode
```

Build the mod:

```powershell
gradle build
```

If your shell is still using Java 8, point Gradle at a newer JDK first:

```powershell
$env:JAVA_HOME='C:\Program Files\Java\jdk-25'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
gradle build
```

The compiled jar will be created at:

```text
build/libs/mcsreasymode-<version>.jar
```

Install that jar into your `mods` folder alongside SpeedrunAPI.

## Development Notes

- Minecraft version: `1.16.1`
- Yarn mappings: `1.16.1+build.21`
- Fabric Loom: `1.15.5`
- SpeedrunAPI dependency: `com.github.contariaa:SpeedrunAPI:v2.1-1.16.1`
- Fabric Resource Loader v0 is bundled through Gradle for resource loading.
- Full Fabric API is intentionally not used.
- Local Gradle temp folders such as `.gradle-tmp*` should not be committed.
