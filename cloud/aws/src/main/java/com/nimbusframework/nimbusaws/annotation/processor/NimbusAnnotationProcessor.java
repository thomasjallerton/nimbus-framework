package com.nimbusframework.nimbusaws.annotation.processor;

import com.fasterxml.jackson.jr.ob.JSON;
import com.google.auto.service.AutoService;
import com.nimbusframework.nimbusaws.annotation.services.dependencies.ClassForReflectionService;
import com.nimbusframework.nimbusaws.annotation.services.files.FileWriter;
import com.nimbusframework.nimbusaws.annotation.services.FunctionEnvironmentService;
import com.nimbusframework.nimbuscore.services.ReadUserConfigService;
import com.nimbusframework.nimbusaws.annotation.services.ResourceFinder;
import com.nimbusframework.nimbusaws.annotation.services.files.NativeImageReflectionWriter;
import com.nimbusframework.nimbusaws.annotation.services.functions.*;
import com.nimbusframework.nimbusaws.annotation.services.functions.decorators.FunctionDecoratorHandler;
import com.nimbusframework.nimbusaws.annotation.services.functions.decorators.KeepWarmDecoratorHandler;
import com.nimbusframework.nimbusaws.annotation.services.resources.*;
import com.nimbusframework.nimbusaws.annotation.services.useresources.*;
import com.nimbusframework.nimbusaws.cloudformation.CloudFormationFiles;
import com.nimbusframework.nimbusaws.wrappers.customRuntimeEntry.CustomRuntimeEntryFileBuilder;
import com.nimbusframework.nimbuscore.persisted.CloudProvider;
import com.nimbusframework.nimbuscore.persisted.NimbusState;
import com.nimbusframework.nimbuscore.persisted.UserConfig;

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

    private Messager messager;

    @Override
    public synchronized void init(final ProcessingEnvironment processingEnv) {
        super.init(processingEnv);

        messager = processingEnv.getMessager();

        fileWriter = new FileWriter(processingEnv.getFiler());
        nativeImageReflectionWriter = new NativeImageReflectionWriter(fileWriter);
        userConfig = new ReadUserConfigService().readUserConfig();

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
                        userConfig.getAssemble(),
                        userConfig.getCustomRuntime()
                ),
                new HashSet<>()
        );

        classForReflectionService = new ClassForReflectionService(processingData, processingEnv.getTypeUtils());

        customRuntimeEntryFileBuilder = new CustomRuntimeEntryFileBuilder(processingEnv);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        FunctionEnvironmentService functionEnvironmentService = new FunctionEnvironmentService(
                cloudFormationFiles,
                processingData.getNimbusState()
        );

        List<CloudResourceResourceCreator> resourceCreators = new LinkedList<>();
        resourceCreators.add(new DocumentStoreResourceCreator(roundEnv, cloudFormationFiles, processingData, classForReflectionService));
        resourceCreators.add(new KeyValueStoreResourceCreator(roundEnv, cloudFormationFiles, processingData, classForReflectionService, processingEnv));
        resourceCreators.add(new NotificationTopicResourceCreator(roundEnv, cloudFormationFiles, processingData.getNimbusState()));
        resourceCreators.add(new QueueResourceCreator(roundEnv, cloudFormationFiles, processingData.getNimbusState()));
        resourceCreators.add(new RelationalDatabaseResourceCreator(roundEnv, cloudFormationFiles, processingData.getNimbusState()));
        resourceCreators.add(new FileStorageBucketResourceCreator(roundEnv, cloudFormationFiles, processingData.getNimbusState()));
        resourceCreators.add(new RegisterForReflectionResourceCreator(roundEnv, cloudFormationFiles, processingData.getNimbusState(), classForReflectionService));

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

        List<FunctionInformation> allInformation = new LinkedList<>();
        for (FunctionResourceCreator creator : functionResourceCreators) {
            allInformation.addAll(creator.handle(roundEnv, functionEnvironmentService));
        }
        functions.addAll(allInformation);

        List<UsesResourcesProcessor> usesResourcesProcessors = new LinkedList<>();
        usesResourcesProcessors.add(new UsesBasicServerlessFunctionClientProcessor(cloudFormationFiles, processingEnv, processingData.getNimbusState(), messager));
        usesResourcesProcessors.add(new UsesDocumentStoreProcessor(cloudFormationFiles, processingEnv, processingData.getNimbusState(), messager));
        usesResourcesProcessors.add(new UsesFileStorageClientProcessor(cloudFormationFiles, messager, resourceFinder, processingData.getNimbusState()));
        usesResourcesProcessors.add(new UsesKeyValueStoreProcessor(cloudFormationFiles, processingEnv, processingData.getNimbusState(), messager));
        usesResourcesProcessors.add(new UsesNotificationTopicProcessor(cloudFormationFiles, messager, resourceFinder, processingData.getNimbusState()));
        usesResourcesProcessors.add(new UsesQueueProcessor(cloudFormationFiles, messager, resourceFinder, processingData.getNimbusState()));
        usesResourcesProcessors.add(new UsesRelationalDatabaseProcessor(cloudFormationFiles, processingEnv, processingData.getNimbusState()));
        usesResourcesProcessors.add(new UsesServerlessFunctionWebSocketClientProcessor(cloudFormationFiles, processingData.getNimbusState()));
        usesResourcesProcessors.add(new EnvironmentVariablesProcessor(processingData.getNimbusState(), messager));
        usesResourcesProcessors.add(new ForceDependencyProcessor(processingData.getNimbusState()));

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

