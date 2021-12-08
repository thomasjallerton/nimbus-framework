package com.nimbusframework.nimbuslocal.exampleHandlers

import com.nimbusframework.nimbuscore.annotations.function.DocumentStoreServerlessFunction
import com.nimbusframework.nimbuscore.annotations.persistent.StoreEventType
import com.nimbusframework.nimbuslocal.exampleModels.Document

class ExampleDocumentHandler {

    @DocumentStoreServerlessFunction(dataModel = Document::class, method = StoreEventType.INSERT)
    fun handleInsert(newDocument: Document): Boolean {
        return true
    }

    @DocumentStoreServerlessFunction(dataModel = Document::class, method = StoreEventType.MODIFY)
    fun handleModify(oldDocument: Document, newDocument: Document): Boolean {
        return true
    }

    @DocumentStoreServerlessFunction(dataModel = Document::class, method = StoreEventType.REMOVE)
    fun handleRemove(oldDocument: Document): Boolean {
        return true
    }
}