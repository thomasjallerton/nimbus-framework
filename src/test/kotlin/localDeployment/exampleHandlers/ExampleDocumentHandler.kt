package localDeployment.exampleHandlers

import annotation.annotations.function.DocumentStoreServerlessFunction
import annotation.annotations.persistent.StoreUpdate
import localDeployment.exampleModels.Document

class ExampleDocumentHandler {

    @DocumentStoreServerlessFunction(dataModel = Document::class, method = StoreUpdate.INSERT)
    fun handleInsert(newDocument: Document): Boolean {
        return true
    }

    @DocumentStoreServerlessFunction(dataModel = Document::class, method = StoreUpdate.MODIFY)
    fun handleModify(oldDocument: Document, newDocument: Document): Boolean {
        return true
    }

    @DocumentStoreServerlessFunction(dataModel = Document::class, method = StoreUpdate.REMOVE)
    fun handleRemove(oldDocument: Document): Boolean {
        return true
    }
}