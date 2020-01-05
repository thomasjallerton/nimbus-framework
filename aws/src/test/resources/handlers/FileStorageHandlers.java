package handlers;

import com.nimbusframework.nimbuscore.annotations.file.FileStorageEventType;
import com.nimbusframework.nimbuscore.annotations.function.FileStorageServerlessFunction;
import com.nimbusframework.nimbuscore.eventabstractions.FileStorageEvent;
import models.FileStorage;

public class FileStorageHandlers {

  @FileStorageServerlessFunction(fileStorageBucket = FileStorage.class, eventType = FileStorageEventType.OBJECT_CREATED)
  public void newObject(FileStorageEvent event) {
    System.out.println("New file added: " + event.getKey() + " with size " + event.getSize() + " bytes");
  }

}
