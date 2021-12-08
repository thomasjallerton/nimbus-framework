package handlers;

import com.nimbusframework.nimbuscore.annotations.deployment.ForceDependency;
import com.nimbusframework.nimbuscore.annotations.function.HttpMethod;
import com.nimbusframework.nimbuscore.annotations.function.HttpServerlessFunction;
import com.nimbusframework.nimbuscore.clients.ClientBuilder;

public class ForceDependencyHandler {

    @HttpServerlessFunction(method = HttpMethod.POST, path = "test")
    @ForceDependency(classPaths = {"com.test.test", "com.example.test"})
    public void func() {
        ClientBuilder.getEnvironmentVariableClient();
    }

}
