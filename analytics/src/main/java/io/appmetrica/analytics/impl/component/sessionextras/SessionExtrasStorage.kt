package io.appmetrica.analytics.impl.component.sessionextras

import android.content.Context
import io.appmetrica.analytics.impl.component.ComponentId
import io.appmetrica.analytics.impl.db.storage.DatabaseStorageFactory
import io.appmetrica.analytics.logger.internal.YLogger

class SessionExtrasStorage(context: Context, componentId: ComponentId) {

    private val tag = "[SessionExtrasStorage-${componentId.apiKey}]"
    private val dbKey = "session_extras"

    private val converter = SessionExtrasConverter()
    private val serializer = SessionExtrasSerializer()

    private val binaryDataHelper =
        DatabaseStorageFactory.getInstance(context).getBinaryDbHelperForComponent(componentId).also {
            YLogger.info(tag, "create helper for componentId = $componentId")
        }

    var extras: Map<String, ByteArray>
        get() {
            try {
                val valueFromDb = binaryDataHelper.get(dbKey)
                if (valueFromDb != null && valueFromDb.isNotEmpty()) {
                    return converter.toModel(serializer.toState(valueFromDb))
                }
            } catch (e: Throwable) {
                YLogger.error(tag, e)
            }
            return converter.toModel(serializer.defaultValue())
        }
        set(value) {
            binaryDataHelper.insert(dbKey, serializer.toByteArray(converter.fromModel(value)))
        }
}
