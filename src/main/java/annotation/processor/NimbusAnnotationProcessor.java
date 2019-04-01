package annotation.processor;

import annotation.annotations.database.UsesRelationalDatabase;
import annotation.annotations.document.UsesDocumentStore;
import annotation.annotations.file.UsesFileStorageClient;
import annotation.annotations.function.UsesBasicServerlessFunctionClient;
import annotation.annotations.keyvalue.UsesKeyValueStore;
import annotation.annotations.notification.UsesNotificationTopic;
import annotation.annotations.queue.UsesQueue;
import annotation.annotations.websocket.UsesServerlessFunctionWebSocketClient;
import annotation.services.CloudformationWriter;
import annotation.services.FunctionEnvironmentService;
import annotation.services.ReadUserConfigService;
import annotation.services.ResourceFinder;
import annotation.services.functions.*;
import annotation.services.resources.*;
import annotation.wrappers.annotations.datamodel.DataModelAnnotation;
import annotation.wrappers.annotations.datamodel.UsesDocumentStoreAnnotation;
import annotation.wrappers.annotations.datamodel.UsesKeyValueStoreAnnotation;
import annotation.wrappers.annotations.datamodel.UsesRelationalDatabaseAnnotation;
import cloudformation.CloudFormationDocuments;
import cloudformation.CloudFormationTemplate;
import cloudformation.resource.IamRoleResource;
import cloudformation.resource.Resource;
import cloudformation.resource.ResourceCollection;
import cloudformation.resource.database.RdsResource;
import cloudformation.resource.file.FileBucket;
import cloudformation.resource.function.FunctionResource;
import cloudformation.resource.notification.SnsTopicResource;
import cloudformation.resource.queue.QueueResource;
import cloudformation.resource.websocket.WebSocketApi;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auto.service.AutoService;
import persisted.NimbusState;
import persisted.UserConfig;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.text.SimpleDateFormat;
import java.util.*;

@SupportedAnnotationTypes({
        "annotation.annotations.function.HttpServerlessFunction",
        "annotation.annotations.function.repeatable.HttpServerlessFunctions",
        "annotation.annotations.function.QueueServerlessFunction",
        "annotation.annotations.function.repeatable.QueueServerlessFunctions",
        "annotation.annotations.function.DocumentStoreServerlessFunction",
        "annotation.annotations.function.repeatable.DocumentStoreServerlessFunctions",
        "annotation.annotations.function.NotificationServerlessFunction",
        "annotation.annotations.function.repeatable.NotificationServerlessFunctions",
        "annotation.annotations.function.BasicServerlessFunction",
        "annotation.annotations.function.repeatable.BasicServerlessFunctions",
        "annotation.annotations.function.KeyValueStoreServerlessFunction",
        "annotation.annotations.function.repeatable.KeyValueStoreServerlessFunctions",
        "annotation.annotations.function.FileStorageServerlessFunction",
        "annotation.annotations.function.repeatable.FileStorageServerlessFunctions",
        "annotation.annotations.function.WebSocketServerlessFunction",
        "annotation.annotations.function.repeatable.WebSocketServerlessFunctions",
        "annotation.annotations.dynamo.KeyValueStore",
        "annotation.annotations.dynamo.KeyValueStores",
        "annotation.annotations.dynamo.DocumentStore",
        "annotation.annotations.dynamo.DocumentStores",
        "annotation.annotations.database.RelationalDatabase",
        "annotation.annotations.database.RelationalDatabases",
        "annotation.annotations.deployment.FileUpload",
        "annotation.annotations.deployment.FileUploads",
        "annotation.annotations.deployment.AfterDeployment",
        "annotation.annotations.deployment.AfterDeployments",
        "annotation.annotations.file.FileStorageBucket",
        "annotation.annotations.file.FileStorageBuckets"
})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@AutoService(Processor.class)
public class NimbusAnnotationProcessor extends AbstractProcessor {

    private NimbusState nimbusState = null;

    private CloudformationWriter cloudformationWriter;

    private UserConfig userConfig;

    private Map<String, CloudFormationDocuments> cfDocuments = new HashMap<>();

    private Messager messager;

    @Override
    public synchronized void init(final ProcessingEnvironment processingEnv) {
        super.init(processingEnv);

        messager = processingEnv.getMessager();

        cloudformationWriter = new CloudformationWriter(processingEnv.getFiler());
        userConfig = new ReadUserConfigService().readUserConfig();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        if (nimbusState == null) {
            Calendar cal = Calendar.getInstance();
            SimpleDateFormat simpleDateFormat =
                    new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSzzz", Locale.US);

            String compilationTime = simpleDateFormat.format(cal.getTime());
            nimbusState = new NimbusState(userConfig.getProjectName(), compilationTime, new HashMap<>(), new HashMap<>(), false);
        }

        FunctionEnvironmentService functionEnvironmentService = new FunctionEnvironmentService(
                cfDocuments,
                nimbusState
        );


        List<CloudResourceResourceCreator> resourceCreators = new LinkedList<>();
        resourceCreators.add(new DocumentStoreResourceCreator(roundEnv, cfDocuments, nimbusState));
        resourceCreators.add(new KeyValueStoreResourceCreator(roundEnv, cfDocuments, nimbusState, processingEnv));
        resourceCreators.add(new RelationalDatabaseResourceCreator(roundEnv, cfDocuments, nimbusState));
        resourceCreators.add(new FileStorageBucketResourceCreator(roundEnv, cfDocuments, nimbusState));

        for (CloudResourceResourceCreator creator : resourceCreators) {
            creator.create();
        }

        List<FunctionResourceCreator> functionResourceCreators = new LinkedList<>();
        functionResourceCreators.add(new DocumentStoreFunctionResourceCreator(cfDocuments, nimbusState, processingEnv));
        functionResourceCreators.add(new KeyValueStoreFunctionResourceCreator(cfDocuments, nimbusState, processingEnv));
        functionResourceCreators.add(new HttpFunctionResourceCreator(cfDocuments, nimbusState, processingEnv));
        functionResourceCreators.add(new NotificationFunctionResourceCreator(cfDocuments, nimbusState, processingEnv));
        functionResourceCreators.add(new QueueFunctionResourceCreator(cfDocuments, nimbusState, processingEnv));
        functionResourceCreators.add(new BasicFunctionResourceCreator(cfDocuments, nimbusState, processingEnv));
        functionResourceCreators.add(new FileStorageResourceCreator(cfDocuments, nimbusState, processingEnv));
        functionResourceCreators.add(new WebSocketFunctionResourceCreator(cfDocuments, nimbusState, processingEnv));

        functionResourceCreators.add(new FileUploadResourceCreator(cfDocuments, nimbusState, processingEnv));
        functionResourceCreators.add(new AfterDeploymentResourceCreator(cfDocuments, nimbusState, processingEnv));

        List<FunctionInformation> allInformation = new LinkedList<>();
        for (FunctionResourceCreator creator : functionResourceCreators) {
            allInformation.addAll(creator.handle(roundEnv, functionEnvironmentService));
        }

        handleUseResources(allInformation);

        if (roundEnv.processingOver()) {

            for (Map.Entry<String, CloudFormationDocuments> entry : cfDocuments.entrySet()) {
                String stage = entry.getKey();
                CloudFormationDocuments cloudFormationDocuments = entry.getValue();

                CloudFormationTemplate update = new CloudFormationTemplate(cloudFormationDocuments.getUpdateResources(), cloudFormationDocuments.getUpdateOutputs());
                CloudFormationTemplate create = new CloudFormationTemplate(cloudFormationDocuments.getCreateResources(), cloudFormationDocuments.getCreateOutputs());

                cloudformationWriter.saveTemplate("cloudformation-stack-update-" + stage, update);
                cloudformationWriter.saveTemplate("cloudformation-stack-create-" + stage, create);
            }

            ObjectMapper mapper = new ObjectMapper();
            try {
                cloudformationWriter.saveJsonFile("nimbus-state", mapper.writeValueAsString(nimbusState));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    private void handleUseResources(List<FunctionInformation> functionInformationList) {
        for (FunctionInformation functionInformation : functionInformationList) {
            handleUseResources(functionInformation.getElement(), functionInformation.getResource());
        }
    }

    private void handleUseResources(Element serverlessMethod, FunctionResource functionResource) {
        IamRoleResource iamRoleResource = functionResource.getIamRoleResource();
        ResourceFinder resourceFinder = new ResourceFinder(cfDocuments, processingEnv, nimbusState);
        for (UsesDocumentStore usesDocumentStore : serverlessMethod.getAnnotationsByType(UsesDocumentStore.class)) {
            for (String stage : usesDocumentStore.stages()) {
                if (stage.equals(functionResource.getStage())) {
                    DataModelAnnotation dataModelAnnotation = new UsesDocumentStoreAnnotation(usesDocumentStore);
                    Resource resource = resourceFinder.getDocumentStoreResource(dataModelAnnotation, serverlessMethod, stage);

                    if (resource != null) {
                        iamRoleResource.addAllowStatement("dynamodb:*", resource, "");
                    }
                }
            }
        }
        for (UsesKeyValueStore usesKeyValueStore : serverlessMethod.getAnnotationsByType(UsesKeyValueStore.class)) {
            for (String stage : usesKeyValueStore.stages()) {
                if (stage.equals(functionResource.getStage())) {
                    DataModelAnnotation annotation = new UsesKeyValueStoreAnnotation(usesKeyValueStore);
                    Resource resource = resourceFinder.getKeyValueStoreResource(annotation, serverlessMethod, stage);

                    if (resource != null) {
                        iamRoleResource.addAllowStatement("dynamodb:*", resource, "");
                    }
                }
            }
        }

        for (UsesRelationalDatabase usesRelationalDatabase : serverlessMethod.getAnnotationsByType(UsesRelationalDatabase.class)) {
            for (String stage : usesRelationalDatabase.stages()) {
                if (stage.equals(functionResource.getStage())) {

                    DataModelAnnotation annotation = new UsesRelationalDatabaseAnnotation(usesRelationalDatabase);
                    RdsResource resource = resourceFinder.getRelationalDatabaseResource(annotation, serverlessMethod, stage);

                    if (resource != null) {
                        functionResource.addEnvVariable(resource.getName() + "_CONNECTION_URL", resource.getAttribute("Endpoint.Address"));
                        functionResource.addEnvVariable(resource.getName() + "_PASSWORD", resource.getDatabaseConfiguration().getPassword());
                        functionResource.addEnvVariable(resource.getName() + "_USERNAME", resource.getDatabaseConfiguration().getUsername());
                        functionResource.addDependsOn(resource);
                    }
                }
            }
        }

        for (UsesBasicServerlessFunctionClient usesBasicServerlessFunctionClient : serverlessMethod.getAnnotationsByType(UsesBasicServerlessFunctionClient.class)) {
            for (String stage : usesBasicServerlessFunctionClient.stages()) {
                if (stage.equals(functionResource.getStage())) {

                    functionResource.addEnvVariable("NIMBUS_PROJECT_NAME", nimbusState.getProjectName());
                    functionResource.addEnvVariable("FUNCTION_STAGE", stage);
                    ResourceCollection updateResources = cfDocuments.get(stage).getUpdateResources();
                    List<Resource> invokableFunctions = updateResources.getInvokableFunctions();
                    for (Resource invokableFunction : invokableFunctions) {
                        functionResource.getIamRoleResource().addAllowStatement("lambda:*", invokableFunction, "");
                    }
                }
            }
        }

        for (UsesQueue usesQueue : serverlessMethod.getAnnotationsByType(UsesQueue.class)) {
            for (String stage : usesQueue.stages()) {
                if (stage.equals(functionResource.getStage())) {

                    CloudFormationDocuments cloudFormationDocuments = cfDocuments.get(stage);

                    if (cloudFormationDocuments == null || usesQueue.id().equals("") || cloudFormationDocuments.getSavedResources().get(usesQueue.id()) == null) {
                        messager.printMessage(Diagnostic.Kind.ERROR, "Unable to find id of queue, have you set it in the @QueueServerlessFunction?", serverlessMethod);
                        return;
                    }

                    Resource referencedQueue = cloudFormationDocuments.getSavedResources().get(usesQueue.id());
                    if (!(referencedQueue instanceof QueueResource)) {
                        messager.printMessage(Diagnostic.Kind.ERROR, "Resource with id " + usesQueue.id() + " is not a Queue", serverlessMethod);
                        return;
                    }

                    functionResource.addEnvVariable("NIMBUS_QUEUE_URL_ID_" + usesQueue.id().toUpperCase(), referencedQueue.getRef());
                    iamRoleResource.addAllowStatement("sqs:SendMessage", referencedQueue, "");
                }
            }
        }

        for (UsesNotificationTopic notificationTopic: serverlessMethod.getAnnotationsByType(UsesNotificationTopic.class)) {
            for (String stage : notificationTopic.stages()) {
                if (stage.equals(functionResource.getStage())) {

                    SnsTopicResource snsTopicResource = new SnsTopicResource(notificationTopic.topic(), null, nimbusState, stage);
                    CloudFormationDocuments cloudFormationDocuments = cfDocuments.get(stage);
                    if (cloudFormationDocuments == null) {
                        messager.printMessage(Diagnostic.Kind.ERROR, "No serverless function annotation found for UsesNotificationTopic", serverlessMethod);
                        return;
                    }
                    cloudFormationDocuments.getUpdateResources().addResource(snsTopicResource);

                    functionResource.addEnvVariable("NIMBUS_STAGE", stage);
                    functionResource.addEnvVariable("SNS_TOPIC_ARN_" + notificationTopic.topic().toUpperCase(), snsTopicResource.getRef());
                    iamRoleResource.addAllowStatement("sns:Subscribe", snsTopicResource, "");
                    iamRoleResource.addAllowStatement("sns:Unsubscribe", snsTopicResource, "");
                    iamRoleResource.addAllowStatement("sns:Publish", snsTopicResource, "");
                }
            }
        }

        for (UsesFileStorageClient fileStorage: serverlessMethod.getAnnotationsByType(UsesFileStorageClient.class)) {
            for (String stage : fileStorage.stages()) {
                if (stage.equals(functionResource.getStage())) {

                    FileBucket newBucket = new FileBucket(nimbusState, fileStorage.bucketName(), stage);

                    ResourceCollection updateResources = cfDocuments.get(stage).getUpdateResources();
                    FileBucket existingBucket = (FileBucket) updateResources.get(newBucket.getName());

                    FileBucket permissionsBucket = existingBucket;
                    if (existingBucket == null) {
                        updateResources.addResource(newBucket);
                        permissionsBucket = newBucket;
                    }
                    functionResource.addEnvVariable("NIMBUS_STAGE", stage);
                    iamRoleResource.addAllowStatement("s3:GetObject", permissionsBucket, "");
                    iamRoleResource.addAllowStatement("s3:DeleteObject", permissionsBucket, "");
                    iamRoleResource.addAllowStatement("s3:PutObject", permissionsBucket, "");
                    iamRoleResource.addAllowStatement("s3:GetObject", permissionsBucket, "/*");
                    iamRoleResource.addAllowStatement("s3:DeleteObject", permissionsBucket, "/*");
                    iamRoleResource.addAllowStatement("s3:PutObject", permissionsBucket, "/*");
                }
            }
        }

        for (UsesServerlessFunctionWebSocketClient webSocketClient: serverlessMethod.getAnnotationsByType(UsesServerlessFunctionWebSocketClient.class)) {
            for (String stage : webSocketClient.stages()) {
                if (stage.equals(functionResource.getStage())) {

                    WebSocketApi webSocketApi = cfDocuments.get(stage).getRootWebSocketApi();
                    if (webSocketApi != null) {
                        functionResource.addEnvVariable("WEBSOCKET_ENDPOINT", webSocketApi.getEndpoint());

                        iamRoleResource.addAllowStatement("execute-api:ManageConnections", webSocketApi, "/*");
                    }
                }
            }
        }
    }

}

