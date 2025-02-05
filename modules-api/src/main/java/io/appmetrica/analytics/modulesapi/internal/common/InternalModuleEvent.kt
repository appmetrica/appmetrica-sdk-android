package io.appmetrica.analytics.modulesapi.internal.common

import io.appmetrica.analytics.coreutils.internal.collection.CollectionUtils

/**
 * Custom event parameters.
 */
class InternalModuleEvent private constructor(builder: Builder) {

    /**
     * @return event type
     */
    val type: Int = builder.type

    /**
     * @return event name
     */
    val name: String? = builder.name

    /**
     * @return event value
     */
    val value: String? = builder.value

    /**
     * @return the way this event will be tracked
     */
    val serviceDataReporterType: Int? = builder.serviceDataReporterType

    private val environment: List<Map.Entry<String, Any>>? = CollectionUtils.getListFromMap(builder.environment)
    private val extras: List<Map.Entry<String, ByteArray>>? = CollectionUtils.getListFromMap(builder.extras)
    private val attributes: List<Map.Entry<String, Any>>? = CollectionUtils.getListFromMap(builder.attributes)

    /**
     * @return event environment
     */
    fun getEnvironment(): Map<String, Any>? {
        return CollectionUtils.getMapFromListOrNull(environment)
    }

    /**
     * @return event extras
     */
    fun getExtras(): Map<String, ByteArray>? {
        return CollectionUtils.getMapFromListOrNull(extras)
    }

    /**
     * @return event attributes
     */
    fun getAttributes(): Map<String, Any>? {
        return CollectionUtils.getMapFromListOrNull(attributes)
    }

    override fun toString(): String {
        return "ModuleEvent{" +
            "type=" + type +
            ", name='" + name + '\'' +
            ", value='" + value + '\'' +
            ", serviceDataReporterType=" + serviceDataReporterType +
            ", environment=" + environment +
            ", extras=" + extras +
            ", attributes=" + attributes +
            '}'
    }

    /**
     * Builds a new [ModuleEvent] object.
     */
    class Builder(internal val type: Int) {
        var name: String? = null
        var value: String? = null
        var serviceDataReporterType: Int? = null
        var environment: Map<String, Any>? = null
        var extras: Map<String, ByteArray>? = null
        var attributes: Map<String, Any>? = null

        /**
         * Sets event name.
         *
         * @param name [String] value of event name
         * @return same [Builder] object
         */
        fun withName(name: String?): Builder {
            this.name = name
            return this
        }

        /**
         * Sets event value. Can be replaced with [ModuleEvent.attributes]
         * if [ModuleEvent.attributes] is not null or empty.
         *
         * @param value [String] value of event value
         * @return same [Builder] object
         */
        fun withValue(value: String?): Builder {
            this.value = value
            return this
        }

        /**
         * Sets the way event is processed.
         *
         * @param serviceDataReporterType type of [ServiceDataReporter]
         * @return same [Builder] object
         */
        fun withServiceDataReporterType(serviceDataReporterType: Int): Builder {
            this.serviceDataReporterType = serviceDataReporterType
            return this
        }

        /**
         * Sets event environment.
         *
         * @param environment map with environment keys and values
         * @return same [Builder] object
         */
        fun withEnvironment(environment: Map<String, Any>?): Builder {
            if (environment != null) {
                this.environment = HashMap(environment)
            }
            return this
        }

        /**
         * Sets event extras.
         *
         * @param extras map with extras keys and values
         * @return same [Builder] object
         */
        fun withExtras(extras: Map<String, ByteArray>?): Builder {
            if (extras != null) {
                this.extras = HashMap(extras)
            }
            return this
        }

        /**
         * Sets event attributes. It will replace [ModuleEvent.value] if not null or empty.
         *
         * @param attributes map with attributes keys and values
         * @return same [Builder] object
         */
        fun withAttributes(attributes: Map<String, Any>?): Builder {
            if (attributes != null) {
                this.attributes = HashMap(attributes)
            }
            return this
        }

        /**
         * Creates instance of [ModuleEvent].
         *
         * @return [ModuleEvent] object
         */
        open fun build(): InternalModuleEvent {
            return InternalModuleEvent(this)
        }
    }

    companion object {
        /**
         * Creates new instance of [Builder].
         *
         * @param type event type
         * @return instance of [Builder]
         */
        @JvmStatic
        fun newBuilder(type: Int): Builder {
            return Builder(type)
        }
    }
}
