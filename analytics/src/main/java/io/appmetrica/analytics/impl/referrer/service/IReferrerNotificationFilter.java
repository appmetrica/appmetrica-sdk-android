package io.appmetrica.analytics.impl.referrer.service;

import androidx.annotation.Nullable;
import io.appmetrica.analytics.impl.referrer.common.ReferrerInfo;

public interface IReferrerNotificationFilter {

    boolean shouldNotify(@Nullable ReferrerInfo referrerInfo);
}
