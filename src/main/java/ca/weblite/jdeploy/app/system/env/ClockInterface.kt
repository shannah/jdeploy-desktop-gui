package ca.weblite.jdeploy.app.system.env

import java.util.Calendar

interface ClockInterface {
    fun now(): Calendar
}