package com.vihttools.mobile.data

/**
 * Circular buffer for managing the last 10 active reports
 * When buffer is full, new reports replace the oldest ones
 */
class ReportCircularBuffer(private val maxSize: Int = 10) {

    private val buffer = mutableListOf<Report>()
    private val deduplicationSet = mutableSetOf<String>()  // nickname+ID pairs

    /**
     * Add a report to the buffer
     * Returns true if added, false if duplicate
     */
    fun add(report: Report): Boolean {
        val key = "${report.nickname}:${report.playerId}"

        // Check for duplicates
        if (deduplicationSet.contains(key)) {
            return false
        }

        // Add to buffer
        buffer.add(report)
        deduplicationSet.add(key)

        // Remove oldest if buffer is full
        if (buffer.size > maxSize) {
            val removed = buffer.removeAt(0)
            deduplicationSet.remove("${removed.nickname}:${removed.playerId}")
        }

        return true
    }

    /**
     * Get all reports in the buffer
     */
    fun getAll(): List<Report> {
        return buffer.toList()
    }

    /**
     * Get a report by ID
     */
    fun getById(id: Int): Report? {
        return buffer.find { it.id == id }
    }

    /**
     * Remove a report from the buffer
     */
    fun remove(report: Report): Boolean {
        val removed = buffer.remove(report)
        if (removed) {
            deduplicationSet.remove("${report.nickname}:${report.playerId}")
        }
        return removed
    }

    /**
     * Clear all reports
     */
    fun clear() {
        buffer.clear()
        deduplicationSet.clear()
    }

    /**
     * Get the count of reports
     */
    fun size(): Int {
        return buffer.size
    }

    /**
     * Check if buffer is full
     */
    fun isFull(): Boolean {
        return buffer.size >= maxSize
    }

    /**
     * Get count of unread reports
     */
    fun getUnreadCount(): Int {
        return buffer.count { !it.isAnswered }
    }

    /**
     * Check if a report exists (by nickname and ID)
     */
    fun exists(nickname: String, playerId: Int): Boolean {
        return deduplicationSet.contains("$nickname:$playerId")
    }
}
