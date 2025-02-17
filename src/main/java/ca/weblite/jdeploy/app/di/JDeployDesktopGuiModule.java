package ca.weblite.jdeploy.app.di;

import ca.weblite.jdeploy.DIContext;
import ca.weblite.jdeploy.app.config.JdeployAppConfig;
import ca.weblite.jdeploy.app.config.JdeployAppConfigInterface;
import ca.weblite.jdeploy.app.repositories.impl.jpa.di.EmfProvider;
import ca.weblite.jdeploy.app.repositories.impl.jpa.di.EmfProviderInterface;
import ca.weblite.jdeploy.app.repositories.impl.jpa.di.JdeployJpaModule;
import ca.weblite.jdeploy.app.system.env.ClockInterface;
import ca.weblite.jdeploy.app.system.impl.javase.SystemClock;
import org.codejargon.feather.Provides;

public class JDeployDesktopGuiModule {

    public void install() {
        DIContext.initialize(
                new JdeployJpaModule(),
                new JdeployGuiModule(),
                this
        );
    }

    @Provides
    protected EmfProviderInterface getEmfProvider() {
        return DIContext.get(EmfProvider.class);
    }

    @Provides
    protected JdeployAppConfigInterface getConfig() {
        return DIContext.get(JdeployAppConfig.class);
    }

    @Provides
    protected ClockInterface getClock() {
        return DIContext.get(SystemClock.class);
    }
}
