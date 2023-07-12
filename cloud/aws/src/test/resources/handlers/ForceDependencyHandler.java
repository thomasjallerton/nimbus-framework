package handlers;

import com.nimbusframework.nimbuscore.annotations.http.HttpMethod;
import com.nimbusframework.nimbuscore.annotations.function.HttpServerlessFunction;
import com.nimbusframework.nimbuscore.clients.ClientBuilder;

public class ForceDependencyHandler {

    @HttpServerlessFunction(method = HttpMethod.POST, path = "test")
    public void func() {
        ClientBuilder.getEnvironmentVariableClient();
    }

}
