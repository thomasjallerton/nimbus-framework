package document.handlers;

import annotation.annotations.function.DocumentStoreServerlessFunction;
import annotation.annotations.persistent.StoreUpdate;
import document.models.Document;
import wrappers.store.models.StoreEvent;

public class BadDocumentStoreHandlers {

    @DocumentStoreServerlessFunction(dataModel = Document.class, method = StoreUpdate.INSERT)
    public void handleInsert(StoreEvent event1, StoreEvent event2) {
        System.out.println("item was added!");
    }
}
