package clients.function

import testing.LocalNimbusDeployment

internal class BasicServerlessFunctionClientLocal: BasicServerlessFunctionClient {

    private val localDeployment = LocalNimbusDeployment.getInstance()

    override fun invoke(handlerClass: Class<out Any>, functionName: String) {
        val method = localDeployment.getBasicMethod(handlerClass, functionName)
        method.invoke("", Unit.javaClass)
    }

    override fun invoke(handlerClass: Class<out Any>, functionName: String, param: Any) {
        val method = localDeployment.getBasicMethod(handlerClass, functionName)
        method.invoke(param, Unit.javaClass)
    }

    override fun <T> invoke(handlerClass: Class<out Any>, functionName: String, param: Any, responseType: Class<T>): T? {
        val method = localDeployment.getBasicMethod(handlerClass, functionName)
        return method.invoke(param, responseType)
    }

    override fun invokeAsync(handlerClass: Class<out Any>, functionName: String) {
        val method = localDeployment.getBasicMethod(handlerClass, functionName)
        method.invoke("", Unit.javaClass)
    }

    override fun invokeAsync(handlerClass: Class<out Any>, functionName: String, param: Any) {
        val method = localDeployment.getBasicMethod(handlerClass, functionName)
        method.invoke(param, Unit.javaClass)
    }
}