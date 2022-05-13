package mojo;

import com.nimbusframework.nimbuscore.persisted.NimbusState;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import services.*;

import static configuration.ConfigurationKt.DEPLOYMENT_BUCKET_NAME;

@Mojo(name = "destroy-stack")
public class DestroyMojo extends AbstractMojo {

    private Log logger;

    @Parameter(property = "region", defaultValue = "eu-west-1")
    private String region;

    @Parameter(property = "stage", defaultValue = "dev")
    private String stage;

    @Parameter(property = "compiledSourcePath", defaultValue = "target/generated-sources/annotations/")
    private String compiledSourcePath;

    public DestroyMojo() {
        super();
        logger = getLog();
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        String compiledSourcePathFixed = FileService.addDirectorySeparatorIfNecessary(compiledSourcePath);

        NimbusState nimbusState = new PersistedStateService(logger, compiledSourcePathFixed).getNimbusState();

        CloudFormationService cloudFormationService = new CloudFormationService(logger, region);
        S3Service s3Service = new S3Service(region, nimbusState, logger);
        String stackName = nimbusState.getProjectName() + "-" + stage;

        logger.info(nimbusState.getProjectName() + "-" + stage + "-" + DEPLOYMENT_BUCKET_NAME);
        StackService.FindExportResponse bucketName = cloudFormationService.findExport(
                 nimbusState.getProjectName() + "-" + stage + "-" + DEPLOYMENT_BUCKET_NAME);

        if (bucketName.getSuccessful()) {
            logger.info("Found S3 bucket, about to empty");
            s3Service.deleteBucket(bucketName.getResult());
            logger.info("Emptied S3 bucket");
        }



        boolean deleting = cloudFormationService.deleteStack(stackName);
        if (!deleting) throw new MojoFailureException("Unable to delete stack");
        logger.info("Deleting stack");

        cloudFormationService.pollStackStatus(stackName, 0);

        logger.info("Deleted stack successfully");

    }
}
