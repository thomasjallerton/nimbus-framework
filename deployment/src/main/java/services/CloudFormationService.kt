package services

import configuration.AWS_STACK_CREATE_FILE
import org.apache.maven.plugin.MojoFailureException
import org.apache.maven.plugin.logging.Log
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.cloudformation.CloudFormationClient
import software.amazon.awssdk.services.cloudformation.model.*
import java.net.URI

class CloudFormationService(private val logger: Log, region: Region) : StackService {
    private val client: CloudFormationClient = CloudFormationClient.builder()
        .region(region)
        .build()

    private val fileService: FileService = FileService(logger)

    override fun findExport(exportName: String): StackService.FindExportResponse {
        try {
            val exportsResults = client.listExports()
            val exports = exportsResults.exports()

            for (export in exports) {
                if (export.name() == exportName) {
                    return StackService.FindExportResponse(true, export.value())
                }
            }

        } catch (e: InterruptedException) {
            logger.error(e.localizedMessage)
        } catch (e: Exception) {
            logger.error(e.localizedMessage)
        }
        return StackService.FindExportResponse(false, "")
    }

    override fun updateStack(projectName: String, uri: URI): Boolean {
        val updateStackRequest = UpdateStackRequest.builder()
            .stackName(projectName)
            .capabilities(Capability.CAPABILITY_NAMED_IAM)
            .templateURL(uri.toURL().toString())
            .build()
        return try {
            client.updateStack(updateStackRequest)
            true
        } catch (e: Exception) {
            if (e.message!!.contains("No updates are to be performed")) return true
            logger.error(e.localizedMessage)
            false
        }
    }

    override fun createStack(projectName: String, stage: String, compiledSourcesPath: String): StackService.CreateStackResponse {
        val templateText = fileService.getFileText("$compiledSourcesPath$AWS_STACK_CREATE_FILE-$stage.json")
        val createStackRequest = CreateStackRequest.builder()
            .stackName(projectName)
            .capabilities(Capability.CAPABILITY_NAMED_IAM)
            .templateBody(templateText)
            .build()
        try {
            client.createStack(createStackRequest)
            return StackService.CreateStackResponse(true, false)
        } catch (e: AlreadyExistsException) {
            return StackService.CreateStackResponse(true, true)
        } catch (e: java.lang.Exception) {
            logger.error(e.localizedMessage)
        }
        return StackService.CreateStackResponse(false, false)
    }

    override fun deleteStack(stackName: String): Boolean {
        val deleteStackRequest = DeleteStackRequest.builder()
            .stackName(stackName)
            .build()

        return try {
            client.deleteStack(deleteStackRequest)
            true
        } catch (e: java.lang.Exception) {
            logger.error(e.localizedMessage)
            false
        }
    }

    override fun getStackStatus(stackName: String): String{
        val describeStackRequest = DescribeStacksRequest.builder()
            .stackName(stackName)
            .build()
        try {
            val response = client.describeStacks(describeStackRequest)
            val stacks = response.stacks()

            if (stacks.size == 1) {
                return stacks[0].stackStatus().name
            }
        } catch (e: java.lang.Exception) {
            if (!e.localizedMessage.contains("does not exist")) {
                logger.error(e.localizedMessage)
            }
        }
        return "STACK_NOT_FOUND"
    }

    override fun getStackErrorReason(stackName: String): String {
        val describeStackRequest = DescribeStackEventsRequest.builder()
            .stackName(stackName)
            .build()

        val response = client.describeStackEvents(describeStackRequest)
        val events = response.stackEvents()

        for (event in events) {
            if (isErrorStatus(event.resourceStatus().name)
                && !event.resourceStatusReason().contains("Resource update cancelled")
                && !event.resourceStatusReason().contains("Resource creation cancelled")
            ) {
                return event.resourceStatusReason()
            }
        }
        return "Couldn't find error, look at CloudFormation stack log"
    }

    override fun isErrorStatus(stackStatus: String): Boolean {
        return when (stackStatus) {
            "CREATE_FAILED" -> true
            "DELETE_FAILED" -> true
            "ROLLBACK_FAILED" -> true
            "UPDATE_ROLLBACK_FAILED" -> true
            "UPDATE_FAILED" -> true
            else -> false
        }
    }

    override fun canContinue(stackStatus: String): StackService.ContinueResponse {
        return when (stackStatus) {
            "CREATE_COMPLETE" -> StackService.ContinueResponse(true, false)
            "CREATE_FAILED" -> StackService.ContinueResponse(true, true)
            "CREATE_IN_PROGRESS" -> StackService.ContinueResponse(false, false)
            "DELETE_COMPLETE" -> StackService.ContinueResponse(true, false)
            "DELETE_FAILED" -> StackService.ContinueResponse(true, true)
            "DELETE_IN_PROGRESS" -> StackService.ContinueResponse(false, false)
            "REVIEW_IN_PROGRESS" -> StackService.ContinueResponse(false, false)
            "ROLLBACK_COMPLETE" -> StackService.ContinueResponse(true, false)
            "ROLLBACK_FAILED" -> StackService.ContinueResponse(true, true)
            "ROLLBACK_IN_PROGRESS" -> StackService.ContinueResponse(false, false)
            "UPDATE_COMPLETE" -> StackService.ContinueResponse(true, false)
            "UPDATE_COMPLETE_CLEANUP_IN_PROGRESS" -> StackService.ContinueResponse(false, false)
            "UPDATE_IN_PROGRESS" -> StackService.ContinueResponse(false, false)
            "UPDATE_ROLLBACK_COMPLETE" -> StackService.ContinueResponse(true, true)
            "UPDATE_ROLLBACK_COMPLETE_CLEANUP_IN_PROGRESS" -> StackService.ContinueResponse(false, false)
            "UPDATE_ROLLBACK_FAILED" -> StackService.ContinueResponse(true, true)
            "UPDATE_ROLLBACK_IN_PROGRESS" -> StackService.ContinueResponse(false, false)
            "STACK_NOT_FOUND" -> StackService.ContinueResponse(true, false)
            else -> StackService.ContinueResponse(false, false)
        }
    }

    @Throws(MojoFailureException::class)
    override fun pollStackStatus(projectName: String, count: Int) {
        val status = getStackStatus(projectName)
        val continueResponse = canContinue(status)

        if (continueResponse.canContinue) {
            if (continueResponse.needErrorMessage) {
                val errorMessage = getStackErrorReason(projectName)
                throw MojoFailureException(errorMessage)
            }
            println()
        } else {
            try {
                Thread.sleep(2000)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }

            if (count < 50) {
                print("*")
                pollStackStatus(projectName, count + 1)
            } else {
                println("*")
                pollStackStatus(projectName, 0)
            }
        }
    }
}
