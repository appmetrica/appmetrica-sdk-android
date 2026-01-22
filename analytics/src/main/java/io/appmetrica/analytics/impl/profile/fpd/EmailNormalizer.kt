package io.appmetrica.analytics.impl.profile.fpd

// see https://nda.ya.ru/t/iN7J7tXE7Mpbnu and https://nda.ya.ru/t/omArXm6n7NDTjp
internal class EmailNormalizer : AttributeValueNormalizer {

    private val minEmailLength = 5
    private val maxEmailLength = 100
    private val maxDomainSize = 255
    private val minDomainLevel = 2
    private val maxDomainLabelSize = 63
    private val minDomainLabelSize = 1
    private val minDomainTLDLabelSize = 2
    private val minLocalPartSize = 1
    private val maxLocalPartSize = 64
    private val emailLocalPartRegex = "^[a-zA-Z0-9'!#$%&*+-/=?^_`{|}~]+$".toRegex()
    private val yandexDomainRegex = "(?:^|\\.)(?:(ya\\.ru)|(?:yandex)\\.(\\w+|com?\\.\\w+))$".toRegex()
    private val yandexWhiteListTLD = listOf(
        "ru",
        "by",
        "kz",
        "az",
        "kg",
        "lv",
        "md",
        "tj",
        "tm",
        "uz",
        "ee",
        "fr",
        "lt",
        "com",
        "co.il",
        "com.ge",
        "com.am",
        "com.tr",
        "com.ru"
    )
    private val yandexRuDomain = "yandex.ru"

    private val gmailDomain = "gmail.com"
    private val googleEmailDomain = "googlemail.com"

    override fun normalize(value: String): String? {
        val email = value
            .trim()
            .replace("^\\++".toRegex(), "")
            .lowercase()
        val atIndex = email.lastIndexOf('@')
        if (atIndex == -1) {
            return null
        }
        var local = email.substring(0, atIndex)
        var domain = email.substring(atIndex + 1)

        if (!validateEmail(local, domain)) {
            return null
        }

        domain = domain.replace(googleEmailDomain, gmailDomain)
        if (isYandexSearchDomain(domain)) {
            domain = yandexRuDomain
        }

        if (domain == yandexRuDomain) {
            local = local.replace(".", "-")
        } else if (domain == gmailDomain) {
            local = local.replace(".", "")
        }

        local = local.takeWhile { it != '+' }

        return checkEmailLength("$local@$domain")
    }

    private fun checkEmailLength(email: String): String? {
        return if (email.length < minEmailLength || email.length > maxEmailLength) {
            null
        } else {
            email
        }
    }

    private fun validateEmail(local: String, domain: String): Boolean {
        return validateLocalPart(local) && validateDomain(domain)
    }

    private fun validateLocalPart(local: String): Boolean {
        val localLength = local.length

        if (localLength < minLocalPartSize || localLength > maxLocalPartSize) {
            return false
        }

        return local.split('.').all { part ->
            val partLength = part.length
            if (partLength < minLocalPartSize) {
                return false
            }
            if (part.firstOrNull() == '"' && part.lastOrNull() == '"' && partLength > 2) {
                return validateLocalQuoted(part)
            }
            if (!emailLocalPartRegex.matches(part)) {
                return false
            }
            true
        }
    }

    private fun validateLocalQuoted(part: String): Boolean {
        var position = 1
        while (position + 2 < part.length) {
            val charCode = part[position].code
            if (charCode < 32 || charCode == 34 || charCode > 126) {
                return false
            }
            if (charCode == 92) {
                if (position + 2 == part.length) {
                    return false
                }
                if (part[position + 1].code < 32) {
                    return false
                }
                position += 1
            }
            position += 1
        }
        return true
    }

    private fun isYandexSearchDomain(host: String): Boolean {
        val match = yandexDomainRegex.find(host)
        if (match != null) {
            val (_, matchedYaRu, matchedYandexTld) = match.groupValues

            if (matchedYandexTld.isNotEmpty()) {
                return yandexWhiteListTLD.contains(matchedYandexTld)
            }

            if (matchedYaRu.isNotEmpty()) {
                return true
            }
        }
        return false
    }

    private fun verifyLabel(label: String): Boolean {
        if (label.length > maxDomainLabelSize || label.length < minDomainLabelSize) {
            return false
        }

        if (!label.first().isLetterOrDigit() || !label.last().isLetterOrDigit()) {
            return false
        }

        return label.all { it.isLetterOrDigit() || it == '-' }
    }

    private fun verifyTld(tld: String): Boolean {
        return tld.length >= minDomainTLDLabelSize &&
            verifyLabel(tld) &&
            !tld.all { it.isDigit() }
    }

    private fun validateDomain(domain: String): Boolean {
        if (domain.length > maxDomainSize) {
            return false
        }

        val labels = domain.split(".")

        if (labels.size < minDomainLevel) {
            return false
        }

        return labels.dropLast(1).all { verifyLabel(it) } && verifyTld(labels.last())
    }
}
