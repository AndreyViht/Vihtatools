package com.vihttools.mobile.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_settings")

object SettingsManager {

    // Keys
    private val BUTTON_POSITION_X = intPreferencesKey("button_position_x")
    private val BUTTON_POSITION_Y = intPreferencesKey("button_position_y")
    private val BUTTON_TRANSPARENCY = floatPreferencesKey("button_transparency")
    private val OCR_SCAN_INTERVAL = longPreferencesKey("ocr_scan_interval")
    private val IS_DARK_THEME = booleanPreferencesKey("is_dark_theme")
    private val LAST_KNOWN_POSITION = stringPreferencesKey("last_known_position")

    // Button Positions
    enum class ButtonPosition(val x: Int, val y: Int) {
        TOP_RIGHT(0, 100),
        TOP_LEFT(-104, 100),
        BOTTOM_RIGHT(0, -104),
        BOTTOM_LEFT(-104, -104)
    }

    // OCR Intervals
    enum class OCRInterval(val ms: Long) {
        FAST(500L),
        NORMAL(1000L),
        BALANCED(1500L),
        SLOW(2000L)
    }

    fun getButtonPositionX(context: Context): Flow<Int> {
        return context.dataStore.data.map { preferences ->
            preferences[BUTTON_POSITION_X] ?: ButtonPosition.TOP_RIGHT.x
        }
    }

    fun getButtonPositionY(context: Context): Flow<Int> {
        return context.dataStore.data.map { preferences ->
            preferences[BUTTON_POSITION_Y] ?: ButtonPosition.TOP_RIGHT.y
        }
    }

    suspend fun setButtonPosition(context: Context, x: Int, y: Int) {
        context.dataStore.edit { preferences ->
            preferences[BUTTON_POSITION_X] = x
            preferences[BUTTON_POSITION_Y] = y
        }
    }

    fun getButtonTransparency(context: Context): Flow<Float> {
        return context.dataStore.data.map { preferences ->
            preferences[BUTTON_TRANSPARENCY] ?: 0.5f
        }
    }

    suspend fun setButtonTransparency(context: Context, transparency: Float) {
        context.dataStore.edit { preferences ->
            preferences[BUTTON_TRANSPARENCY] = transparency.coerceIn(0.2f, 0.8f)
        }
    }

    fun getOCRScanInterval(context: Context): Flow<Long> {
        return context.dataStore.data.map { preferences ->
            preferences[OCR_SCAN_INTERVAL] ?: OCRInterval.BALANCED.ms
        }
    }

    suspend fun setOCRScanInterval(context: Context, interval: Long) {
        context.dataStore.edit { preferences ->
            preferences[OCR_SCAN_INTERVAL] = interval
        }
    }

    fun getIsDarkTheme(context: Context): Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            preferences[IS_DARK_THEME] ?: true
        }
    }

    suspend fun setIsDarkTheme(context: Context, isDark: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_DARK_THEME] = isDark
        }
    }

    suspend fun setLastKnownPosition(context: Context, position: String) {
        context.dataStore.edit { preferences ->
            preferences[LAST_KNOWN_POSITION] = position
        }
    }

    fun getLastKnownPosition(context: Context): Flow<String> {
        return context.dataStore.data.map { preferences ->
            preferences[LAST_KNOWN_POSITION] ?: "TOP_RIGHT"
        }
    }
}
