package com.alfayedoficial.astagfirullah.data.telemetry

import com.alfayedoficial.astagfirullah.AstagfirullahSettings
import com.alfayedoficial.astagfirullah.core.Constants
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.util.concurrency.AppExecutorUtil
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Anonymous, aggregate usage telemetry.
 *
 * Every install accumulates a count of remembrance phrases displayed and, once an hour in
 * the background, sends the batch to the backend keyed by a random per-install device id.
 * This is how usage from users who are NOT logged in still gets counted; logged-in
 * per-account stats are unaffected and continue through the existing path.
 *
 * Privacy: the only things transmitted are a random UUID device id, an integer count, and
 * the platform string. No name, email, file, project, or code ever leaves the machine. The
 * whole feature is off with one switch in Settings ([AstagfirullahSettings.anonymousStatsEnabled]).
 */
@Service(Service.Level.APP)
class PraiseTelemetryService {

    private val logger = Logger.getInstance(PraiseTelemetryService::class.java)
    private val scheduled = AtomicBoolean(false)
    private val sending = AtomicBoolean(false)

    private val settings get() = AstagfirullahSettings.getInstance()

    /** Accumulates displayed phrases toward the next hourly flush. Cheap; safe to call often. */
    fun record(count: Int) {
        if (count <= 0) return
        synchronized(this) {
            val next = settings.pendingStatsCount.toLong() + count
            settings.pendingStatsCount = next.coerceAtMost(Constants.TELEMETRY_MAX_BATCH.toLong()).toInt()
        }
    }

    /** Idempotently schedules the hourly background flush. Call once at startup. */
    fun ensureScheduled() {
        if (!scheduled.compareAndSet(false, true)) return
        val interval = Constants.TELEMETRY_FLUSH_INTERVAL_MINUTES
        AppExecutorUtil.getAppScheduledExecutorService().scheduleWithFixedDelay(
            { flush() },
            interval, // initial delay: don't send the instant the IDE opens
            interval,
            TimeUnit.MINUTES,
        )
        logger.debug("Praise telemetry scheduled every $interval min")
    }

    /**
     * Sends the pending count if there is anything to send and the user has not opted out.
     * Pending is cleared only on a confirmed 2xx, so a failed or not-yet-deployed endpoint
     * simply carries the count to the next hour rather than losing it. Runs on a background
     * thread already (the scheduler); never call from the EDT.
     */
    fun flush() {
        if (!settings.anonymousStatsEnabled) return
        if (!sending.compareAndSet(false, true)) return
        try {
            val count = synchronized(this) { settings.pendingStatsCount }
            if (count <= 0) return

            val deviceId = settings.getOrCreateDeviceId()
            val ok = postCount(deviceId, count)
            if (ok) {
                synchronized(this) {
                    // Subtract exactly what we sent; anything recorded meanwhile survives.
                    settings.pendingStatsCount = (settings.pendingStatsCount - count).coerceAtLeast(0)
                }
                settings.lastStatsFlushTime = System.currentTimeMillis()
                logger.debug("Flushed $count praise counts")
            }
        } catch (e: Exception) {
            logger.debug("Telemetry flush failed (will retry next interval): ${e.message}")
        } finally {
            sending.set(false)
        }
    }

    private fun postCount(deviceId: String, count: Int): Boolean {
        val url = "${Constants.API_BASE_URL_V1}${Constants.API_TELEMETRY_PRAISE_ENDPOINT}"
        var connection: HttpURLConnection? = null
        return try {
            val body = """{"device_id":"$deviceId","count":$count,"app_type":"${Constants.API_APP_TYPE}","platform":"JETBRAINS_PLUGIN"}"""
            connection = (URL(url).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                doOutput = true
                connectTimeout = (Constants.API_TIMEOUT_SECONDS * 1000).toInt()
                readTimeout = (Constants.API_TIMEOUT_SECONDS * 1000).toInt()
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Accept", "application/json")
            }
            OutputStreamWriter(connection.outputStream).use { it.write(body); it.flush() }
            val code = connection.responseCode
            // Drain so the connection can be pooled.
            runCatching { (if (code in 200..299) connection.inputStream else connection.errorStream)?.readBytes() }
            code in 200..299
        } catch (e: Exception) {
            logger.debug("Telemetry POST error: ${e.message}")
            false
        } finally {
            connection?.disconnect()
        }
    }

    companion object {
        @JvmStatic
        fun getInstance(): PraiseTelemetryService =
            com.intellij.openapi.application.ApplicationManager.getApplication()
                .getService(PraiseTelemetryService::class.java)
    }
}
