package io.appmetrica.analytics.testutils.ktlint.rules

import com.pinterest.ktlint.core.RuleSet
import com.pinterest.ktlint.core.RuleSetProvider

class AppMetricaRuleSetProvider : RuleSetProvider {

    override fun get() = RuleSet(
        "appmetrica-rules",
        NoCaptorAnnotationRule(),
        NoMockAnnotationRule(),
        NoMockitoAnnotationsRule(),
        NoMockitoWhenRule(),
        NoTestPrefixRule(),
        TestInheritanceRule(),
    )
}
