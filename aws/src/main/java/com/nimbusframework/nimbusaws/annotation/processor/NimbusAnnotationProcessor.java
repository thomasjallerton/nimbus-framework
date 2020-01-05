package com.nimbusframework.nimbusaws.annotation.processor;

import com.nimbusframework.nimbusaws.annotation.services.CloudFormationWriter;
import com.nimbusframework.nimbusaws.annotation.services.FunctionEnvironmentService;
import com.nimbusframework.nimbusaws.annotation.services.ReadUserConfigService;
import com.nimbusframework.nimbusaws.annotation.services.ResourceFinder;
import com.nimbusframework.nimbusaws.annotation.services.functions.AfterDeploymentResourceCreator;
import com.nimbusframework.nimbusaws.annotation.services.functions.BasicFunctionResourceCreator;
import com.nimbusframework.nimbusaws.annotation.services.functions.DocumentStoreFunctionResourceCreator;
import com.nimbusframework.nimbusaws.annotation.services.functions.FileStorageResourceCreator;
import com.nimbusframework.nimbusaws.annotation.services.functions.FileUploadResourceCreator;
import com.nimbusframework.nimbusaws.annotation.services.functions.FunctionResourceCreator;
import com.nimbusframework.nimbusaws.annotation.services.functions.HttpFunctionResourceCreator;
import com.nimbusframework.nimbusaws.annotation.services.functions.KeyValueStoreFunctionResourceCreator;
import com.nimbusframework.nimbusaws.annotation.services.functions.NotificationFunctionResourceCreator;
import com.nimbusframework.nimbusaws.annotation.services.functions.QueueFunctionResourceCreator;
import com.nimbusframework.nimbusaws.annotation.services.functions.WebSocketFunctionResourceCreator;
import com.nimbusframework.nimbusaws.annotation.services.resources.*;
import com.nimbusframework.nimbusaws.annotation.services.useresources.EnvironmentVariablesProcessor;
import com.nimbusframework.nimbusaws.annotation.services.useresources.ForceDependencyProcessor;
import com.nimbusframework.nimbusaws.annotation.services.useresources.UsesBasicServerlessFunctionClientProcessor;
import com.nimbusframework.nimbusaws.annotation.services.useresources.UsesDocumentStoreProcessor;
import com.nimbusframework.nimbusaws.annotation.services.useresources.UsesFileStorageClientProcessor;
import com.nimbusframework.nimbusaws.annotation.services.useresources.UsesKeyValueStoreProcessor;
import com.nimbusframework.nimbusaws.annotation.services.useresources.UsesNotificationTopicProcessor;
import com.nimbusframework.nimbusaws.annotation.services.useresources.UsesQueueProcessor;
import com.nimbusframework.nimbusaws.annotation.services.useresources.UsesRelationalDatabaseProcessor;
import com.nimbusframework.nimbusaws.annotation.services.useresources.UsesResourcesProcessor;
import com.nimbusframework.nimbusaws.annotation.services.useresources.UsesServerlessFunctionWebSocketClientProcessor;
import com.nimbusframework.nimbusaws.cloudformation.CloudFormationFiles;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auto.service.AutoService;
import com.nimbusframework.nimbuscore.persisted.NimbusState;
import com.nimbusframework.nimbuscore.persisted.UserConfig;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
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
        "com.nimbusframework.nimbuscore.annotations.file.FileStorageBucketDefinitions"
})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@AutoService(Processor.class)
public class NimbusAnnotationProcessor extends AbstractProcessor {

    private NimbusState nimbusState = null;

    private CloudFormationWriter cloudformationWriter;

    private UserConfig userConfig;

    private Map<String, CloudFormationFiles> cloudFormationFiles = new HashMap<>();

    private Messager messager;

    @Override
    public synchronized void init(final ProcessingEnvironment processingEnv) {
        super.init(processingEnv);

        messager = processingEnv.getMessager();

        cloudformationWriter = new CloudFormationWriter(processingEnv.getFiler());
        userConfig = new ReadUserConfigService().readUserConfig();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        if (nimbusState == null) {
            Calendar cal = Calendar.getInstance();
            SimpleDateFormat simpleDateFormat =
                    new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSzzz", Locale.US);

            String compilationTime = simpleDateFormat.format(cal.getTime());
            nimbusState = new NimbusState(userConfig.getProjectName(), compilationTime, new HashMap<>(), new HashMap<>(), new HashMap<>(), new HashSet<>(), userConfig.getAssemble());
        }

        FunctionEnvironmentService functionEnvironmentService = new FunctionEnvironmentService(
                cloudFormationFiles,
                nimbusState
        );


        List<CloudResourceResourceCreator> resourceCreators = new LinkedList<>();
        resourceCreators.add(new DocumentStoreResourceCreator(roundEnv, cloudFormationFiles, nimbusState));
        resourceCreators.add(new KeyValueStoreResourceCreator(roundEnv, cloudFormationFiles, nimbusState, processingEnv));
        resourceCreators.add(new NotificationTopicResourceCreator(roundEnv, cloudFormationFiles, nimbusState));
        resourceCreators.add(new QueueResourceCreator(roundEnv, cloudFormationFiles, nimbusState));
        resourceCreators.add(new RelationalDatabaseResourceCreator(roundEnv, cloudFormationFiles, nimbusState));
        resourceCreators.add(new FileStorageBucketResourceCreator(roundEnv, cloudFormationFiles, nimbusState));

        for (CloudResourceResourceCreator creator : resourceCreators) {
            creator.create();
        }

        ResourceFinder resourceFinder = new ResourceFinder(cloudFormationFiles, processingEnv, nimbusState);

        List<FunctionResourceCreator> functionResourceCreators = new LinkedList<>();
        functionResourceCreators.add(new DocumentStoreFunctionResourceCreator(cloudFormationFiles, nimbusState, processingEnv, messager, resourceFinder));
        functionResourceCreators.add(new KeyValueStoreFunctionResourceCreator(cloudFormationFiles, nimbusState, processingEnv, messager, resourceFinder));
        functionResourceCreators.add(new HttpFunctionResourceCreator(cloudFormationFiles, nimbusState, processingEnv));
        functionResourceCreators.add(new NotificationFunctionResourceCreator(cloudFormationFiles, nimbusState, processingEnv, messager, resourceFinder));
        functionResourceCreators.add(new QueueFunctionResourceCreator(cloudFormationFiles, nimbusState, processingEnv, messager, resourceFinder));
        functionResourceCreators.add(new BasicFunctionResourceCreator(cloudFormationFiles, nimbusState, processingEnv));
        functionResourceCreators.add(new FileStorageResourceCreator(cloudFormationFiles, nimbusState, processingEnv, messager, resourceFinder));
        functionResourceCreators.add(new WebSocketFunctionResourceCreator(cloudFormationFiles, nimbusState, processingEnv));

        functionResourceCreators.add(new FileUploadResourceCreator(cloudFormationFiles, nimbusState, resourceFinder, messager));
        functionResourceCreators.add(new AfterDeploymentResourceCreator(cloudFormationFiles, nimbusState, processingEnv));

        List<FunctionInformation> allInformation = new LinkedList<>();
        for (FunctionResourceCreator creator : functionResourceCreators) {
            allInformation.addAll(creator.handle(roundEnv, functionEnvironmentService));
        }

        List<UsesResourcesProcessor> usesResourcesProcessors = new LinkedList<>();
        usesResourcesProcessors.add(new UsesBasicServerlessFunctionClientProcessor(cloudFormationFiles, processingEnv, nimbusState, messager));
        usesResourcesProcessors.add(new UsesDocumentStoreProcessor(cloudFormationFiles, processingEnv, nimbusState, messager));
        usesResourcesProcessors.add(new UsesFileStorageClientProcessor(cloudFormationFiles, messager, resourceFinder));
        usesResourcesProcessors.add(new UsesKeyValueStoreProcessor(cloudFormationFiles, processingEnv, nimbusState, messager));
        usesResourcesProcessors.add(new UsesNotificationTopicProcessor(cloudFormationFiles, messager, resourceFinder));
        usesResourcesProcessors.add(new UsesQueueProcessor(cloudFormationFiles, messager, resourceFinder));
        usesResourcesProcessors.add(new UsesRelationalDatabaseProcessor(cloudFormationFiles, processingEnv, nimbusState));
        usesResourcesProcessors.add(new UsesServerlessFunctionWebSocketClientProcessor(cloudFormationFiles));
        usesResourcesProcessors.add(new EnvironmentVariablesProcessor(messager));
        usesResourcesProcessors.add(new ForceDependencyProcessor());

        for (FunctionInformation functionInformation : allInformation) {
            for (UsesResourcesProcessor handler : usesResourcesProcessors) {
                handler.handleUseResources(functionInformation.getElement(), functionInformation.getResource());
            }
        }

        if (roundEnv.processingOver()) {

            for (Map.Entry<String, CloudFormationFiles> entry : cloudFormationFiles.entrySet()) {
                String stage = entry.getKey();
                CloudFormationFiles cloudFormationFiles= entry.getValue();

                cloudformationWriter.saveTemplate("cloudformation-stack-update-" + stage, cloudFormationFiles.getUpdateTemplate());
                cloudformationWriter.saveTemplate("cloudformation-stack-create-" + stage, cloudFormationFiles.getCreateTemplate());
            }

            ObjectMapper mapper = new ObjectMapper();
            try {
                cloudformationWriter.saveJsonFile("nimbus-state", mapper.writerWithDefaultPrettyPrinter().writeValueAsString(nimbusState));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
        return true;
    }
}

