package com.nimbusframework.nimbuslocal.deployment.store

import com.nimbusframework.nimbuscore.clients.store.ReadItemRequest

class ReadItemRequestLocal<T>(val executeRead: () -> T?): ReadItemRequest<T>()