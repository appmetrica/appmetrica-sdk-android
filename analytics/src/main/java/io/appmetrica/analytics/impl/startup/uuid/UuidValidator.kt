package io.appmetrica.analytics.impl.startup.uuid

import io.appmetrica.analytics.logger.internal.DebugLogger
import java.util.UUID

class UuidValidator {

    private val tag = "[UuidValidator]"

    private val delimiter = "-"

    fun isValid(uuidStr: String?): Boolean {
        if (uuidStr == null || uuidStr.length != 32) {
            DebugLogger.info(tag, "Uuid is invalid: length = ${uuidStr?.length}; value = $uuidStr")
            return false
        }

        val uuid = parseUuid(uuidStr)
        if (uuid == null) {
            DebugLogger.error(tag, "Uuid has invalid format: $uuidStr")
            return false
        }

        return true
    }

    private fun parseUuid(uuidStr: String): UUID? = try {
        UUID.fromString(normalizeUuid(uuidStr))
    } catch (e: Throwable) {
        DebugLogger.error(tag, e)
        null
    }

    private fun normalizeUuid(uuid: String) = uuid.substring(0, 8) + delimiter +
        uuid.substring(8, 12) + delimiter +
        uuid.substring(12, 16) + delimiter +
        uuid.substring(16, 20) + delimiter +
        uuid.substring(20, 32)
}
