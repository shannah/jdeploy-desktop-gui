package ca.weblite.jdeploy.app.config

import java.nio.file.Path
import java.nio.file.Paths

class JdeployAppConfig: JdeployAppConfigInterface {
    override fun getJdbcUrl(): String {
        return "jdbc:sqlite:${getAppDataPath().resolve("jdeploy.db")}"
    }

    override fun getAppId(): String {
        return "ca.weblite.jdeploy.app"
    }

    override fun getAppDataPath(): Path {
        val os = System.getProperty("os.name").toLowerCase()
        var appDataPath: Path = Paths.get(System.getProperty("user.home"))

        when {
            os.contains("win") -> {
                val appData = System.getenv("APPDATA") ?: System.getenv("LOCALAPPDATA")
                appDataPath = Paths.get(appData ?: (System.getProperty("user.home") + "\\AppData\\Roaming"))
            }
            os.contains("mac") -> {
                val home = System.getenv("HOME") ?: System.getProperty("user.home")
                appDataPath = Paths.get(home, "Library", "Application Support")
            }
            os.contains("nix") || os.contains("nux") -> {
                val configDir = System.getenv("XDG_CONFIG_HOME")
                appDataPath = if (configDir != null) {
                    Paths.get(configDir)
                } else {
                    Paths.get(System.getProperty("user.home"), ".config")
                }
            }
        }

        return appDataPath.resolve(getAppId())
    }
}