package annotation.models.resource

import annotation.models.persisted.NimbusState
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

class FunctionResource(
        private val handler: String,
        private val name: String,
        nimbusState: NimbusState
) : Resource(nimbusState) {

    private val timeout: Int = 5
    private val memory: Int = 1024

    override fun getName(): String {
        return "${name}Function"
    }

    override fun toCloudFormation(): JSONObject {
        val functionResource = JSONObject()
        functionResource.put("Type", "AWS::Lambda::Function")

        val properties = JSONObject()
        val code = JSONObject()
        val s3Bucket = JSONObject()
        s3Bucket.put("Ref", "NimbusDeploymentBucket")

        code.put("S3Bucket", s3Bucket)
        code.put("S3Key", "nimbus/${nimbusState.projectName}/${nimbusState.compilationTimeStamp}/lambdacode")

        properties.put("Code", code)
        properties.put("FunctionName", name)
        properties.put("Handler", handler)
        properties.put("MemorySize", memory)

        val role = JSONObject()
        val roleFunc = JSONArray()
        roleFunc.put("IamRoleLambdaExecution")
        roleFunc.put("Arn")
        role.put("Fn::GetAtt", roleFunc)
        properties.put("Role", role)

        properties.put("Runtime", "java8")
        properties.put("Timeout", timeout)

        val dependsOn = JSONArray()
        dependsOn.put("IamRoleLambdaExecution")
        functionResource.put("DependsOn", dependsOn)

        functionResource.put("Properties", properties)

        return functionResource
    }

}

//"MessageQueueInsertToDynamoLambdaFunction": {
//    "Type": "AWS::Lambda::Function",
//    "Properties": {
//        "Code": {
//          "S3Bucket": {
//              "Ref": "ServerlessDeploymentBucket"
//           },
//           "S3Key": "serverless/serverless-framework-test/dev/1543339335225-2018-11-27T17:22:15.225Z/target/serverless-test.jar"
//        },
//        "FunctionName": "serverless-framework-test-dev-messageQueueInsertToDynamo",
//        "Handler": "handlers.MessageQueueHandler::handle",
//        "MemorySize": 1024,
//        "Role": {
//          "Fn::GetAtt": [
//              "IamRoleLambdaExecution",
//              "Arn"
//          ]
//        },
//        "Runtime": "java8",
//        "Timeout": 6
//    },
//    "DependsOn": [
//      "MessageQueueInsertToDynamoLogGroup",
//      "IamRoleLambdaExecution"
//    ]
//}