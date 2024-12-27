package ca.weblite.jdeploy.app.tests

import ca.weblite.jdeploy.app.system.env.ClockInterface

class ProgrammableClock: ClockInterface {
    private var timeInMillis: Long = 0

    fun setTimeInMillis(timeInMillis: Long) {
        this.timeInMillis = timeInMillis
    }

    override fun now(): java.util.Calendar {
        val calendar = java.util.Calendar.getInstance()
        calendar.timeInMillis = timeInMillis
        return calendar
    }
}