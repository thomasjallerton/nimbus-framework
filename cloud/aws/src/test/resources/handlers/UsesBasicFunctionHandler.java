package handlers;

import com.nimbusframework.nimbuscore.annotations.http.HttpMethod;
import com.nimbusframework.nimbuscore.annotations.function.HttpServerlessFunction;
import com.nimbusframework.nimbuscore.annotations.function.UsesBasicServerlessFunction;
import com.nimbusframework.nimbuscore.clients.ClientBuilder;

public class UsesBasicFunctionHandler {

    @HttpServerlessFunction(method = HttpMethod.POST, path = "test")
    @UsesBasicServerlessFunction(targetClass = BasicHandlers.class, methodName = "getCurrentTime")
    public void func() {
        ClientBuilder.getBasicServerlessFunctionClient(BasicHandlers.class, "getCurrentTime");
    }

    @HttpServerlessFunction(method = HttpMethod.POST, path = "test2")
    @UsesBasicServerlessFunction(targetClass = BasicHandlers.class, methodName = "not_valid")
    public void func2() {
        ClientBuilder.getBasicServerlessFunctionClient(BasicHandlers.class, "getCurrentTime");
    }

}
