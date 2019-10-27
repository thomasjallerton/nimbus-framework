package handlers;

import com.nimbusframework.nimbuscore.annotations.function.DocumentStoreServerlessFunction;
import com.nimbusframework.nimbuscore.annotations.persistent.StoreEventType;
import com.nimbusframework.nimbuscore.eventabstractions.StoreEvent;
import models.Document;

public class BadDocumentStoreHandlers {

    @DocumentStoreServerlessFunction(dataModel = Document.class, method = StoreEventType.INSERT)
    public void handleInsert(StoreEvent event1, StoreEvent event2) {
        System.out.println("item was added!");
    }
}
