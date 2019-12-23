package handlers;

import com.nimbusframework.nimbuscore.annotations.function.HttpMethod;
import com.nimbusframework.nimbuscore.annotations.function.HttpServerlessFunction;
import com.nimbusframework.nimbuscore.annotations.keyvalue.UsesKeyValueStore;
import com.nimbusframework.nimbuscore.clients.ClientBuilder;
import models.Document;
import models.KeyValue;

public class UsesKeyValueStoreHandler {

  @HttpServerlessFunction(method = HttpMethod.POST, path = "test")
  @UsesKeyValueStore(dataModel = KeyValue.class)
  public void func() {
    ClientBuilder.getDocumentStoreClient(KeyValue.class);
  }

  @HttpServerlessFunction(method = HttpMethod.POST, path = "test2")
  @UsesKeyValueStore(dataModel = Document.class)
  public void func2() {
    ClientBuilder.getDocumentStoreClient(KeyValue.class);
  }

}
