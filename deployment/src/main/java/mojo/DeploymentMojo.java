package mojo;

import assembly.FunctionHasher;
import com.nimbusframework.nimbuscore.persisted.*;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import persisted.*;
import services.*;

import java.io.File;
import java.net.URI;
import java.util.*;

import static configuration.ConfigurationKt.*;

@Mojo(name = "deploy",
        requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME,
        requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class DeploymentMojo extends AbstractMojo {

    private Log logger;

    @Parameter(property = "region", defaultValue = "eu-west-1")
    private String region;

    @Parameter(property = "stage", defaultValue = "dev")
    private String stage;

    @Parameter(property = "shadedJarPath", defaultValue = "target/functions.jar")
    private String shadedJarPath;

    @Parameter(property = "compiledSourcePath", defaultValue = "target/generated-sources/annotations/")
    private String compiledSourcePath;


    @Parameter(property = "addEntireJar", defaultValue = "false")
    private String addEntireJar;

    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject mavenProject;

    public DeploymentMojo() {
        super();
        logger = getLog();
    }

    private StackService stackService;

    private void initialise(NimbusState nimbusState) {
        if (nimbusState.getCloudProvider() == CloudProvider.AWS) {
            stackService = new CloudFormationService(logger, region);
        } else if (nimbusState.getCloudProvider() == CloudProvider.AZURE) {
            stackService = new AzureResourceManagerService(logger, region);
        }
    }

    public void execute() throws MojoExecutionException, MojoFailureException {
        logger.info("Starting");
        String compiledSourcePathFixed = FileService.addDirectorySeparatorIfNecessary(compiledSourcePath);
        PersistedStateService persistedStateService = new PersistedStateService(logger, compiledSourcePathFixed);

        FileService fileService = new FileService(logger);
        logger.info("Created File Service");

        NimbusState nimbusState = persistedStateService.getNimbusState();
        DeploymentInformation deploymentInformation = persistedStateService.getDeploymentInformation(stage);

        logger.info("Got deployment information");
        initialise(nimbusState);

        logger.info("Initialised");
        S3Service s3Service = new S3Service(region, nimbusState, logger);

        String stackName = nimbusState.getProjectName() + "-" + stage;
        logger.info("Beginning deployment for project: " + nimbusState.getProjectName() + ", stage: " + stage);
        //Try to create stack
        StackService.CreateStackResponse createSuccessful = stackService.createStack(stackName, stage, compiledSourcePathFixed);
        if (!createSuccessful.getSuccessful()) throw new MojoFailureException("Unable to create stack");

        if (createSuccessful.getAlreadyExists()) {
            logger.info("Stack already exists, proceeding to update");
        } else {
            logger.info("Creating stack");
            logger.info("Polling stack create progress");
            stackService.pollStackStatus(stackName, 0);
            logger.info("Stack created");
        }


        StackService.FindExportResponse lambdaBucketName = stackService.findExport(
                nimbusState.getProjectName() + "-" + stage + "-" + DEPLOYMENT_BUCKET_NAME);

        if (!lambdaBucketName.getSuccessful()) throw new MojoFailureException("Unable to find deployment bucket");

        Map<String, DeployedFunctionInformation> newFunctions = new HashMap<>();
        DeploymentInformation newDeployment = new DeploymentInformation(nimbusState.getCompilationTimeStamp(), newFunctions);
        FunctionHasher functionHasher = new FunctionHasher(FileService.addDirectorySeparatorIfNecessary(mavenProject.getBuild().getOutputDirectory()));
        Map<String, String> versionToReplace = new HashMap<>();

        Set<FunctionFileToUpload> functionFilesToUpload = new HashSet<>();
        for (HandlerInformation handlerInformation : nimbusState.getHandlerFiles()) {
            String classPath = handlerInformation.getHandlerClassPath();
            String currentHash = functionHasher.determineFunctionHash(classPath);

            String fileInS3Path;
            if (handlerInformation.getOverrideFileName() == null) {
                // Use default file
                fileInS3Path = nimbusState.getCompilationTimeStamp() + "/" + "lambdacode";
                functionFilesToUpload.add(new FunctionFileToUpload(shadedJarPath, "lambdacode"));
            } else {
                fileInS3Path = nimbusState.getCompilationTimeStamp() + "/" + handlerInformation.getOverrideFileName();
                functionFilesToUpload.add(new FunctionFileToUpload(handlerInformation.getOverrideFileName(), handlerInformation.getOverrideFileName()));
            }

            DeployedFunctionInformation newFunction = new DeployedFunctionInformation(fileInS3Path, currentHash);
            newFunctions.put(classPath, newFunction);
            versionToReplace.put(handlerInformation.getFileReplacementVariable(), fileInS3Path);
        }

        int i = 1;
        for (FunctionFileToUpload functionFileToUpload : functionFilesToUpload) {
            logger.info("Uploading lambda file " + i + " of " + functionFilesToUpload.size());
            boolean uploadSuccessful = s3Service.uploadFileToCompilationFolder(lambdaBucketName.getResult(), functionFileToUpload.getSourceFilePath(), functionFileToUpload.getTargetFilePath());
            if (!uploadSuccessful) throw new MojoFailureException("Failed uploading lambda code");
            i++;
        }

        persistedStateService.saveDeploymentInformation(newDeployment, stage);
        s3Service.uploadStringToS3(lambdaBucketName.getResult(), nimbusState.getCompilationTimeStamp(), S3_DEPLOYMENT_PATH);

        File fixedUpdateTemplate = fileService.replaceInFile(versionToReplace, new File(compiledSourcePathFixed + AWS_STACK_UPDATE_FILE + "-" + stage + ".json"));

        //Try to update stack
        logger.info("Uploading cloudformation file");
        boolean cloudFormationUploadSuccessful = s3Service.uploadFileToCompilationFolder(lambdaBucketName.getResult(), fixedUpdateTemplate.getPath(), "update-template");
        if (!cloudFormationUploadSuccessful)
            throw new MojoFailureException("Failed uploading cloudformation update code");

        URI cloudformationUri = s3Service.getUri(lambdaBucketName.getResult(), "update-template");

        boolean updating = stackService.updateStack(stackName, cloudformationUri);

        if (!updating) throw new MojoFailureException("Unable to update stack");

        logger.info("Updating stack");

        stackService.pollStackStatus(stackName, 0);

        logger.info("Updated stack successfully, deployment complete");

        //Deal with substitutions
        Map<String, String> substitutionParams = new HashMap<>();
        Map<String, String> outputMessages = new HashMap<>();

        substitutionParams.put(SubstitutionConstants.STAGE, stage);

        List<ExportInformation> exports = nimbusState.getExports().getOrDefault(stage, new LinkedList<>());
        for (ExportInformation export : exports) {
            StackService.FindExportResponse exportResponse = stackService.findExport(export.getExportName());
            if (exportResponse.getSuccessful()) {
                String result = exportResponse.getResult();
                substitutionParams.put(export.getSubstitutionVariable(), result);
                outputMessages.put(export.getExportMessage(), result);
            }
        }

        if (nimbusState.getFileUploads().size() > 0) {
            logger.info("Starting File Uploads");

            Map<String, List<FileUploadDescription>> bucketUploads = nimbusState.getFileUploads().get(stage);
            for (Map.Entry<String, List<FileUploadDescription>> bucketUpload : bucketUploads.entrySet()) {
                String bucketName = bucketUpload.getKey();
                for (FileUploadDescription fileUploadDescription : bucketUpload.getValue()) {
                    String localFile = fileUploadDescription.getLocalFile();
                    String targetFile = fileUploadDescription.getTargetFile();

                    s3Service.uploadFileToS3(bucketName, localFile, targetFile, substitutionParams, fileUploadDescription.getFileUploadVariableSubstitutionFileRegex());
                }
            }
        }

        if (nimbusState.getAfterDeployments().size() > 0) {
            if (nimbusState.getAfterDeployments().size() == 1) {
                logger.info("Starting after deployment script");
            } else {
                logger.info("Starting after deployment scripts");
            }

            LambdaService lambdaClient = new LambdaService(logger, region);

            List<String> afterDeployments = nimbusState.getAfterDeployments().get(stage);
            if (afterDeployments != null) {
                for (String lambda : afterDeployments) {
                    lambdaClient.invokeNoArgs(lambda);
                }
            }
        }

        logger.info("Deployment completed");

        for (Map.Entry<String, String> entry : outputMessages.entrySet()) {
            logger.info(entry.getKey() + entry.getValue());
        }
    }
}
