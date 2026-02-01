package com.example.mission

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews

class MissionWidget : AppWidgetProvider() {

    private val PREFS_NAME = "MissionPrefs"
    //private val KEY_MISSION = "mission_text"
    //private val KEY_POINTS = "mission_points"

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        updateAllWidgets(context, appWidgetManager, appWidgetIds)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == AppWidgetManager.ACTION_APPWIDGET_UPDATE) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val ids = appWidgetManager.getAppWidgetIds(ComponentName(context, MissionWidget::class.java))
            updateAllWidgets(context, appWidgetManager, ids)
        }
    }

    private fun updateAllWidgets(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        // Use the same key as MainActivity
        val textoSalvo = prefs.getString("LISTA_COMPLETA", "") ?: ""

        var mission = "Sem missão"
        var points = ""

        if (textoSalvo.isNotEmpty()) {
            val primeira = textoSalvo.split(";;;")[0]
            val partes = primeira.split("|")
            if (partes.size == 2) {
                mission = partes[0]
                points = "${partes[1]} pts" // Added "pts" for clarity
            }
        }

        for (appWidgetId in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.widget_mission)
            views.setTextViewText(R.id.widget_title, mission)
            views.setTextViewText(R.id.widget_text, points)

            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
            views.setOnClickPendingIntent(R.id.widget_container, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
