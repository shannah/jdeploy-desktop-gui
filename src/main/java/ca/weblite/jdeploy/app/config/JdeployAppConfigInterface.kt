package ca.weblite.jdeploy.app.config

import java.nio.file.Path

interface JdeployAppConfigInterface {
    fun getJdbcUrl(): String
    fun getAppId(): String
    fun getAppDataPath(): Path
}