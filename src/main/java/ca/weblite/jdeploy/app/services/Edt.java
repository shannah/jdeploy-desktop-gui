package ca.weblite.jdeploy.app.services;

import javax.inject.Singleton;
import java.awt.*;

@Singleton
public class Edt {
    public void invokeLater(Runnable runnable) {
        EventQueue.invokeLater(runnable);
    }
}
