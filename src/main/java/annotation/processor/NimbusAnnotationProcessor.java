package annotation.processor;

import annotation.annotations.database.RelationalDatabase;
import annotation.annotations.database.UsesRelationalDatabase;
import annotation.annotations.document.DocumentStore;
import annotation.annotations.document.UsesDocumentStore;
import annotation.annotations.function.UsesBasicServerlessFunctionClient;
import annotation.annotations.keyvalue.KeyValueStore;
import annotation.annotations.keyvalue.UsesKeyValueStore;
import annotation.annotations.notification.UsesNotificationTopic;
import annotation.annotations.persistent.Key;
import annotation.annotations.queue.UsesQueue;
import annotation.services.ResourceFinder;
import annotation.wrappers.annotations.datamodel.*;
import cloudformation.CloudFormationTemplate;
import cloudformation.outputs.OutputCollection;
import cloudformation.persisted.NimbusState;
import cloudformation.persisted.UserConfig;
import cloudformation.resource.*;
import cloudformation.resource.database.DatabaseConfiguration;
import cloudformation.resource.database.SubnetGroup;
import cloudformation.resource.ec2.*;
import cloudformation.resource.database.RdsResource;
import cloudformation.resource.dynamo.DynamoResource;
import cloudformation.resource.function.FunctionResource;
import cloudformation.resource.notification.SnsTopicResource;
import cloudformation.resource.queue.QueueResource;
import annotation.services.FileService;
import annotation.services.FunctionEnvironmentService;
import annotation.services.ReadUserConfigService;
import annotation.services.functions.*;
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
import java.util.function.Function;

@SupportedAnnotationTypes({
        "annotation.annotations.function.HttpServerlessFunction",
        "annotation.annotations.function.QueueServerlessFunction",
        "annotation.annotations.function.DocumentStoreServerlessFunction",
        "annotation.annotations.function.NotificationServerlessFunction",
        "annotation.annotations.function.BasicServerlessFunction",
        "annotation.annotations.function.KeyValueStoreServerlessFunction",
        "annotation.annotations.dynamo.KeyValueStore",
        "annotation.annotations.dynamo.DocumentStore",
        "annotation.annotations.database.RelationalDatabase"
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

        FunctionEnvironmentService functionEnvironmentService = new FunctionEnvironmentService(
                createResources,
                updateResources,
                createOutputs,
                updateOutputs,
                nimbusState
        );

        handleDocumentStore(roundEnv, updateResources);
        handleKeyValueStore(roundEnv, updateResources);
        handleRelationalDatabase(roundEnv, updateResources);

        List<FunctionResourceCreator> resourceCreators = new LinkedList<>();
        resourceCreators.add(new DocumentStoreFunctionResourceCreator(updateResources, nimbusState, processingEnv));
        resourceCreators.add(new KeyValueStoreFunctionResourceCreator(updateResources, nimbusState, processingEnv));
        resourceCreators.add(new HttpFunctionResourceCreator(updateResources, nimbusState, processingEnv));
        resourceCreators.add(new NotificationFunctionResourceCreator(updateResources, nimbusState, processingEnv));
        resourceCreators.add(new QueueFunctionResourceCreator(updateResources, nimbusState, processingEnv, savedResources));
        resourceCreators.add(new BasicFunctionResourceCreator(updateResources, nimbusState, processingEnv));

        List<FunctionInformation> allInformation = new LinkedList<>();
        for (FunctionResourceCreator creator : resourceCreators) {
            allInformation.addAll(creator.handle(roundEnv, functionEnvironmentService));
        }

        handleUseResources(allInformation, updateResources);


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

    private void handleUseResources(List<FunctionInformation> functionInformationList, ResourceCollection updateResources) {
        for (FunctionInformation functionInformation : functionInformationList) {
            handleUseResources(functionInformation.getElement(), functionInformation.getResource(), updateResources);
        }
    }

    private void handleRelationalDatabase(RoundEnvironment roundEnv, ResourceCollection updateResources) {
        Set<? extends Element> annotatedElements = roundEnv.getElementsAnnotatedWith(RelationalDatabase.class);

        for (Element type : annotatedElements) {
            RelationalDatabase relationalDatabase = type.getAnnotation(RelationalDatabase.class);

            DatabaseConfiguration databaseConfiguration = new DatabaseConfiguration(
                    relationalDatabase.name(),
                    relationalDatabase.username(),
                    relationalDatabase.password(),
                    relationalDatabase.databaseLanguage(),
                    relationalDatabase.databaseSize(),
                    relationalDatabase.allocatedSizeGB());

            Vpc vpc = new Vpc(nimbusState);
            SecurityGroupResource securityGroupResource = new SecurityGroupResource(vpc, nimbusState);
            Subnet publicSubnet = new Subnet(vpc, "a","10.0.1.0/24", nimbusState);
            Subnet publicSubnet2 = new Subnet(vpc, "b", "10.0.0.0/24", nimbusState);
            List<Subnet> subnets = new LinkedList<>();
            subnets.add(publicSubnet);
            subnets.add(publicSubnet2);

            SubnetGroup subnetGroup = new SubnetGroup(subnets, nimbusState);
            RdsResource rdsResource = new RdsResource(databaseConfiguration, securityGroupResource, subnetGroup, nimbusState);

            InternetGateway internetGateway = new InternetGateway(nimbusState);
            VpcGatewayAttachment vpcGatewayAttachment = new VpcGatewayAttachment(vpc, internetGateway, nimbusState);
            RouteTable table = new RouteTable(vpc, nimbusState);
            Route route = new Route(table, internetGateway, nimbusState);
            RouteTableAssociation rta1 = new RouteTableAssociation(table, publicSubnet, nimbusState);
            RouteTableAssociation rta2 = new RouteTableAssociation(table, publicSubnet2, nimbusState);

            updateResources.addResource(vpc);
            updateResources.addResource(publicSubnet);
            updateResources.addResource(publicSubnet2);
            updateResources.addResource(subnetGroup);
            updateResources.addResource(securityGroupResource);
            updateResources.addResource(rdsResource);
            updateResources.addResource(internetGateway);
            updateResources.addResource(vpcGatewayAttachment);
            updateResources.addResource(table);
            updateResources.addResource(route);
            updateResources.addResource(rta1);
            updateResources.addResource(rta2);
        }
    }

    private void handleDocumentStore(RoundEnvironment roundEnv, ResourceCollection updateResources) {
        Set<? extends Element> annotatedElements = roundEnv.getElementsAnnotatedWith(DocumentStore.class);

        for (Element type : annotatedElements) {
            DocumentStore documentStore = type.getAnnotation(DocumentStore.class);

            String tableName = determineTableName(documentStore.tableName(), type.getSimpleName().toString());
            DynamoResource dynamoResource = new DynamoResource(tableName, nimbusState);

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
                String tableName = determineTableName(keyValueStore.tableName(), type.getSimpleName().toString());
                DynamoResource dynamoResource = new DynamoResource(tableName, nimbusState);

                DataModelAnnotation dataModelAnnotation = new KeyValueStoreAnnotation(keyValueStore);
                TypeElement element = dataModelAnnotation.getTypeElement(processingEnv);

                dynamoResource.addHashKeyClass(keyValueStore.keyName(), element);
                updateResources.addResource(dynamoResource);
            }
        }
    }

    private void handleUseResources(Element serverlessMethod, FunctionResource functionResource, ResourceCollection updateResources) {
        IamRoleResource iamRoleResource = functionResource.getIamRoleResource();
        ResourceFinder resourceFinder = new ResourceFinder(updateResources, processingEnv, nimbusState);
        for (UsesDocumentStore usesDocumentStore : serverlessMethod.getAnnotationsByType(UsesDocumentStore.class)) {

            DataModelAnnotation dataModelAnnotation = new UsesDocumentStoreAnnotation(usesDocumentStore);
            Resource resource = resourceFinder.getDocumentStoreResource(dataModelAnnotation, serverlessMethod);

            if (resource != null) {
                iamRoleResource.addAllowStatement("dynamodb:*", resource, "");
            }
        }
        for (UsesKeyValueStore usesKeyValueStore : serverlessMethod.getAnnotationsByType(UsesKeyValueStore.class)) {
            DataModelAnnotation annotation = new UsesKeyValueStoreAnnotation(usesKeyValueStore);
            Resource resource = resourceFinder.getKeyValueStoreResource(annotation, serverlessMethod);

            if (resource != null) {
                iamRoleResource.addAllowStatement("dynamodb:*", resource, "");
            }
        }

        for (UsesRelationalDatabase usesRelationalDatabase : serverlessMethod.getAnnotationsByType(UsesRelationalDatabase.class)) {
            DataModelAnnotation annotation = new UsesRelationalDatabaseAnnotation(usesRelationalDatabase);
            Resource resource = resourceFinder.getRelationalDatabaseResource(annotation, serverlessMethod);

            if (resource != null) {
                functionResource.addEnvVariable(resource.getName() + "_CONNECTION_URL", resource.getAttribute("Endpoint.Address"));
                functionResource.addDependsOn(resource);
            }
        }

        for (UsesBasicServerlessFunctionClient ignored : serverlessMethod.getAnnotationsByType(UsesBasicServerlessFunctionClient.class)) {
            functionResource.addEnvVariable("NIMBUS_PROJECT_NAME", nimbusState.getProjectName());
            List<Resource> invokableFunctions = updateResources.getInvokableFunctions();
            for (Resource invokableFunction : invokableFunctions) {
                functionResource.getIamRoleResource().addAllowStatement("lambda:*", invokableFunction, "");
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
            iamRoleResource.addAllowStatement("sqs:SendMessage", referencedQueue, "");
        }

        for (UsesNotificationTopic notificationTopic: serverlessMethod.getAnnotationsByType(UsesNotificationTopic.class)) {

            SnsTopicResource snsTopicResource = new SnsTopicResource(notificationTopic.topic(), null, nimbusState);
            updateResources.addResource(snsTopicResource);

            functionResource.addEnvVariable("SNS_TOPIC_ARN_" + notificationTopic.topic().toUpperCase(), snsTopicResource.getArn(""));
            iamRoleResource.addAllowStatement("sns:Subscribe", snsTopicResource, "");
            iamRoleResource.addAllowStatement("sns:Unsubscribe", snsTopicResource, "");
            iamRoleResource.addAllowStatement("sns:Publish", snsTopicResource, "");
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

