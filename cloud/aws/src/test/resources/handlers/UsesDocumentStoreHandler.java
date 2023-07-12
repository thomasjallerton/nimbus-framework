package handlers;

import com.nimbusframework.nimbuscore.annotations.document.UsesDocumentStore;
import com.nimbusframework.nimbuscore.annotations.http.HttpMethod;
import com.nimbusframework.nimbuscore.annotations.function.HttpServerlessFunction;
import com.nimbusframework.nimbuscore.clients.ClientBuilder;
import models.Document;
import models.KeyValue;

public class UsesDocumentStoreHandler {

  @HttpServerlessFunction(method = HttpMethod.POST, path = "test")
  @UsesDocumentStore(dataModel = Document.class)
  public void func() {
    ClientBuilder.getDocumentStoreClient(Document.class);
  }

  @HttpServerlessFunction(method = HttpMethod.POST, path = "test2")
  @UsesDocumentStore(dataModel = KeyValue.class)
  public void func2() {
    ClientBuilder.getDocumentStoreClient(KeyValue.class);
  }
}
