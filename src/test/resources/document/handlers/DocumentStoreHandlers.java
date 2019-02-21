package document.handlers;

import annotation.annotations.function.DocumentStoreServerlessFunction;
import annotation.annotations.persistent.StoreUpdate;
import document.models.Document;
import wrappers.document.models.StoreEvent;

public class DocumentStoreHandlers {

    @DocumentStoreServerlessFunction(dataModel = Document.class, method = StoreUpdate.INSERT)
    public void handleInsert0() {
        System.out.println("item was added!");
    }

    @DocumentStoreServerlessFunction(dataModel = Document.class, method = StoreUpdate.INSERT)
    public void handleInsert1(Document newEvent) {
        System.out.println("This item was added! " + newEvent);
    }

    @DocumentStoreServerlessFunction(dataModel = Document.class, method = StoreUpdate.INSERT)
    public void handleInsert2(Document newEvent, StoreEvent storeEvent) {
        System.out.println( "This item was added! " + newEvent);
    }

    @DocumentStoreServerlessFunction(dataModel = Document.class, method = StoreUpdate.INSERT)
    public void handleInsert3(StoreEvent storeEvent, Document newEvent) {
        System.out.println("This item was added! " + newEvent);
    }

    @DocumentStoreServerlessFunction(dataModel = Document.class, method = StoreUpdate.MODIFY)
    public void handleModify0() {
        System.out.println("Item was changed");
    }

    @DocumentStoreServerlessFunction(dataModel = Document.class, method = StoreUpdate.MODIFY)
    public void handleModify1(Document newEvent) {
        System.out.println("This item was changed to " + newEvent);
    }

    @DocumentStoreServerlessFunction(dataModel = Document.class, method = StoreUpdate.MODIFY)
    public void handleModify2(Document newEvent, StoreEvent storeEvent) {
        System.out.println("This item was changed to " + newEvent);
    }

    @DocumentStoreServerlessFunction(dataModel = Document.class, method = StoreUpdate.MODIFY)
    public void handleModify3(StoreEvent storeEvent, Document newEvent) {
        System.out.println("This item was changed to " + newEvent);
    }

    @DocumentStoreServerlessFunction(dataModel = Document.class, method = StoreUpdate.MODIFY)
    public void handleModify4(Document oldEvent, Document newEvent) {
        System.out.println("This item was changed from " + oldEvent + " to " + newEvent);
    }

    @DocumentStoreServerlessFunction(dataModel = Document.class, method = StoreUpdate.MODIFY)
    public void handleModify5(Document oldEvent, Document newEvent, StoreEvent storeEvent) {
        System.out.println("This item was changed from " + oldEvent + " to " + newEvent);
    }

    @DocumentStoreServerlessFunction(dataModel = Document.class, method = StoreUpdate.REMOVE)
    public void handleRemove0() {
        System.out.println("Item was removed!");
    }

    @DocumentStoreServerlessFunction(dataModel = Document.class, method = StoreUpdate.REMOVE)
    public void handleRemove1(Document oldEvent) {
        System.out.println("This item was removed! " + oldEvent);
    }

    @DocumentStoreServerlessFunction(dataModel = Document.class, method = StoreUpdate.REMOVE)
    public void handleRemove2(Document oldEvent, StoreEvent storeEvent) {
        System.out.println("This item was removed! " + oldEvent);
    }

    @DocumentStoreServerlessFunction(dataModel = Document.class, method = StoreUpdate.REMOVE)
    public void handleRemove3(StoreEvent storeEvent, Document oldEvent) {
        System.out.println("This item was removed! " + oldEvent);
    }
}