package handlers;

import com.nimbusframework.nimbuscore.annotations.function.KeyValueStoreServerlessFunction;
import com.nimbusframework.nimbuscore.annotations.persistent.StoreEventType;
import com.nimbusframework.nimbuscore.eventabstractions.StoreEvent;
import models.KeyValue;

public class KeyValueStoreHandlers {

    @KeyValueStoreServerlessFunction(dataModel = KeyValue.class, method = StoreEventType.INSERT)
    public void handleInsert0() {
        System.out.println("item was added!");
    }

    @KeyValueStoreServerlessFunction(dataModel = KeyValue.class, method = StoreEventType.INSERT)
    public void handleInsert1(KeyValue newEvent) {
        System.out.println("This item was added! " + newEvent);
    }

    @KeyValueStoreServerlessFunction(dataModel = KeyValue.class, method = StoreEventType.INSERT)
    public void handleInsert2(KeyValue newEvent, StoreEvent storeEvent) {
        System.out.println( "This item was added! " + newEvent);
    }

    @KeyValueStoreServerlessFunction(dataModel = KeyValue.class, method = StoreEventType.INSERT)
    public void handleInsert3(StoreEvent storeEvent, KeyValue newEvent) {
        System.out.println("This item was added! " + newEvent);
    }

    @KeyValueStoreServerlessFunction(dataModel = KeyValue.class, method = StoreEventType.MODIFY)
    public void handleModify0() {
        System.out.println("Item was changed");
    }

    @KeyValueStoreServerlessFunction(dataModel = KeyValue.class, method = StoreEventType.MODIFY)
    public void handleModify1(KeyValue newEvent) {
        System.out.println("This item was changed to " + newEvent);
    }

    @KeyValueStoreServerlessFunction(dataModel = KeyValue.class, method = StoreEventType.MODIFY)
    public void handleModify2(KeyValue newEvent, StoreEvent storeEvent) {
        System.out.println("This item was changed to " + newEvent);
    }

    @KeyValueStoreServerlessFunction(dataModel = KeyValue.class, method = StoreEventType.MODIFY)
    public void handleModify3(StoreEvent storeEvent, KeyValue newEvent) {
        System.out.println("This item was changed to " + newEvent);
    }

    @KeyValueStoreServerlessFunction(dataModel = KeyValue.class, method = StoreEventType.MODIFY)
    public void handleModify4(KeyValue oldEvent, KeyValue newEvent) {
        System.out.println("This item was changed from " + oldEvent + " to " + newEvent);
    }

    @KeyValueStoreServerlessFunction(dataModel = KeyValue.class, method = StoreEventType.MODIFY)
    public void handleModify5(KeyValue oldEvent, KeyValue newEvent, StoreEvent storeEvent) {
        System.out.println("This item was changed from " + oldEvent + " to " + newEvent);
    }

    @KeyValueStoreServerlessFunction(dataModel = KeyValue.class, method = StoreEventType.REMOVE)
    public void handleRemove0() {
        System.out.println("Item was removed!");
    }

    @KeyValueStoreServerlessFunction(dataModel = KeyValue.class, method = StoreEventType.REMOVE)
    public void handleRemove1(KeyValue oldEvent) {
        System.out.println("This item was removed! " + oldEvent);
    }

    @KeyValueStoreServerlessFunction(dataModel = KeyValue.class, method = StoreEventType.REMOVE)
    public void handleRemove2(KeyValue oldEvent, StoreEvent storeEvent) {
        System.out.println("This item was removed! " + oldEvent);
    }

    @KeyValueStoreServerlessFunction(dataModel = KeyValue.class, method = StoreEventType.REMOVE)
    public void handleRemove3(StoreEvent storeEvent, KeyValue oldEvent) {
        System.out.println("This item was removed! " + oldEvent);
    }
}