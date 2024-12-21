package ca.weblite.jdeploy.app.tests

import ca.weblite.jdeploy.app.di.DIContext
import ca.weblite.jdeploy.app.repositories.impl.jpa.di.EmfProviderInterface
import ca.weblite.jdeploy.app.repositories.impl.jpa.tests.TestEmfProvider
import org.codejargon.feather.Provides

class TestDIContext: DIContext() {
    override fun getEmfProvider(): EmfProviderInterface {
        return TestEmfProvider()
    }
}