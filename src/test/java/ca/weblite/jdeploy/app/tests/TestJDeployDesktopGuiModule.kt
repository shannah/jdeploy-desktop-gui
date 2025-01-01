package ca.weblite.jdeploy.app.tests

import ca.weblite.jdeploy.DIContext
import ca.weblite.jdeploy.app.config.JdeployAppConfigInterface
import ca.weblite.jdeploy.app.config.TestAppConfig
import ca.weblite.jdeploy.app.di.JDeployDesktopGuiModule
import ca.weblite.jdeploy.app.repositories.impl.jpa.di.EmfProviderInterface
import ca.weblite.jdeploy.app.repositories.impl.jpa.tests.TestEmfProvider
import ca.weblite.jdeploy.app.system.env.ClockInterface

class TestJDeployDesktopGuiModule: JDeployDesktopGuiModule() {

    private var clock: ClockInterface? = null

    public fun setClock(clock: ClockInterface) {
        this.clock = clock
    }

    override fun getClock(): ClockInterface {
        if (clock == null) {
           return super.getClock()
        }
        return clock!!
    }

    override fun getEmfProvider(): EmfProviderInterface {
        return TestEmfProvider()
    }

    override fun getConfig(): JdeployAppConfigInterface {
        return DIContext.get(TestAppConfig::class.java)
    }
}