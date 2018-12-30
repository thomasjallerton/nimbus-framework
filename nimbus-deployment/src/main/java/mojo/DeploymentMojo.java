package mojo;

import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.cloudformation.AmazonCloudFormationClientBuilder;
import com.amazonaws.services.cloudformation.model.CreateStackRequest;
import com.amazonaws.util.IOUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Mojo(name = "nimbus-deployment")
public class DeploymentMojo extends AbstractMojo {

    Log logger;

    @Parameter( property = "region", defaultValue = "eu-west-1" )
    private String region;


    public void execute() throws MojoExecutionException, MojoFailureException {
        logger = getLog();
        AmazonCloudFormation client = AmazonCloudFormationClientBuilder.standard()
                .withRegion(region)
                .build();

        CreateStackRequest createStackRequest = new CreateStackRequest()
                .withStackName("nimbus-project")
                .withTemplateBody(getTemplateText(".nimbus/cloudformation-stack-update.json"));

        client.createStack(createStackRequest);
        logger.info("Created Stack Successfully");
    }

    private String getTemplateText(final String templatePath) {

        try {
            byte[] encoded = Files.readAllBytes(Paths.get(templatePath));
            return new String(encoded);
        } catch (IOException e) {
            logger.error(e);
        }
        return "";
    }
}
