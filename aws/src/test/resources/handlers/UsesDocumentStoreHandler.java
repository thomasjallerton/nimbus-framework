package handlers;

import com.nimbusframework.nimbuscore.annotations.document.UsesDocumentStore;
import com.nimbusframework.nimbuscore.annotations.function.HttpMethod;
import com.nimbusframework.nimbuscore.annotations.function.HttpServerlessFunction;
import com.nimbusframework.nimbuscore.clients.ClientBuilder;
import models.Document;

public class UsesDocumentStoreHandler {

  @HttpServerlessFunction(method = HttpMethod.POST, path = "test")
  @UsesDocumentStore(dataModel = Document.class)
  public void func() {
    ClientBuilder.getDocumentStoreClient(Document.class);
  }

}
