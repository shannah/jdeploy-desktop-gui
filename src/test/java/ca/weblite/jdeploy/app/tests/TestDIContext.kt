package ca.weblite.jdeploy.app.tests

import ca.weblite.jdeploy.app.config.JdeployAppConfig
import ca.weblite.jdeploy.app.config.JdeployAppConfigInterface
import ca.weblite.jdeploy.app.config.TestAppConfig
import ca.weblite.jdeploy.app.di.DIContext
import ca.weblite.jdeploy.app.repositories.impl.jpa.di.EmfProviderInterface
import ca.weblite.jdeploy.app.repositories.impl.jpa.tests.TestEmfProvider
import org.codejargon.feather.Provides

class TestDIContext: DIContext() {
    override fun getEmfProvider(): EmfProviderInterface {
        return TestEmfProvider()
    }

    override fun getConfig(): JdeployAppConfigInterface {
        return this.getInstance(TestAppConfig::class.java)
    }
}