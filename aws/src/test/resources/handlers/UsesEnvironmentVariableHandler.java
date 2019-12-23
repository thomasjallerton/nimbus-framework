package handlers;

import com.nimbusframework.nimbuscore.annotations.function.EnvironmentVariable;
import com.nimbusframework.nimbuscore.annotations.function.HttpMethod;
import com.nimbusframework.nimbuscore.annotations.function.HttpServerlessFunction;
import com.nimbusframework.nimbuscore.clients.ClientBuilder;

public class UsesEnvironmentVariableHandler {

    @HttpServerlessFunction(method = HttpMethod.POST, path = "test")
    @EnvironmentVariable(key = "TEST_KEY", value = "TEST_VALUE")
    public void func() {
        ClientBuilder.getEnvironmentVariableClient();
    }

    @HttpServerlessFunction(method = HttpMethod.POST, path = "test2")
    @EnvironmentVariable(key = "TEST_KEY", value = "${TEST_VALUE}")
    public void func2() {
        ClientBuilder.getEnvironmentVariableClient();
    }

}
