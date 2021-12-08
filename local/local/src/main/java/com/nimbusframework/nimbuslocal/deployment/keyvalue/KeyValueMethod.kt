package com.nimbusframework.nimbuslocal.deployment.keyvalue

import com.nimbusframework.nimbuscore.annotations.persistent.StoreEventType
import com.nimbusframework.nimbuscore.eventabstractions.StoreEvent
import com.nimbusframework.nimbuslocal.ServerlessMethod
import com.nimbusframework.nimbuslocal.deployment.function.FunctionType
import com.nimbusframework.nimbuslocal.deployment.store.StoreMethod
import java.lang.reflect.Method

class KeyValueMethod(
        method: Method,
        invokeOn: Any,
        storeEventType: StoreEventType
) : StoreMethod(method, invokeOn, storeEventType, FunctionType.KEY_VALUE_STORE)