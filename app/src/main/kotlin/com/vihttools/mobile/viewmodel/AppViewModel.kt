package com.vihttools.mobile.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vihttools.mobile.data.AppDatabase
import com.vihttools.mobile.data.Report
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val reportDao = database.reportDao()

    private val _reports = MutableStateFlow<List<Report>>(emptyList())
    val reports: StateFlow<List<Report>> = _reports.asStateFlow()

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()

    private val _isOverlayRunning = MutableStateFlow(false)
    val isOverlayRunning: StateFlow<Boolean> = _isOverlayRunning.asStateFlow()

    private val _gameDetected = MutableStateFlow(false)
    val gameDetected: StateFlow<Boolean> = _gameDetected.asStateFlow()

    init {
        observeReports()
    }

    private fun observeReports() {
        viewModelScope.launch {
            reportDao.getAllReports().collect { reportList ->
                _reports.value = reportList
            }
        }

        viewModelScope.launch {
            reportDao.getUnreadReportCount().collect { count ->
                _unreadCount.value = count
            }
        }
    }

    fun addReport(nickname: String, playerId: Int, text: String, reportCount: Int) {
        viewModelScope.launch {
            val report = Report(
                nickname = nickname,
                playerId = playerId,
                text = text,
                reportCount = reportCount,
                timestamp = System.currentTimeMillis()
            )
            reportDao.insertReport(report)
        }
    }

    fun markReportAsAnswered(reportId: Int) {
        viewModelScope.launch {
            reportDao.markAsAnswered(reportId, System.currentTimeMillis())
        }
    }

    fun setOverlayRunning(running: Boolean) {
        _isOverlayRunning.value = running
    }

    fun setGameDetected(detected: Boolean) {
        _gameDetected.value = detected
    }

    fun getActiveReportCount(): Int {
        return _reports.value.count { !it.isAnswered }
    }

    fun getRespondedReportCount(): Int {
        return _reports.value.count { it.isAnswered }
    }
}
