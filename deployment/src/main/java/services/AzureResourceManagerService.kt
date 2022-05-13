package services

import com.microsoft.azure.management.Azure
import com.microsoft.azure.management.resources.Deployment
import com.microsoft.azure.management.resources.DeploymentMode
import com.microsoft.azure.management.resources.fluentcore.arm.Region
import com.microsoft.rest.LogLevel
import configuration.AZURE_TEMPLATE_CREATE_FILE
import org.apache.maven.plugin.logging.Log
import java.io.File
import java.net.URI

class AzureResourceManagerService(private val logger: Log, region: String) : StackService {

    private val azureAuthLocation = System.getenv("AZURE_AUTH_LOCATION")
    private val credFile = File(azureAuthLocation)

    private val fileService: FileService = FileService(logger)

    private val region = Region.UK_SOUTH //Region.create(region, region)

    private val azure by lazy {
        Azure.configure()
                .withLogLevel(LogLevel.BASIC)
                .authenticate(credFile)
                .withDefaultSubscription()
    }

    private var currentDeployment: Deployment? = null

    override fun findExport(exportName: String): StackService.FindExportResponse {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun updateStack(projectName: String, uri: URI): Boolean {
        val templateText = fileService.getFileText(uri)

        azure.resourceGroups().define(projectName)
                .withRegion(region)
                .create()

        val deployName = projectName + "-create-" + System.currentTimeMillis()

        currentDeployment = azure.deployments().define(deployName)
                .withExistingResourceGroup(projectName)
                .withTemplate(templateText)
                .withParameters("{}")
                .withMode(DeploymentMode.INCREMENTAL)
                .create();

        return true;
    }

    override fun createStack(projectName: String, stage: String, compiledSourcesPath: String): StackService.CreateStackResponse {
        val templateText = fileService.getFileText("$compiledSourcesPath$AZURE_TEMPLATE_CREATE_FILE-$stage.json")

        azure.resourceGroups().define(projectName)
                .withRegion(region)
                .create()

        val deployName = projectName + "-create-" + System.currentTimeMillis()

        currentDeployment = azure.deployments().define(deployName)
                .withExistingResourceGroup(projectName)
                .withTemplate(templateText)
                .withParameters("{}")
                .withMode(DeploymentMode.INCREMENTAL)
                .create();

        return StackService.CreateStackResponse(true, false)
    }

    override fun deleteStack(stackName: String): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getStackStatus(stackName: String): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getStackErrorReason(stackName: String): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun isErrorStatus(stackStatus: String): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun canContinue(stackStatus: String): StackService.ContinueResponse {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun pollStackStatus(projectName: String, count: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}