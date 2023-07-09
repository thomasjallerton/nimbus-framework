package handlers;

import com.nimbusframework.nimbuscore.annotations.file.UsesFileStorageBucket;
import com.nimbusframework.nimbuscore.annotations.http.HttpMethod;
import com.nimbusframework.nimbuscore.annotations.function.HttpServerlessFunction;
import com.nimbusframework.nimbuscore.clients.ClientBuilder;
import models.FileStorage;

public class UsesFileStorageClientHandler {

    @HttpServerlessFunction(method = HttpMethod.POST, path = "test")
    @UsesFileStorageBucket(fileStorageBucket = FileStorage.class)
    public void func() {
        ClientBuilder.getFileStorageClient(FileStorage.class);
    }

}
