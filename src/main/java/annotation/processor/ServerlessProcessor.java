package annotation.processor;

import annotation.models.CloudFormationTemplate;
import annotation.models.outputs.BucketNameOutput;
import annotation.models.outputs.Output;
import annotation.models.outputs.OutputCollection;
import annotation.models.persisted.NimbusState;
import annotation.models.resource.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auto.service.AutoService;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Set;

@SupportedAnnotationTypes("annotation.annotations.ServerlessFunction")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@AutoService(Processor.class)
public class ServerlessProcessor extends AbstractProcessor {

    //TODO: Add memoization so that on repeat processes do not have to run code again
    NimbusState persistent = null;

    @Override
    public synchronized void init(final ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        if (persistent == null) {
            Calendar cal = Calendar.getInstance();
            SimpleDateFormat simpleDateFormat =
                    new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSzzz", Locale.US);

            String compilationTime = simpleDateFormat.format(cal.getTime());
            persistent = new NimbusState(compilationTime);
        }

        ResourceCollection updateResources = new ResourceCollection();
        ResourceCollection createResources = new ResourceCollection();

        OutputCollection updateOutputs = new OutputCollection();
        OutputCollection createOutputs = new OutputCollection();

        for (TypeElement annotation : annotations) {
            Set<? extends Element> annotatedElements = roundEnv.getElementsAnnotatedWith(annotation);

            Policy lambdaPolicy = new Policy("lambda");

            //Should be a handler
            for (Element element : annotatedElements) {
                if (element.getKind() == ElementKind.METHOD) {
                    String methodName = element.getSimpleName().toString();
                    Element enclosing = element.getEnclosingElement();
                    String name = enclosing.getSimpleName().toString();

                    PackageElement packageElem = (PackageElement) enclosing.getEnclosingElement();
                    String qualifiedName = packageElem.getQualifiedName().toString();

                    String handler;
                    if (qualifiedName.isEmpty()) {
                        handler = enclosing.getSimpleName().toString() + "::" + methodName;
                    } else {
                        handler = qualifiedName + "." + name + "::" + methodName;
                    }


                    FunctionResource function = new FunctionResource(handler, name, persistent.getCompilationTimeStamp());
                    Resource logGroup = new LogGroupResource(methodName);
                    Resource bucket   = new NimbusBucketResource();

                    lambdaPolicy.addAllowStatement("logs:CreateLogStream", logGroup, ":*");
                    lambdaPolicy.addAllowStatement("logs:PutLogEvents", logGroup, ":*:*");

                    updateResources.addResource(function);
                    updateResources.addResource(logGroup);
                    updateResources.addResource(bucket);

                    createResources.addResource(bucket);

                    Output bucketName = new BucketNameOutput();
                    createOutputs.addOutput(bucketName);
                    updateOutputs.addOutput(bucketName);
                }
            }

            IamRoleResource iamRole = new IamRoleResource(lambdaPolicy);
            updateResources.addResource(iamRole);
        }

        CloudFormationTemplate update = new CloudFormationTemplate(updateResources, updateOutputs);
        CloudFormationTemplate create = new CloudFormationTemplate(createResources, createOutputs);

        saveTemplate("cloudformation-stack-update", update);
        saveTemplate("cloudformation-stack-create", create);
        ObjectMapper mapper = new ObjectMapper();
        try {
            saveJsonFIle("nimbus-state", mapper.writeValueAsString(persistent));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return true;
    }

    private void saveTemplate(String name, CloudFormationTemplate template) {
        if (template.valid()) {
            saveJsonFIle(name, template.toString());
        }
    }

    private void saveJsonFIle(String name, String file) {
        try {
            Path path = Paths.get(".nimbus/" + name + ".json");
            path.toFile().getParentFile().mkdirs();
            byte[] strToBytes = file.getBytes();
            System.out.println(path.toAbsolutePath().toString());
            Files.write(path, strToBytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
