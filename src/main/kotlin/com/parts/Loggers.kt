package com.parts

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/** Utility functions to get loggers w/o platform type warnings, etc. */
object Loggers {
    /** Get a logger for the given class. */
    inline fun <reified T : Any> getLogger(): Logger = LoggerFactory.getLogger(T::class.java)
}
