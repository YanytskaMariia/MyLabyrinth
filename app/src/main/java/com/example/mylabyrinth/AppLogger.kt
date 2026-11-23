package com.example.mylabyrinth

import java.util.logging.ConsoleHandler
import java.util.logging.Level
import java.util.logging.Logger
import java.util.logging.SimpleFormatter

// Об'єкт для логування подій застосунку
object AppLogger {
    // Налаштування логера: виводить усі рівні повідомлень в консоль
    private val logger: Logger = Logger.getLogger("MyLabyrinthApp").apply {
        level = Level.ALL
        useParentHandlers = false
        val handler = ConsoleHandler().apply {
            level = Level.ALL
            formatter = SimpleFormatter()
        }
        addHandler(handler)
    }

    // Метод для інформаційних повідомлень
    fun i(message: String) {
        logger.log(Level.INFO, message)
    }

    // Метод для налагоджувальних повідомлень
    fun d(message: String) {
        logger.log(Level.FINE, message)
    }

    // Метод для попереджень
    fun w(message: String) {
        logger.log(Level.WARNING, message)
    }
}