package handlers;

import com.nimbusframework.nimbuscore.annotations.file.UsesFileStorage;
import com.nimbusframework.nimbuscore.annotations.function.HttpMethod;
import com.nimbusframework.nimbuscore.annotations.function.HttpServerlessFunction;
import com.nimbusframework.nimbuscore.clients.ClientBuilder;

public class UsesFileStorageClientHandler {

    @HttpServerlessFunction(method = HttpMethod.POST, path = "test")
    @UsesFileStorage(bucketName = "Test")
    public void func() {
        ClientBuilder.getFileStorageClient("Test");
    }

}
