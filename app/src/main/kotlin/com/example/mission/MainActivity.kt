package com.example.mission

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var editText: EditText
    private lateinit var editTextPoints: EditText
    private lateinit var scoreTextView: TextView
    private lateinit var button: Button
    private lateinit var buttonDone: Button
    private lateinit var buttonMainLine: Button
    private val PREFS_NAME = "MissionPrefs"
    private val KEY_MISSION = "mission_text"
    private val KEY_POINTS = "mission_points"
    private val KEY_SCORE = "score_text"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        editText = findViewById(R.id.editTextMission)
        editTextPoints = findViewById(R.id.editTextPoints)
        scoreTextView = findViewById(R.id.scoreView)
        button = findViewById(R.id.buttonSave)
        buttonDone = findViewById(R.id.buttonDone)
        buttonMainLine = findViewById(R.id.button_main_line)

        //Isso aqui que salva os pontos e as missoes
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        editText.setText(prefs.getString(KEY_MISSION, ""))
        editTextPoints.setText(prefs.getString(KEY_POINTS, ""))
        scoreTextView.text = prefs.getInt(KEY_SCORE, 0).toString()

        button.setOnClickListener {
            val mission = editText.text.toString()
            val points = editTextPoints.text.toString()
            prefs.edit()
                .putString(KEY_MISSION, mission)
                .putString(KEY_POINTS, points)
                .apply()

            // Atualiza o widget
            val intent = Intent(this, MissionWidget::class.java)
            intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            val ids = AppWidgetManager.getInstance(this)
                .getAppWidgetIds(ComponentName(this, MissionWidget::class.java))
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
            sendBroadcast(intent)

            Toast.makeText(this, "Missão salva!", Toast.LENGTH_SHORT).show()
        }

        buttonDone.setOnClickListener {
            val points = editTextPoints.text.toString().toInt()
            addScore(points)
            val actualScore = scoreTextView.text.toString().toInt()
            prefs.edit().putInt(KEY_SCORE, actualScore).apply()
        }

        buttonMainLine.setOnClickListener {
            // Cria a intenção: (Origem: AQUI, Destino: NovaTela)
            val intent = Intent(this, Objectives::class.java)

            // Executa a ação
            startActivity(intent)
        }
    }

    fun addScore(points: Int){
        var sum = scoreTextView.text.toString().toInt()
        sum += points
        scoreTextView.text = sum.toString()
    }
}
