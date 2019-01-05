package annotation.processor;

import annotation.annotations.HttpServerlessFunction;
import annotation.models.CloudFormationTemplate;
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
import wrappers.http.HttpServerlessFunctionFileBuilder;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeMirror;
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
        System.out.println("PROCESSOR INITED");
        super.init(processingEnv);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        System.out.println("PROCESSOR PROCESSING SOMETHING");
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

                ExecutableType executableType = (ExecutableType)type.asType();
                List<? extends TypeMirror> parameters = executableType.getParameterTypes();
                TypeMirror returnType = executableType.getReturnType();

                PackageElement packageElem = (PackageElement) enclosing.getEnclosingElement();
                String qualifiedName = packageElem.getQualifiedName().toString();

                HttpServerlessFunctionFileBuilder fileBuilder = new HttpServerlessFunctionFileBuilder(
                        processingEnv,
                        className,
                        methodName,
                        qualifiedName,
                        parameters,
                        processingEnv.getMessager()
                );

                String handler = fileBuilder.getHandler();

                FunctionResource functionResource = functionParserService.newFunction(handler, className, methodName);

                functionParserService.newHttpMethod(httpFunction, functionResource);

                //Create wrapper code
                fileBuilder.createClass(
                        qualifiedName,
                        returnType
                );
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
