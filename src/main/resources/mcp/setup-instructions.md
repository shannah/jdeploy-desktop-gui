# jDeploy Setup Instructions

After initializing the project, complete these steps to ensure your application can be deployed:

## 1. Verify Build Configuration

Check that your project produces an executable JAR:

**Maven Projects** (`pom.xml`):
- Ensure `maven-jar-plugin` is configured with a main class in the manifest
- Use `maven-dependency-plugin` to copy dependencies to a `libs/` directory
- The manifest should include `classpathPrefix: libs/`

**Gradle Projects** (`build.gradle` or `build.gradle.kts`):
- Configure the `jar` task with a manifest specifying the main class
- Ensure dependencies are copied alongside the JAR
- Alternative: use the Shadow plugin to create a fat/uber JAR

## 2. Configure package.json

Verify the `jdeploy` section in `package.json`:

```json
{
  "jdeploy": {
    "jar": "target/your-app.jar",
    "javaVersion": "17",
    "title": "Your Application Name"
  }
}
```

Key fields:
- `jar`: Path to your executable JAR (relative to project root)
- `javaVersion`: Minimum Java version required (e.g., "11", "17", "21")
- `title`: Display name for your application

## 3. Detect JavaFX Usage

Search your source files for JavaFX imports:
```
import javafx.
```

If JavaFX is used, add to the jdeploy config:
```json
{
  "jdeploy": {
    "javafx": true
  }
}
```

## 4. Add Application Icon

Place a square PNG image (preferably 256x256 pixels or larger) named `icon.png` in the project root.

Common locations to find existing icons:
- `src/main/resources/icons/`
- `src/main/resources/images/`
- Framework-specific directories

## 5. Optional: Configure Build Command

If your project requires a build step before packaging, add:
```json
{
  "jdeploy": {
    "buildCommand": "mvn clean package -DskipTests"
  }
}
```

## 6. Compose Multiplatform Projects

For Kotlin Compose Multiplatform projects:
- Use explicit platform dependencies instead of `compose.desktop.currentOs`
- Declare dependencies for: `linux_x64`, `linux_arm64`, `macos_x64`, `macos_arm64`, `windows_x64`
- Consider using the Shadow plugin to create a unified JAR

## 7. Test Your Configuration

Run these commands to verify:
```bash
# Install jDeploy CLI (if not already installed)
npm install -g jdeploy

# Build and test locally
npx jdeploy build

# Preview the app
npx jdeploy run
```

## Common Issues

**JAR not found**: Verify the `jar` path in package.json matches your build output location.

**Missing dependencies**: Ensure your JAR manifest includes the classpath or use a shaded JAR.

**Java version mismatch**: Set `javaVersion` to match your project's minimum requirement.

**Native libraries**: For apps using native libraries (JNI), you may need to configure platform-specific builds.
