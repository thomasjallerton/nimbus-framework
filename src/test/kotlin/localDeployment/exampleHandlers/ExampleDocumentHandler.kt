package localDeployment.exampleHandlers

import com.nimbusframework.nimbuscore.annotation.annotations.function.DocumentStoreServerlessFunction
import com.nimbusframework.nimbuscore.annotation.annotations.persistent.StoreEventType
import localDeployment.exampleModels.Document

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