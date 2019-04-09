package com.nimbusframework.nimbuscore.testing.services.usesresources

import com.nimbusframework.nimbuscore.annotation.annotations.document.UsesDocumentStore
import com.nimbusframework.nimbuscore.testing.function.FunctionEnvironment
import com.nimbusframework.nimbuscore.testing.function.PermissionType
import com.nimbusframework.nimbuscore.testing.function.permissions.StorePermission
import com.nimbusframework.nimbuscore.testing.services.LocalResourceHolder
import java.lang.reflect.Method

class LocalUsesDocumentStoreHandler(
        localResourceHolder: LocalResourceHolder,
        private val stage: String
): LocalUsesResourcesHandler(localResourceHolder) {
    override fun handleUsesResources(clazz: Class<out Any>, method: Method, functionEnvironment: FunctionEnvironment) {
        val usesDocumentStores = method.getAnnotationsByType(UsesDocumentStore::class.java)

        for (usesDocumentStore in usesDocumentStores) {
            if (usesDocumentStore.stages.contains(stage)) {
                functionEnvironment.addPermission(PermissionType.DOCUMENT_STORE, StorePermission(usesDocumentStore.dataModel.qualifiedName.toString()))
            }
        }
    }
}