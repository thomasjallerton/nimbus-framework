package com.nimbusframework.nimbusaws.annotation.processor;

import com.fasterxml.jackson.jr.ob.JSON;
import com.google.auto.service.AutoService;
import com.nimbusframework.nimbusaws.cloudformation.generation.abstractions.ClassForReflectionService;
import com.nimbusframework.nimbusaws.cloudformation.generation.files.FileWriter;
import com.nimbusframework.nimbusaws.cloudformation.generation.abstractions.FunctionEnvironmentService;
import com.nimbusframework.nimbusaws.cloudformation.generation.abstractions.FunctionResourceCreator;
import com.nimbusframework.nimbusaws.cloudformation.generation.abstractions.UsesResourcesProcessor;
import com.nimbusframework.nimbusaws.cloudformation.generation.resources.*;
import com.nimbusframework.nimbusaws.cloudformation.generation.resources.apigateway.ApiGatewayConfigResourceCreator;
import com.nimbusframework.nimbusaws.cloudformation.generation.resources.apigateway.HttpFunctionResourceCreator;
import com.nimbusframework.nimbusaws.cloudformation.generation.resources.apigateway.UsesServerlessFunctionWebSocketClientProcessor;
import com.nimbusframework.nimbusaws.cloudformation.generation.resources.apigateway.WebSocketFunctionResourceCreator;
import com.nimbusframework.nimbusaws.cloudformation.generation.resources.basicfunction.AfterDeploymentResourceCreator;
import com.nimbusframework.nimbusaws.cloudformation.generation.resources.basicfunction.BasicFunctionResourceCreator;
import com.nimbusframework.nimbusaws.cloudformation.generation.resources.basicfunction.UsesBasicServerlessFunctionClientProcessor;
import com.nimbusframework.nimbusaws.cloudformation.generation.resources.cognito.ExistingCognitoResourceCreator;
import com.nimbusframework.nimbusaws.cloudformation.generation.resources.cognito.UsesCognitoProcessor;
import com.nimbusframework.nimbusaws.cloudformation.generation.resources.cognito.UsesDocumentStoreProcessor;
import com.nimbusframework.nimbusaws.cloudformation.generation.resources.database.RelationalDatabaseResourceCreator;
import com.nimbusframework.nimbusaws.cloudformation.generation.resources.database.UsesRelationalDatabaseProcessor;
import com.nimbusframework.nimbusaws.cloudformation.generation.resources.dynamo.*;
import com.nimbusframework.nimbusaws.cloudformation.generation.resources.environment.EnvironmentVariablesProcessor;
import com.nimbusframework.nimbusaws.cloudformation.generation.resources.filestoragebucket.FileStorageBucketResourceCreator;
import com.nimbusframework.nimbusaws.cloudformation.generation.resources.filestoragebucket.FileStorageResourceCreator;
import com.nimbusframework.nimbusaws.cloudformation.generation.resources.filestoragebucket.FileUploadResourceCreator;
import com.nimbusframework.nimbusaws.cloudformation.generation.resources.filestoragebucket.UsesFileStorageClientProcessor;
import com.nimbusframework.nimbusaws.cloudformation.generation.resources.notification.NotificationFunctionResourceCreator;
import com.nimbusframework.nimbusaws.cloudformation.generation.resources.notification.NotificationTopicResourceCreator;
import com.nimbusframework.nimbusaws.cloudformation.generation.resources.notification.UsesNotificationTopicProcessor;
import com.nimbusframework.nimbusaws.cloudformation.generation.resources.queue.QueueFunctionResourceCreator;
import com.nimbusframework.nimbusaws.cloudformation.generation.resources.queue.QueueResourceCreator;
import com.nimbusframework.nimbusaws.cloudformation.generation.resources.queue.UsesQueueProcessor;
import com.nimbusframework.nimbusaws.cloudformation.generation.resources.secretsmanager.UsesSecretsManagerProcessor;
import com.nimbusframework.nimbuscore.services.ReadUserConfigService;
import com.nimbusframework.nimbusaws.cloudformation.generation.abstractions.ResourceFinder;
import com.nimbusframework.nimbusaws.cloudformation.generation.files.NativeImageReflectionWriter;
import com.nimbusframework.nimbusaws.cloudformation.generation.abstractions.FunctionDecoratorHandler;
import com.nimbusframework.nimbusaws.cloudformation.generation.abstractions.KeepWarmDecoratorHandler;
import com.nimbusframework.nimbusaws.cloudformation.model.CloudFormationFiles;
import com.nimbusframework.nimbusaws.wrappers.customRuntimeEntry.CustomRuntimeEntryFileBuilder;
import com.nimbusframework.nimbuscore.persisted.CloudProvider;
import com.nimbusframework.nimbuscore.persisted.NimbusState;
import com.nimbusframework.nimbuscore.persisted.userconfig.UserConfig;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

@SupportedAnnotationTypes({
        "com.nimbusframework.nimbuscore.annotations.function.HttpServerlessFunction",
        "com.nimbusframework.nimbuscore.annotations.function.repeatable.HttpServerlessFunctions",
        "com.nimbusframework.nimbuscore.annotations.function.QueueServerlessFunction",
        "com.nimbusframework.nimbuscore.annotations.function.repeatable.QueueServerlessFunctions",
        "com.nimbusframework.nimbuscore.annotations.function.DocumentStoreServerlessFunction",
        "com.nimbusframework.nimbuscore.annotations.function.repeatable.DocumentStoreServerlessFunctions",
        "com.nimbusframework.nimbuscore.annotations.function.NotificationServerlessFunction",
        "com.nimbusframework.nimbuscore.annotations.function.repeatable.NotificationServerlessFunctions",
        "com.nimbusframework.nimbuscore.annotations.function.BasicServerlessFunction",
        "com.nimbusframework.nimbuscore.annotations.function.repeatable.BasicServerlessFunctions",
        "com.nimbusframework.nimbuscore.annotations.function.KeyValueStoreServerlessFunction",
        "com.nimbusframework.nimbuscore.annotations.function.repeatable.KeyValueStoreServerlessFunctions",
        "com.nimbusframework.nimbuscore.annotations.function.FileStorageServerlessFunction",
        "com.nimbusframework.nimbuscore.annotations.function.repeatable.FileStorageServerlessFunctions",
        "com.nimbusframework.nimbuscore.annotations.function.WebSocketServerlessFunction",
        "com.nimbusframework.nimbuscore.annotations.function.repeatable.WebSocketServerlessFunctions",
        "com.nimbusframework.nimbuscore.annotations.dynamo.KeyValueStoreDefinition",
        "com.nimbusframework.nimbuscore.annotations.dynamo.KeyValueStoreDefinitions",
        "com.nimbusframework.nimbuscore.annotations.dynamo.DocumentStoreDefinition",
        "com.nimbusframework.nimbuscore.annotations.dynamo.DocumentStoreDefinitions",
        "com.nimbusframework.nimbuscore.annotations.queue.QueueDefinition",
        "com.nimbusframework.nimbuscore.annotations.queue.QueueDefinitions",
        "com.nimbusframework.nimbuscore.annotations.notification.NotificationTopicDefinition",
        "com.nimbusframework.nimbuscore.annotations.notification.NotificationTopicDefinitions",
        "com.nimbusframework.nimbuscore.annotations.database.RelationalDatabaseDefinition",
        "com.nimbusframework.nimbuscore.annotations.database.RelationalDatabaseDefinitions",
        "com.nimbusframework.nimbuscore.annotations.deployment.FileUpload",
        "com.nimbusframework.nimbuscore.annotations.deployment.FileUploads",
        "com.nimbusframework.nimbuscore.annotations.deployment.AfterDeployment",
        "com.nimbusframework.nimbuscore.annotations.deployment.AfterDeployments",
        "com.nimbusframework.nimbuscore.annotations.deployment.ForceDependency",
        "com.nimbusframework.nimbuscore.annotations.deployment.ForceDependencies",
        "com.nimbusframework.nimbuscore.annotations.file.FileStorageBucketDefinition",
        "com.nimbusframework.nimbuscore.annotations.file.FileStorageBucketDefinitions",
        "com.nimbusframework.nimbusaws.annotation.annotations.keyvalue.DynamoDbKeyValueStore",
        "com.nimbusframework.nimbusaws.annotation.annotations.keyvalue.DynamoDbKeyValueStores",
        "com.nimbusframework.nimbusaws.annotation.annotations.document.DynamoDbDocumentStore",
        "com.nimbusframework.nimbusaws.annotation.annotations.document.DynamoDbDocumentStores",
        "com.nimbusframework.nimbusaws.annotation.annotations.database.RdsDatabase",
        "com.nimbusframework.nimbusaws.annotation.annotations.database.RdsDatabases",
        "com.nimbusframework.nimbusaws.annotation.annotations.nativeimage.RegisterForReflection"
})
@SupportedSourceVersion(SourceVersion.RELEASE_11)
@AutoService(Processor.class)
public class NimbusAnnotationProcessor extends AbstractProcessor {

    private ProcessingData processingData = null;

    private FileWriter fileWriter;

    private CustomRuntimeEntryFileBuilder customRuntimeEntryFileBuilder;

    private NativeImageReflectionWriter nativeImageReflectionWriter;

    private ClassForReflectionService classForReflectionService;

    private UserConfig userConfig;

    private Map<String, CloudFormationFiles> cloudFormationFiles = new HashMap<>();

    private List<FunctionInformation> functions = new LinkedList<>();

    private UserConfigValidator userConfigValidator = new UserConfigValidator();

    private Messager messager;

    public NimbusAnnotationProcessor() {}

    public NimbusAnnotationProcessor(UserConfig config) {
        this.userConfig = config;
    }

    @Override
    public synchronized void init(final ProcessingEnvironment processingEnv) {
        super.init(processingEnv);

        messager = processingEnv.getMessager();

        fileWriter = new FileWriter(processingEnv.getFiler());
        nativeImageReflectionWriter = new NativeImageReflectionWriter(fileWriter);
        if (userConfig == null) {
            userConfig = new ReadUserConfigService().readUserConfig();
        }

        userConfigValidator.validateUserConfig(userConfig, messager);

        Calendar cal = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat =
                new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSzzz", Locale.US);

        String compilationTime = simpleDateFormat.format(cal.getTime());

        processingData = new ProcessingData(
                new NimbusState(
                        userConfig.getProjectName(),
                        CloudProvider.AWS,
                        compilationTime,
                        userConfig.getDefaultStages(),
                        userConfig.getKeepWarmStages(),
                        new HashMap<>(),
                        new HashMap<>(),
                        new HashMap<>(),
                        new HashSet<>(),
                        userConfig.getCustomRuntime(),
                        userConfig.getLogGroupRetentionInDays()
                ),
                userConfig.getHttpErrorMessageType(),
                new HashSet<>(),
                new HashSet<>(),
                userConfig.getAllowedHeaders(),
                userConfig.getAllowedOrigins()
        );

        classForReflectionService = new ClassForReflectionService(processingData, processingEnv.getTypeUtils());

        customRuntimeEntryFileBuilder = new CustomRuntimeEntryFileBuilder(processingEnv);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        FunctionEnvironmentService functionEnvironmentService = new FunctionEnvironmentService(
                cloudFormationFiles,
                processingData
        );

        List<CloudResourceResourceCreator> resourceCreators = new LinkedList<>();
        resourceCreators.add(new DocumentStoreResourceCreator(roundEnv, cloudFormationFiles, processingData, classForReflectionService));
        resourceCreators.add(new KeyValueStoreResourceCreator(roundEnv, cloudFormationFiles, processingData, classForReflectionService, processingEnv));
        resourceCreators.add(new NotificationTopicResourceCreator(roundEnv, cloudFormationFiles, processingData.getNimbusState()));
        resourceCreators.add(new QueueResourceCreator(roundEnv, cloudFormationFiles, processingData.getNimbusState()));
        resourceCreators.add(new RelationalDatabaseResourceCreator(roundEnv, cloudFormationFiles, processingData.getNimbusState()));
        resourceCreators.add(new FileStorageBucketResourceCreator(roundEnv, cloudFormationFiles, processingData.getNimbusState()));
        resourceCreators.add(new ExistingCognitoResourceCreator(roundEnv, cloudFormationFiles, processingData.getNimbusState()));
        resourceCreators.add(new RegisterForReflectionResourceCreator(roundEnv, cloudFormationFiles, processingData.getNimbusState(), classForReflectionService));
        resourceCreators.add(new ApiGatewayConfigResourceCreator(roundEnv, cloudFormationFiles, processingData, processingEnv, messager, functionEnvironmentService));

        for (CloudResourceResourceCreator creator : resourceCreators) {
            creator.create();
        }

        Set<FunctionDecoratorHandler> functionDecoratorHandlers = new HashSet<>();
        functionDecoratorHandlers.add(new KeepWarmDecoratorHandler(processingData.getNimbusState(), functionEnvironmentService));

        ResourceFinder resourceFinder = new ResourceFinder(cloudFormationFiles, processingEnv, processingData.getNimbusState());

        List<FunctionResourceCreator> functionResourceCreators = new LinkedList<>();
        functionResourceCreators.add(new DocumentStoreFunctionResourceCreator(cloudFormationFiles, processingData, classForReflectionService, processingEnv, functionDecoratorHandlers, messager, resourceFinder));
        functionResourceCreators.add(new KeyValueStoreFunctionResourceCreator(cloudFormationFiles, processingData, classForReflectionService, processingEnv, functionDecoratorHandlers, messager, resourceFinder));
        functionResourceCreators.add(new HttpFunctionResourceCreator(cloudFormationFiles, processingData, classForReflectionService, processingEnv, functionDecoratorHandlers, messager));
        functionResourceCreators.add(new NotificationFunctionResourceCreator(cloudFormationFiles, processingData, classForReflectionService, processingEnv, functionDecoratorHandlers, messager, resourceFinder));
        functionResourceCreators.add(new QueueFunctionResourceCreator(cloudFormationFiles, processingData, processingEnv, classForReflectionService, functionDecoratorHandlers, messager, resourceFinder));
        functionResourceCreators.add(new BasicFunctionResourceCreator(cloudFormationFiles, processingData, classForReflectionService, processingEnv, functionDecoratorHandlers, messager));
        functionResourceCreators.add(new FileStorageResourceCreator(cloudFormationFiles, processingData, classForReflectionService, processingEnv, functionDecoratorHandlers, messager, resourceFinder));
        functionResourceCreators.add(new WebSocketFunctionResourceCreator(cloudFormationFiles, processingData, classForReflectionService, processingEnv, functionDecoratorHandlers, messager));

        functionResourceCreators.add(new FileUploadResourceCreator(cloudFormationFiles, processingData, processingEnv, functionDecoratorHandlers, messager, resourceFinder));
        functionResourceCreators.add(new AfterDeploymentResourceCreator(cloudFormationFiles, processingData, classForReflectionService, processingEnv, functionDecoratorHandlers, messager));

        List<FunctionInformation> allInformation = new LinkedList<>(processingData.getAdditionalFunctions());
        for (FunctionResourceCreator creator : functionResourceCreators) {
            allInformation.addAll(creator.handle(roundEnv, functionEnvironmentService));
        }
        functions.addAll(allInformation);

        List<UsesResourcesProcessor> usesResourcesProcessors = new LinkedList<>();
        usesResourcesProcessors.add(new UsesBasicServerlessFunctionClientProcessor(cloudFormationFiles, processingEnv, processingData.getNimbusState(), messager));
        usesResourcesProcessors.add(new UsesDocumentStoreProcessor(cloudFormationFiles, processingEnv, processingData.getNimbusState(), messager));
        usesResourcesProcessors.add(new UsesFileStorageClientProcessor(messager, resourceFinder, processingData.getNimbusState()));
        usesResourcesProcessors.add(new UsesKeyValueStoreProcessor(cloudFormationFiles, processingEnv, processingData.getNimbusState(), messager));
        usesResourcesProcessors.add(new UsesNotificationTopicProcessor(messager, resourceFinder, processingData.getNimbusState()));
        usesResourcesProcessors.add(new UsesQueueProcessor(messager, resourceFinder, processingData.getNimbusState()));
        usesResourcesProcessors.add(new UsesCognitoProcessor(cloudFormationFiles, processingEnv, messager, processingData.getNimbusState()));
        usesResourcesProcessors.add(new UsesRelationalDatabaseProcessor(resourceFinder, processingData.getNimbusState()));
        usesResourcesProcessors.add(new UsesServerlessFunctionWebSocketClientProcessor(cloudFormationFiles, processingData.getNimbusState()));
        usesResourcesProcessors.add(new EnvironmentVariablesProcessor(processingData.getNimbusState(), messager));
        usesResourcesProcessors.add(new UsesSecretsManagerProcessor(processingData.getNimbusState()));

        for (FunctionInformation functionInformation : allInformation) {
            for (UsesResourcesProcessor handler : usesResourcesProcessors) {
                handler.handleUseResources(functionInformation.getElement(), functionInformation.getResource());
            }
        }

        if (roundEnv.processingOver()) {
            if (userConfig.getCustomRuntime()) {
                customRuntimeEntryFileBuilder.createCustomRuntimeEntryFunction(functions);
            }

            for (Map.Entry<String, CloudFormationFiles> entry : cloudFormationFiles.entrySet()) {
                String stage = entry.getKey();
                CloudFormationFiles cloudFormationFiles = entry.getValue();

                fileWriter.saveTemplate("cloudformation-stack-update-" + stage, cloudFormationFiles.getUpdateTemplate());
                fileWriter.saveTemplate("cloudformation-stack-create-" + stage, cloudFormationFiles.getCreateTemplate());
            }

            try {
                fileWriter.saveJsonFile("nimbus-state", JSON.std.with(JSON.Feature.PRETTY_PRINT_OUTPUT).asString(processingData.getNimbusState()));
                if (userConfig.getCustomRuntime()) {
                    nativeImageReflectionWriter.writeReflectionConfig(processingData.getClassesForReflection());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

}

