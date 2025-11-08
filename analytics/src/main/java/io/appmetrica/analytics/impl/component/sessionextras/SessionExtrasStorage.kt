package io.appmetrica.analytics.impl.component.sessionextras

import android.content.Context
import io.appmetrica.analytics.impl.GlobalServiceLocator
import io.appmetrica.analytics.impl.component.ComponentId
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger

class SessionExtrasStorage(context: Context, componentId: ComponentId) {

    private val tag = "[SessionExtrasStorage-${componentId.anonymizedApiKey}]"
    private val dbKey = "session_extras"

    private val converter = SessionExtrasConverter()
    private val serializer = SessionExtrasSerializer()

    private val binaryDataHelper = GlobalServiceLocator.getInstance().getStorageFactory()
        .getComponentBinaryDataHelper(context, componentId).also {
            DebugLogger.info(tag, "create helper for componentId = $componentId")
        }

    var extras: Map<String, ByteArray>
        get() {
            try {
                val valueFromDb = binaryDataHelper.get(dbKey)
                if (valueFromDb != null && valueFromDb.isNotEmpty()) {
                    return converter.toModel(serializer.toState(valueFromDb))
                }
            } catch (e: Throwable) {
                DebugLogger.error(tag, e)
            }
            return converter.toModel(serializer.defaultValue())
        }
        set(value) {
            binaryDataHelper.insert(dbKey, serializer.toByteArray(converter.fromModel(value)))
        }
}
