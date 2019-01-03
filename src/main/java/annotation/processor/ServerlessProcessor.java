package annotation.processor;

import annotation.annotations.HttpServerlessFunction;
import annotation.models.CloudFormationTemplate;
import annotation.models.outputs.BucketNameOutput;
import annotation.models.outputs.Output;
import annotation.models.outputs.OutputCollection;
import annotation.models.persisted.NimbusState;
import annotation.models.persisted.UserConfig;
import annotation.models.resource.*;
import annotation.services.FileService;
import annotation.services.FunctionParserService;
import annotation.services.ReadUserConfigService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auto.service.AutoService;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@SupportedAnnotationTypes("annotation.annotations.HttpServerlessFunction")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@AutoService(Processor.class)
public class ServerlessProcessor extends AbstractProcessor {

    private NimbusState nimbusState = null;

    private FileService fileService = new FileService();

    private UserConfig userConfig = new ReadUserConfigService().readUserConfig();

    private ResourceCollection updateResources = new ResourceCollection();
    private ResourceCollection createResources = new ResourceCollection();

    private OutputCollection updateOutputs = new OutputCollection();
    private OutputCollection createOutputs = new OutputCollection();

    @Override
    public synchronized void init(final ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        if (nimbusState == null) {
            Calendar cal = Calendar.getInstance();
            SimpleDateFormat simpleDateFormat =
                    new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSzzz", Locale.US);

            String compilationTime = simpleDateFormat.format(cal.getTime());
            nimbusState = new NimbusState(userConfig.getProjectName(), compilationTime);
        }

        Set<? extends Element> annotatedElements = roundEnv.getElementsAnnotatedWith(HttpServerlessFunction.class);

        Policy lambdaPolicy = new Policy("lambda", nimbusState);

        FunctionParserService functionParserService = new FunctionParserService(
                lambdaPolicy,
                createResources,
                updateResources,
                createOutputs,
                updateOutputs,
                nimbusState
        );

        for (Element type : annotatedElements) {
            HttpServerlessFunction httpFunction = type.getAnnotation(HttpServerlessFunction.class);

            if (type.getKind() == ElementKind.METHOD) {
                String methodName = type.getSimpleName().toString();
                Element enclosing = type.getEnclosingElement();
                String className = enclosing.getSimpleName().toString();


                PackageElement packageElem = (PackageElement) enclosing.getEnclosingElement();
                String qualifiedName = packageElem.getQualifiedName().toString();

                String handler;
                if (qualifiedName.isEmpty()) {
                    handler = enclosing.getSimpleName().toString() + "::" + methodName;
                } else {
                    handler = qualifiedName + "." + className + "::" + methodName;
                }

                FunctionResource functionResource = functionParserService.newFunction(handler, className, methodName);

                functionParserService.newHttpMethod(httpFunction, functionResource);

            }
        }

        IamRoleResource iamRole = new IamRoleResource(lambdaPolicy, nimbusState);
        updateResources.addResource(iamRole);

        CloudFormationTemplate update = new CloudFormationTemplate(updateResources, updateOutputs);
        CloudFormationTemplate create = new CloudFormationTemplate(createResources, createOutputs);

        fileService.saveTemplate("cloudformation-stack-update", update);
        fileService.saveTemplate("cloudformation-stack-create", create);
        ObjectMapper mapper = new ObjectMapper();
        try {
            fileService.saveJsonFile("nimbus-state", mapper.writeValueAsString(nimbusState));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return true;
    }
}
