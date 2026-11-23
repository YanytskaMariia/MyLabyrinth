package com.example.mylabyrinth

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

// Клас для головного екрана гри: пов'язує модель, відображення та кнопки
class MainActivity : AppCompatActivity() {

    // Модель лабіринту (дані та логіка гри)
    private lateinit var model: LabyrinthModel
    // Відображення лабіринту на екрані
    private lateinit var labyrinthView: LabyrinthView

    // Метод для налаштування екрана під час створення Activity
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        AppLogger.i("MainActivity started") // запис у лог про запуск

        // створення моделі та пошук елементів інтерфейсу
        model = LabyrinthModel()
        labyrinthView = findViewById(R.id.labyrinthView)
        val btnRestart: Button = findViewById(R.id.btnRestart)
        val btnHint: Button = findViewById(R.id.btnHint)
        val tvTitle: TextView = findViewById(R.id.tvTitle)

        // текст інструкції та передача моделі у View
        tvTitle.text = getString(R.string.game_instruction)
        labyrinthView.model = model

        // при виході з лабіринту показати вікно перемоги
        labyrinthView.onWinListener = {
            showWinDialog()
        }

        // кнопка перезапуску гри
        btnRestart.setOnClickListener {
            restartGame()
        }
        // кнопка підказки
        btnHint.setOnClickListener {
            model.findPathToExit()     // Знаходить маршрут
            labyrinthView.invalidate() // Перемальовує поле з жовтими крапками
        }
        // кнопка зміни мови
        val btnLanguage: Button = findViewById(R.id.btnLanguage)

        btnLanguage.setOnClickListener {
            // Перевіряємо, яка мова зараз активна в додатку
            val currentLang = AppCompatDelegate.getApplicationLocales()[0]?.language

            // Якщо зараз українська — ставимо англійську ("en"), інакше — українську ("uk")
            val newLang = if (currentLang == "uk") "en" else "uk"

            // Застосовуємо нову мову (Activity автоматично перествориться з новими рядками)
            val appLocale = LocaleListCompat.forLanguageTags(newLang)
            AppCompatDelegate.setApplicationLocales(appLocale)
        }
    }

    // Метод для перезапуску гри
    private fun restartGame() {
        model.resetGame()           // скинути модель до початкового стану
        labyrinthView.invalidate()  // перемалювати поле
    }

    // Метод для показу діалогу перемоги
    private fun showWinDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.win_title))
            .setMessage(getString(R.string.win_message))
            .setCancelable(false) // закрити можна лише кнопкою
            .setPositiveButton(getString(R.string.btn_play_again)) { _, _ ->
                restartGame() // почати гру заново
            }

            .show()
    }
}