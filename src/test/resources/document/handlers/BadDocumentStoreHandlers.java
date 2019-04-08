package document.handlers;

import com.nimbusframework.nimbuscore.annotation.annotations.function.DocumentStoreServerlessFunction;
import com.nimbusframework.nimbuscore.annotation.annotations.persistent.StoreEventType;
import document.models.Document;
import com.nimbusframework.nimbuscore.wrappers.store.models.StoreEvent;

public class BadDocumentStoreHandlers {

    @DocumentStoreServerlessFunction(dataModel = Document.class, method = StoreEventType.INSERT)
    public void handleInsert(StoreEvent event1, StoreEvent event2) {
        System.out.println("item was added!");
    }
}
