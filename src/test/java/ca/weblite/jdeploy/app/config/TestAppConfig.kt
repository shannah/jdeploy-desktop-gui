package ca.weblite.jdeploy.app.config

import java.nio.file.FileVisitOption
import java.nio.file.Files
import java.nio.file.Path

class TestAppConfig: JdeployAppConfigInterface {
    private val tempDir: Path = Files.createTempDirectory("testAppData")

    init {
        // Ensures the directory gets cleaned up after tests are complete
        Runtime.getRuntime().addShutdownHook(Thread {
            deleteTempDirectory(tempDir)
        })
    }
    override fun getJdbcUrl(): String {
        return "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1"
    }

    override fun getAppId(): String {
        return "ca.weblite.jdeploy.app"
    }

    override fun getAppDataPath(): Path {
        return tempDir
    }

    private fun deleteTempDirectory(directory: Path) {
        // Delete the temporary directory and its contents
        try {
            Files.walk(directory, FileVisitOption.FOLLOW_LINKS)
                .sorted(Comparator.reverseOrder())
                .forEach { path -> path.toFile().delete() }
        } catch (e: Exception) {
            println("Failed to delete temp directory: ${e.message}")
        }
    }
}