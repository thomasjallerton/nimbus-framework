package com.nimbusframework.nimbuslocal.clients

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

internal class LocalInternalClientBuilderTest: StringSpec({

    "Correct is local" {
        LocalInternalClientBuilder.isLocal() shouldBe true
    }

})
