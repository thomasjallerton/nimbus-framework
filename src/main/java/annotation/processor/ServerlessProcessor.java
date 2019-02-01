package annotation.processor;

import annotation.annotations.document.DocumentStore;
import annotation.annotations.function.HttpServerlessFunction;
import annotation.annotations.function.NotificationServerlessFunction;
import annotation.annotations.function.QueueServerlessFunction;
import annotation.annotations.keyvalue.KeyValueStore;
import annotation.annotations.keyvalue.UsesKeyValueStore;
import annotation.annotations.persistent.Key;
import annotation.annotations.document.UsesDocumentStore;
import annotation.models.CloudFormationTemplate;
import annotation.models.outputs.OutputCollection;
import annotation.models.persisted.NimbusState;
import annotation.models.persisted.UserConfig;
import annotation.models.processing.MethodInformation;
import annotation.models.resource.IamRoleResource;
import annotation.models.resource.Policy;
import annotation.models.resource.Resource;
import annotation.models.resource.ResourceCollection;
import annotation.models.resource.function.FunctionConfig;
import annotation.models.resource.function.FunctionResource;
import annotation.models.resource.dynamo.DynamoResource;
import annotation.services.FileService;
import annotation.services.FunctionEnvironmentService;
import annotation.services.ReadUserConfigService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auto.service.AutoService;
import wrappers.http.HttpServerlessFunctionFileBuilder;
import wrappers.notification.NotificationServerlessFunctionFileBuilder;
import wrappers.queue.QueueServerlessFunctionFileBuilder;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@SupportedAnnotationTypes({
        "annotation.annotations.function.HttpServerlessFunction",
        "annotation.annotations.function.QueueServerlessFunction",
        "annotation.annotations.function.NotificationServerlessFunction",
        "annotation.annotations.dynamo.KeyValueStore"
})
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

        Policy lambdaPolicy = new Policy("lambda", nimbusState);

        FunctionEnvironmentService functionEnvironmentService = new FunctionEnvironmentService(
                lambdaPolicy,
                createResources,
                updateResources,
                createOutputs,
                updateOutputs,
                nimbusState
        );

        handleDocumentStore(roundEnv, updateResources);
        handleKeyValueStore(roundEnv, updateResources);
        handleHttpServerlessFunction(roundEnv, functionEnvironmentService);
        handleNotificationServerlessFunction(roundEnv, functionEnvironmentService);
        handleQueueServerlessFunction(roundEnv, functionEnvironmentService);

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

    private void handleHttpServerlessFunction(RoundEnvironment roundEnv, FunctionEnvironmentService functionEnvironmentService) {
        Set<? extends Element> annotatedElements = roundEnv.getElementsAnnotatedWith(HttpServerlessFunction.class);

        for (Element type : annotatedElements) {
            HttpServerlessFunction httpFunction = type.getAnnotation(HttpServerlessFunction.class);

            if (type.getKind() == ElementKind.METHOD) {
                MethodInformation methodInformation = extractMethodInformation(type);


                HttpServerlessFunctionFileBuilder fileBuilder = new HttpServerlessFunctionFileBuilder(
                        processingEnv,
                        methodInformation
                );

                String handler = fileBuilder.getHandler();

                FunctionConfig config = new FunctionConfig(httpFunction.timeout(), httpFunction.memory());
                FunctionResource functionResource = functionEnvironmentService.newFunction(handler, methodInformation, config);

                handleUseResources(type, functionResource, functionEnvironmentService.getLambdaPolicy(), updateResources);

                functionEnvironmentService.newHttpMethod(httpFunction, functionResource);

                fileBuilder.createClass();
            }
        }
    }

    private void handleNotificationServerlessFunction(RoundEnvironment roundEnv, FunctionEnvironmentService functionEnvironmentService) {
        Set<? extends Element> annotatedElements = roundEnv.getElementsAnnotatedWith(NotificationServerlessFunction.class);

        for (Element type : annotatedElements) {
            NotificationServerlessFunction notificationFunction = type.getAnnotation(NotificationServerlessFunction.class);

            if (type.getKind() == ElementKind.METHOD) {
                MethodInformation methodInformation = extractMethodInformation(type);

                NotificationServerlessFunctionFileBuilder fileBuilder = new NotificationServerlessFunctionFileBuilder(
                        processingEnv,
                        methodInformation
                );

                FunctionConfig config = new FunctionConfig(notificationFunction.timeout(), notificationFunction.memory());
                FunctionResource functionResource = functionEnvironmentService.newFunction(fileBuilder.getHandler(), methodInformation, config);

                handleUseResources(type, functionResource, functionEnvironmentService.getLambdaPolicy(), updateResources);

                functionEnvironmentService.newNotification(notificationFunction, functionResource);

                fileBuilder.createClass();
            }
        }
    }

    private void handleQueueServerlessFunction(RoundEnvironment roundEnv, FunctionEnvironmentService functionEnvironmentService) {
        Set<? extends Element> annotatedElements = roundEnv.getElementsAnnotatedWith(QueueServerlessFunction.class);

        for (Element type : annotatedElements) {
            QueueServerlessFunction queueFunction = type.getAnnotation(QueueServerlessFunction.class);

            if (type.getKind() == ElementKind.METHOD) {
                MethodInformation methodInformation = extractMethodInformation(type);

                QueueServerlessFunctionFileBuilder fileBuilder = new QueueServerlessFunctionFileBuilder(
                        processingEnv,
                        methodInformation
                );

                FunctionConfig config = new FunctionConfig(queueFunction.timeout(), queueFunction.memory());
                FunctionResource functionResource = functionEnvironmentService.newFunction(fileBuilder.getHandler(), methodInformation, config);

                handleUseResources(type, functionResource, functionEnvironmentService.getLambdaPolicy(), updateResources);

                functionEnvironmentService.newQueue(queueFunction, functionResource);

                fileBuilder.createClass();
            }
        }
    }

    private MethodInformation extractMethodInformation(Element type) {
        String methodName = type.getSimpleName().toString();
        Element enclosing = type.getEnclosingElement();
        String className = enclosing.getSimpleName().toString();

        ExecutableType executableType = (ExecutableType) type.asType();
        List<? extends TypeMirror> parameters = executableType.getParameterTypes();
        TypeMirror returnType = executableType.getReturnType();

        PackageElement packageElem = (PackageElement) enclosing.getEnclosingElement();
        String qualifiedName = packageElem.getQualifiedName().toString();

        return new MethodInformation(className, methodName, qualifiedName, parameters, returnType);
    }

    private void handleDocumentStore(RoundEnvironment roundEnv, ResourceCollection updateResources) {
        Set<? extends Element> annotatedElements = roundEnv.getElementsAnnotatedWith(DocumentStore.class);

        for (Element type : annotatedElements) {
            DocumentStore documentStore = type.getAnnotation(DocumentStore.class);

            DynamoResource dynamoResource = createDynamoResource(documentStore.tableName(), type.getSimpleName().toString());


            if (type.getKind() == ElementKind.CLASS) {

                for (Element enclosedElement : type.getEnclosedElements()) {
                    for (Key ignored : enclosedElement.getAnnotationsByType(Key.class)) {
                        if (enclosedElement.getKind() == ElementKind.FIELD) {

                            String typeName = enclosedElement.getSimpleName().toString();

                            Object fieldType = enclosedElement.asType();

                            dynamoResource.addHashKey(typeName, fieldType);
                        }
                    }
                }
            }
            updateResources.addResource(dynamoResource);
        }
    }

    private void handleKeyValueStore(RoundEnvironment roundEnv, ResourceCollection updateResources) {
        Set<? extends Element> annotatedElements = roundEnv.getElementsAnnotatedWith(KeyValueStore.class);

        for (Element type : annotatedElements) {
            KeyValueStore keyValueStore = type.getAnnotation(KeyValueStore.class);

            DynamoResource dynamoResource = createDynamoResource(keyValueStore.tableName(), type.getSimpleName().toString());

            dynamoResource.addHashKey(keyValueStore.keyName(), keyValueStore.keyType());
            updateResources.addResource(dynamoResource);
        }
    }

    private DynamoResource createDynamoResource(String givenTableName, String className) {
        String tableName = determineTableName(givenTableName, className);
        return new DynamoResource(tableName, nimbusState);
    }

    private void handleUseResources(Element serverlessMethod, FunctionResource functionResource, Policy policy, ResourceCollection updateResources) {
        for (UsesDocumentStore usesDocumentStore : serverlessMethod.getAnnotationsByType(UsesDocumentStore.class)) {
            DocumentStore documentStore;
            String tableName;

            try
            {
                documentStore = usesDocumentStore.dataModel().getDeclaredAnnotation(DocumentStore.class);
                tableName = determineTableName(documentStore.tableName(), usesDocumentStore.dataModel().getSimpleName());
            }
            catch( MirroredTypeException mte )
            {
                Types TypeUtils = this.processingEnv.getTypeUtils();
                TypeElement typeElement =  (TypeElement)TypeUtils.asElement(mte.getTypeMirror());
                documentStore = typeElement.getAnnotation(DocumentStore.class);
                tableName = determineTableName(documentStore.tableName(), typeElement.getSimpleName().toString());
            }

            Resource resource = updateResources.get(tableName);

            if (resource != null) {
                policy.addAllowStatement("dynamodb:*", resource, "");
            }
        }
        for (UsesKeyValueStore usesKeyValueStore : serverlessMethod.getAnnotationsByType(UsesKeyValueStore.class)) {
            KeyValueStore keyValueStore;
            String tableName;

            try
            {
                keyValueStore = usesKeyValueStore.dataModel().getDeclaredAnnotation(KeyValueStore.class);
                tableName = determineTableName(keyValueStore.tableName(), usesKeyValueStore.dataModel().getSimpleName());
            }
            catch( MirroredTypeException mte )
            {
                Types TypeUtils = this.processingEnv.getTypeUtils();
                TypeElement typeElement =  (TypeElement)TypeUtils.asElement(mte.getTypeMirror());
                keyValueStore = typeElement.getAnnotation(KeyValueStore.class);
                tableName = determineTableName(keyValueStore.tableName(), typeElement.getSimpleName().toString());
            }

            Resource resource = updateResources.get(tableName);

            if (resource != null) {
                policy.addAllowStatement("dynamodb:*", resource, "");
            }
        }


    }

    private String determineTableName(String givenName, String className) {
        String tableName;
        if (givenName.equals("")) {
            tableName = className;
        } else {
            tableName = givenName;
        }
        return tableName;
    }


}

