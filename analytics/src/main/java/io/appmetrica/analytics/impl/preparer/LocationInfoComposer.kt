package io.appmetrica.analytics.impl.preparer

import io.appmetrica.analytics.impl.db.event.DbLocationModel
import io.appmetrica.analytics.impl.protobuf.backend.EventProto

interface LocationInfoComposer {

    fun getLocation(locationInfoFromDb: DbLocationModel?): EventProto.ReportMessage.Location?
}
