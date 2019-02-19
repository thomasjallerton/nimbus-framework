package annotation.processor;

import annotation.annotations.document.DocumentStore;
import annotation.annotations.document.UsesDocumentStore;
import annotation.annotations.function.DocumentStoreServerlessFunction;
import annotation.annotations.function.HttpServerlessFunction;
import annotation.annotations.function.NotificationServerlessFunction;
import annotation.annotations.function.QueueServerlessFunction;
import annotation.annotations.keyvalue.KeyValueStore;
import annotation.annotations.keyvalue.UsesKeyValueStore;
import annotation.annotations.persistent.Key;
import annotation.annotations.queue.UsesQueue;
import annotation.models.CloudFormationTemplate;
import annotation.models.outputs.OutputCollection;
import annotation.models.persisted.NimbusState;
import annotation.models.persisted.UserConfig;
import annotation.models.processing.MethodInformation;
import annotation.models.resource.*;
import annotation.models.resource.dynamo.DynamoResource;
import annotation.models.resource.function.FunctionConfig;
import annotation.models.resource.function.FunctionResource;
import annotation.models.resource.queue.QueueResource;
import annotation.services.FileService;
import annotation.services.FunctionEnvironmentService;
import annotation.services.ReadUserConfigService;
import annotation.wrappers.DataModelAnnotation;
import annotation.wrappers.DocumentStoreServerlessFunctionAnnotation;
import annotation.wrappers.UsesDocumentStoreAnnotation;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auto.service.AutoService;
import wrappers.document.DocumentStoreServerlessFunctionFileBuilder;
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
import javax.tools.Diagnostic;
import java.text.SimpleDateFormat;
import java.util.*;

@SupportedAnnotationTypes({
        "annotation.annotations.function.HttpServerlessFunction",
        "annotation.annotations.function.QueueServerlessFunction",
        "annotation.annotations.function.NotificationServerlessFunction",
        "annotation.annotations.dynamo.KeyValueStore"
})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@AutoService(Processor.class)
public class NimbusAnnotationProcessor extends AbstractProcessor {

    private NimbusState nimbusState = null;

    private FileService fileService = new FileService();

    private UserConfig userConfig = new ReadUserConfigService().readUserConfig();

    private ResourceCollection updateResources = new ResourceCollection();
    private ResourceCollection createResources = new ResourceCollection();

    private OutputCollection updateOutputs = new OutputCollection();
    private OutputCollection createOutputs = new OutputCollection();

    private Messager messager;

    private Map<String, Resource> savedResources = new HashMap<>();

    @Override
    public synchronized void init(final ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        messager = processingEnv.getMessager();
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

        //Handle Functions
        List<FunctionInformation> httpFunctions = handleHttpServerlessFunction(roundEnv, functionEnvironmentService);
        List<FunctionInformation> notificationFunctions = handleNotificationServerlessFunction(roundEnv, functionEnvironmentService);
        List<FunctionInformation> queueFunctions = handleQueueServerlessFunction(roundEnv, functionEnvironmentService);
        List<FunctionInformation> documentStoreFunctions = handleDocumentStoreServerlessFunction(roundEnv, functionEnvironmentService);

        //Now that all resources exist handle permissions
        handleUseResources(httpFunctions, lambdaPolicy, updateResources);
        handleUseResources(notificationFunctions, lambdaPolicy, updateResources);
        handleUseResources(queueFunctions, lambdaPolicy, updateResources);
        handleUseResources(documentStoreFunctions, lambdaPolicy, updateResources);

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

    private void handleUseResources(List<FunctionInformation> functionInformationList, Policy policy, ResourceCollection updateResources) {
        for (FunctionInformation functionInformation : functionInformationList) {
            handleUseResources(functionInformation.element, functionInformation.resource, policy, updateResources);
        }
    }

    private List<FunctionInformation> handleDocumentStoreServerlessFunction(RoundEnvironment roundEnv, FunctionEnvironmentService functionEnvironmentService) {
        Set<? extends Element> annotatedElements = roundEnv.getElementsAnnotatedWith(DocumentStoreServerlessFunction.class);

        List<FunctionInformation> results = new LinkedList<>();
        for (Element type : annotatedElements) {
            DocumentStoreServerlessFunction documentStoreFunction = type.getAnnotation(DocumentStoreServerlessFunction.class);

            if (type.getKind() == ElementKind.METHOD) {
                MethodInformation methodInformation = extractMethodInformation(type);

                DocumentStoreServerlessFunctionFileBuilder fileBuilder = new DocumentStoreServerlessFunctionFileBuilder(
                        processingEnv,
                        methodInformation,
                        type
                );

                String handler = fileBuilder.getHandler();

                FunctionConfig config = new FunctionConfig(documentStoreFunction.timeout(), documentStoreFunction.memory());
                FunctionResource functionResource = functionEnvironmentService.newFunction(handler, methodInformation, config);

                DataModelAnnotation dataModelAnnotation = new DocumentStoreServerlessFunctionAnnotation(documentStoreFunction);
                Resource dynamoResource = getDocumentStoreResource(dataModelAnnotation, type);

                if (dynamoResource != null) {
                    functionEnvironmentService.newDocumentStoreTrigger(dynamoResource, functionResource);
                }

                fileBuilder.createClass();

                results.add(new FunctionInformation(type, functionResource));
            }
        }
        return results;
    }

    private List<FunctionInformation> handleHttpServerlessFunction(RoundEnvironment roundEnv, FunctionEnvironmentService functionEnvironmentService) {
        Set<? extends Element> annotatedElements = roundEnv.getElementsAnnotatedWith(HttpServerlessFunction.class);

        List<FunctionInformation> results = new LinkedList<>();

        for (Element type : annotatedElements) {
            HttpServerlessFunction httpFunction = type.getAnnotation(HttpServerlessFunction.class);

            if (type.getKind() == ElementKind.METHOD) {
                MethodInformation methodInformation = extractMethodInformation(type);


                HttpServerlessFunctionFileBuilder fileBuilder = new HttpServerlessFunctionFileBuilder(
                        processingEnv,
                        methodInformation,
                        type
                );

                String handler = fileBuilder.getHandler();

                FunctionConfig config = new FunctionConfig(httpFunction.timeout(), httpFunction.memory());
                FunctionResource functionResource = functionEnvironmentService.newFunction(handler, methodInformation, config);

                functionEnvironmentService.newHttpMethod(httpFunction, functionResource);

                fileBuilder.createClass();

                results.add(new FunctionInformation(type, functionResource));
            }
        }
        return results;
    }

    private List<FunctionInformation> handleNotificationServerlessFunction(RoundEnvironment roundEnv, FunctionEnvironmentService functionEnvironmentService) {
        Set<? extends Element> annotatedElements = roundEnv.getElementsAnnotatedWith(NotificationServerlessFunction.class);

        List<FunctionInformation> results = new LinkedList<>();

        for (Element type : annotatedElements) {
            NotificationServerlessFunction notificationFunction = type.getAnnotation(NotificationServerlessFunction.class);

            if (type.getKind() == ElementKind.METHOD) {
                MethodInformation methodInformation = extractMethodInformation(type);

                NotificationServerlessFunctionFileBuilder fileBuilder = new NotificationServerlessFunctionFileBuilder(
                        processingEnv,
                        methodInformation,
                        type
                );

                FunctionConfig config = new FunctionConfig(notificationFunction.timeout(), notificationFunction.memory());
                FunctionResource functionResource = functionEnvironmentService.newFunction(fileBuilder.getHandler(), methodInformation, config);

                functionEnvironmentService.newNotification(notificationFunction, functionResource);

                fileBuilder.createClass();

                results.add(new FunctionInformation(type, functionResource));
            }
        }
        return results;
    }

    private List<FunctionInformation> handleQueueServerlessFunction(RoundEnvironment roundEnv, FunctionEnvironmentService functionEnvironmentService) {
        Set<? extends Element> annotatedElements = roundEnv.getElementsAnnotatedWith(QueueServerlessFunction.class);

        List<FunctionInformation> results = new LinkedList<>();

        for (Element type : annotatedElements) {
            QueueServerlessFunction queueFunction = type.getAnnotation(QueueServerlessFunction.class);

            if (type.getKind() == ElementKind.METHOD) {
                MethodInformation methodInformation = extractMethodInformation(type);

                QueueServerlessFunctionFileBuilder fileBuilder = new QueueServerlessFunctionFileBuilder(
                        processingEnv,
                        methodInformation,
                        type
                );

                FunctionConfig config = new FunctionConfig(queueFunction.timeout(), queueFunction.memory());
                FunctionResource functionResource = functionEnvironmentService.newFunction(fileBuilder.getHandler(), methodInformation, config);

                QueueResource newQueue = functionEnvironmentService.newQueue(queueFunction, functionResource);

                if (savedResources.containsKey(queueFunction.id())) {
                    messager.printMessage(Diagnostic.Kind.ERROR, "Can't have multiple consumers of the same queue ("
                            + queueFunction.id() + ")", type);
                    return results;
                }
                savedResources.put(queueFunction.id(), newQueue);

                fileBuilder.createClass();

                results.add(new FunctionInformation(type, functionResource));
            }
        }
        return results;
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

            if (documentStore.existingArn().equals("")) {

                if (type.getKind() == ElementKind.CLASS) {

                    for (Element enclosedElement : type.getEnclosedElements()) {
                        for (Key key : enclosedElement.getAnnotationsByType(Key.class)) {
                            if (enclosedElement.getKind() == ElementKind.FIELD) {

                                String columnName = key.columnName();
                                if (columnName.equals("")) columnName = enclosedElement.getSimpleName().toString();

                                Object fieldType = enclosedElement.asType();

                                dynamoResource.addHashKey(columnName, fieldType);
                            }
                        }
                    }
                }
                updateResources.addResource(dynamoResource);
            }
        }
    }

    private void handleKeyValueStore(RoundEnvironment roundEnv, ResourceCollection updateResources) {
        Set<? extends Element> annotatedElements = roundEnv.getElementsAnnotatedWith(KeyValueStore.class);

        for (Element type : annotatedElements) {
            KeyValueStore keyValueStore = type.getAnnotation(KeyValueStore.class);

            if (keyValueStore.existingArn().equals("")) {
                DynamoResource dynamoResource = createDynamoResource(keyValueStore.tableName(), type.getSimpleName().toString());

                dynamoResource.addHashKey(keyValueStore.keyName(), keyValueStore.keyType());
                updateResources.addResource(dynamoResource);
            }
        }
    }

    private DynamoResource createDynamoResource(String givenTableName, String className) {
        String tableName = determineTableName(givenTableName, className);
        return new DynamoResource(tableName, nimbusState);
    }

    private Resource getDocumentStoreResource(DataModelAnnotation dataModelAnnotation, Element serverlessMethod) {
        try {
            Class<?> dataModel = dataModelAnnotation.getDataModel();
            DocumentStore documentStore = dataModel.getDeclaredAnnotation(DocumentStore.class);
            return getResource(updateResources, documentStore.existingArn(), documentStore.tableName(), dataModel.getSimpleName());
        } catch (MirroredTypeException mte) {
            try {
                Types TypeUtils = this.processingEnv.getTypeUtils();
                TypeElement typeElement = (TypeElement) TypeUtils.asElement(mte.getTypeMirror());
                DocumentStore documentStore = typeElement.getAnnotation(DocumentStore.class);
                return getResource(updateResources, documentStore.existingArn(), documentStore.tableName(), typeElement.getSimpleName().toString());
            } catch (NullPointerException e) {
                messager.printMessage(Diagnostic.Kind.ERROR, "Input class expected to be annotated with DocumentStore but isn't", serverlessMethod);
                return null;
            }
        } catch (NullPointerException e) {
            messager.printMessage(Diagnostic.Kind.ERROR, "Input class expected to be annotated with DocumentStore but isn't", serverlessMethod);
            return null;
        }
    }

    private Resource getKeyValueStoreResource(Class<?> dataModel, Element serverlessMethod) {
        try {
            KeyValueStore keyValueStore = dataModel.getDeclaredAnnotation(KeyValueStore.class);
            return getResource(updateResources, keyValueStore.existingArn(), keyValueStore.tableName(), dataModel.getSimpleName());
        } catch (MirroredTypeException mte) {
            try {
                Types TypeUtils = this.processingEnv.getTypeUtils();
                TypeElement typeElement = (TypeElement) TypeUtils.asElement(mte.getTypeMirror());
                KeyValueStore keyValueStore = typeElement.getAnnotation(KeyValueStore.class);
                return getResource(updateResources, keyValueStore.existingArn(), keyValueStore.tableName(), typeElement.getSimpleName().toString());
            } catch (NullPointerException e) {
                messager.printMessage(Diagnostic.Kind.ERROR, "Input class expected to be annotated with KeyValueStore but isn't", serverlessMethod);
                return null;
            }
        } catch (NullPointerException e) {
            messager.printMessage(Diagnostic.Kind.ERROR, "Input class expected to be annotated with KeyValueStore but isn't", serverlessMethod);
            return null;
        }
    }

    private void handleUseResources(Element serverlessMethod, FunctionResource functionResource, Policy policy, ResourceCollection updateResources) {
        for (UsesDocumentStore usesDocumentStore : serverlessMethod.getAnnotationsByType(UsesDocumentStore.class)) {

            DataModelAnnotation dataModelAnnotation = new UsesDocumentStoreAnnotation(usesDocumentStore);
            Resource resource = getDocumentStoreResource(dataModelAnnotation, serverlessMethod);

            if (resource != null) {
                policy.addAllowStatement("dynamodb:*", resource, "");
            }
        }
        for (UsesKeyValueStore usesKeyValueStore : serverlessMethod.getAnnotationsByType(UsesKeyValueStore.class)) {
            Resource resource = getKeyValueStoreResource(usesKeyValueStore.dataModel(), serverlessMethod);

            if (resource != null) {
                policy.addAllowStatement("dynamodb:*", resource, "");
            }
        }

        for (UsesQueue usesQueue : serverlessMethod.getAnnotationsByType(UsesQueue.class)) {
            if (usesQueue.id().equals("") || savedResources.get(usesQueue.id()) == null) {
                messager.printMessage(Diagnostic.Kind.ERROR, "Unable to find id of queue, have you set it in the @QueueServerlessFunction?", serverlessMethod);
                return;
            }

            Resource referencedQueue = savedResources.get(usesQueue.id());
            if (!(referencedQueue instanceof QueueResource)) {
                messager.printMessage(Diagnostic.Kind.ERROR, "Resource with id " + usesQueue.id() + " is not a Queue", serverlessMethod);
                return;
            }

            functionResource.addEnvVariable("NIMBUS_QUEUE_URL_ID_" + usesQueue.id().toUpperCase(), referencedQueue.getRef());
            policy.addAllowStatement("sqs:SendMessage", referencedQueue, "");
        }
    }

    private Resource getResource(ResourceCollection updateResources, String existingArn, String tableName, String elementName) {
        if (existingArn.equals("")) {
            return updateResources.get(determineTableName(tableName, elementName));
        } else {
            return new ExistingResource(existingArn, nimbusState);
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

    private class FunctionInformation {

        private Element element;
        private FunctionResource resource;

        public FunctionInformation(Element element, FunctionResource resource) {
            this.element = element;
            this.resource = resource;
        }

        public Element getElement() {
            return element;
        }

        public void setElement(Element element) {
            this.element = element;
        }

        public FunctionResource getResource() {
            return resource;
        }

        public void setResource(FunctionResource resource) {
            this.resource = resource;
        }
    }
}

