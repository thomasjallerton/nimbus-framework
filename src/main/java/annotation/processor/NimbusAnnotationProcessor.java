package annotation.processor;

import annotation.annotations.document.DocumentStore;
import annotation.annotations.document.UsesDocumentStore;
import annotation.annotations.keyvalue.KeyValueStore;
import annotation.annotations.keyvalue.UsesKeyValueStore;
import annotation.annotations.persistent.Key;
import annotation.annotations.queue.UsesQueue;
import annotation.cloudformation.CloudFormationTemplate;
import annotation.cloudformation.outputs.OutputCollection;
import annotation.cloudformation.persisted.NimbusState;
import annotation.cloudformation.persisted.UserConfig;
import annotation.cloudformation.resource.*;
import annotation.cloudformation.resource.dynamo.DynamoResource;
import annotation.cloudformation.resource.function.FunctionResource;
import annotation.cloudformation.resource.queue.QueueResource;
import annotation.services.FileService;
import annotation.services.FunctionEnvironmentService;
import annotation.services.ReadUserConfigService;
import annotation.services.functions.*;
import annotation.wrappers.annotations.datamodel.DataModelAnnotation;
import annotation.wrappers.annotations.datamodel.KeyValueStoreAnnotation;
import annotation.wrappers.annotations.datamodel.UsesDocumentStoreAnnotation;
import annotation.wrappers.annotations.datamodel.UsesKeyValueStoreAnnotation;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auto.service.AutoService;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.text.SimpleDateFormat;
import java.util.*;

@SupportedAnnotationTypes({
        "annotation.annotations.function.HttpServerlessFunction",
        "annotation.annotations.function.QueueServerlessFunction",
        "annotation.annotations.function.DocumentStoreServerlessFunction",
        "annotation.annotations.function.NotificationServerlessFunction",
        "annotation.annotations.dynamo.KeyValueStore",
        "annotation.annotations.dynamo.DocumentStore"
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

        List<FunctionResourceCreator> resourceCreators = new LinkedList<>();
        resourceCreators.add(new DocumentStoreFunctionResourceCreator(updateResources, nimbusState, processingEnv));
        resourceCreators.add(new KeyValueStoreFunctionResourceCreator(updateResources, nimbusState, processingEnv));
        resourceCreators.add(new HttpFunctionResourceCreator(updateResources, nimbusState, processingEnv));
        resourceCreators.add(new NotificationFunctionResourceCreator(updateResources, nimbusState, processingEnv));
        resourceCreators.add(new QueueFunctionResourceCreator(updateResources, nimbusState, processingEnv, savedResources));

        for (FunctionResourceCreator creator : resourceCreators) {
            handleUseResources(creator.handle(roundEnv, functionEnvironmentService), lambdaPolicy, updateResources);
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

    private void handleUseResources(List<FunctionInformation> functionInformationList, Policy policy, ResourceCollection updateResources) {
        for (FunctionInformation functionInformation : functionInformationList) {
            handleUseResources(functionInformation.getElement(), functionInformation.getResource(), policy, updateResources);
        }
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
                DataModelAnnotation dataModelAnnotation = new KeyValueStoreAnnotation(keyValueStore);
                TypeElement element = dataModelAnnotation.getTypeElement(processingEnv);

                dynamoResource.addHashKeyClass(keyValueStore.keyName(), element);
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
            TypeElement typeElement = dataModelAnnotation.getTypeElement(this.processingEnv);
            DocumentStore documentStore = typeElement.getAnnotation(DocumentStore.class);
            return getResource(updateResources, documentStore.existingArn(), documentStore.tableName(), typeElement.getSimpleName().toString());
        } catch (NullPointerException e) {
            messager.printMessage(Diagnostic.Kind.ERROR, "Input class expected to be annotated with DocumentStore but isn't", serverlessMethod);
            return null;
        }
    }

    private Resource getKeyValueStoreResource(DataModelAnnotation dataModelAnnotation, Element serverlessMethod) {
            try {
                TypeElement typeElement = dataModelAnnotation.getTypeElement(processingEnv);
                KeyValueStore keyValueStore = typeElement.getAnnotation(KeyValueStore.class);
                return getResource(updateResources, keyValueStore.existingArn(), keyValueStore.tableName(), typeElement.getSimpleName().toString());
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
            DataModelAnnotation annotation = new UsesKeyValueStoreAnnotation(usesKeyValueStore);
            Resource resource = getKeyValueStoreResource(annotation, serverlessMethod);

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
}

