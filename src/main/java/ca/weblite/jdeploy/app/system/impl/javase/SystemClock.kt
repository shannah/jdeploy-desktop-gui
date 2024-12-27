package ca.weblite.jdeploy.app.system.impl.javase

class SystemClock : ca.weblite.jdeploy.app.system.env.ClockInterface {
    override fun now(): java.util.Calendar {
        return java.util.Calendar.getInstance()
    }
}
