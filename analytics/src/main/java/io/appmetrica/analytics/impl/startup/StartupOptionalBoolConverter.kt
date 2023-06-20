package io.appmetrica.analytics.impl.startup

import io.appmetrica.analytics.impl.OptionalBoolConverter
import io.appmetrica.analytics.impl.protobuf.client.StartupStateProtobuf

internal class StartupOptionalBoolConverter : OptionalBoolConverter(
    StartupStateProtobuf.StartupState.OPTIONAL_BOOL_UNDEFINED,
    StartupStateProtobuf.StartupState.OPTIONAL_BOOL_FALSE,
    StartupStateProtobuf.StartupState.OPTIONAL_BOOL_TRUE,
)
