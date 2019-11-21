package com.nimbusframework.nimbusaws.annotation.processor;

import com.nimbusframework.nimbusaws.annotation.services.CloudFormationWriter;
import com.nimbusframework.nimbusaws.annotation.services.FunctionEnvironmentService;
import com.nimbusframework.nimbusaws.annotation.services.ReadUserConfigService;
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
import com.nimbusframework.nimbusaws.annotation.services.resources.CloudResourceResourceCreator;
import com.nimbusframework.nimbusaws.annotation.services.resources.DocumentStoreResourceCreator;
import com.nimbusframework.nimbusaws.annotation.services.resources.FileStorageBucketResourceCreator;
import com.nimbusframework.nimbusaws.annotation.services.resources.KeyValueStoreResourceCreator;
import com.nimbusframework.nimbusaws.annotation.services.resources.RelationalDatabaseResourceCreator;
import com.nimbusframework.nimbusaws.annotation.services.useresources.EnvironmentVariablesHandler;
import com.nimbusframework.nimbusaws.annotation.services.useresources.ForceDependencyHandler;
import com.nimbusframework.nimbusaws.annotation.services.useresources.UsesBasicServerlessFunctionClientHandler;
import com.nimbusframework.nimbusaws.annotation.services.useresources.UsesDocumentStoreHandler;
import com.nimbusframework.nimbusaws.annotation.services.useresources.UsesFileStorageClientHandler;
import com.nimbusframework.nimbusaws.annotation.services.useresources.UsesKeyValueStoreHandler;
import com.nimbusframework.nimbusaws.annotation.services.useresources.UsesNotificationTopicHandler;
import com.nimbusframework.nimbusaws.annotation.services.useresources.UsesQueueHandler;
import com.nimbusframework.nimbusaws.annotation.services.useresources.UsesRelationalDatabaseHandler;
import com.nimbusframework.nimbusaws.annotation.services.useresources.UsesResourcesHandler;
import com.nimbusframework.nimbusaws.annotation.services.useresources.UsesServerlessFunctionWebSocketClientHandler;
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
        "com.nimbusframework.nimbuscore.annotations.dynamo.KeyValueStore",
        "com.nimbusframework.nimbuscore.annotations.dynamo.KeyValueStores",
        "com.nimbusframework.nimbuscore.annotations.dynamo.DocumentStore",
        "com.nimbusframework.nimbuscore.annotations.dynamo.DocumentStores",
        "com.nimbusframework.nimbuscore.annotations.database.RelationalDatabase",
        "com.nimbusframework.nimbuscore.annotations.database.RelationalDatabases",
        "com.nimbusframework.nimbuscore.annotations.deployment.FileUpload",
        "com.nimbusframework.nimbuscore.annotations.deployment.FileUploads",
        "com.nimbusframework.nimbuscore.annotations.deployment.AfterDeployment",
        "com.nimbusframework.nimbuscore.annotations.deployment.AfterDeployments",
        "com.nimbusframework.nimbuscore.annotations.deployment.ForceDependency",
        "com.nimbusframework.nimbuscore.annotations.deployment.ForceDependencies",
        "com.nimbusframework.nimbuscore.annotations.file.FileStorageBucket",
        "com.nimbusframework.nimbuscore.annotations.file.FileStorageBuckets"

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
        resourceCreators.add(new RelationalDatabaseResourceCreator(roundEnv, cloudFormationFiles, nimbusState));
        resourceCreators.add(new FileStorageBucketResourceCreator(roundEnv, cloudFormationFiles, nimbusState));

        for (CloudResourceResourceCreator creator : resourceCreators) {
            creator.create();
        }

        List<FunctionResourceCreator> functionResourceCreators = new LinkedList<>();
        functionResourceCreators.add(new DocumentStoreFunctionResourceCreator(cloudFormationFiles, nimbusState, processingEnv));
        functionResourceCreators.add(new KeyValueStoreFunctionResourceCreator(cloudFormationFiles, nimbusState, processingEnv));
        functionResourceCreators.add(new HttpFunctionResourceCreator(cloudFormationFiles, nimbusState, processingEnv));
        functionResourceCreators.add(new NotificationFunctionResourceCreator(cloudFormationFiles, nimbusState, processingEnv));
        functionResourceCreators.add(new QueueFunctionResourceCreator(cloudFormationFiles, nimbusState, processingEnv));
        functionResourceCreators.add(new BasicFunctionResourceCreator(cloudFormationFiles, nimbusState, processingEnv));
        functionResourceCreators.add(new FileStorageResourceCreator(cloudFormationFiles, nimbusState, processingEnv));
        functionResourceCreators.add(new WebSocketFunctionResourceCreator(cloudFormationFiles, nimbusState, processingEnv));

        functionResourceCreators.add(new FileUploadResourceCreator(cloudFormationFiles, nimbusState, processingEnv));
        functionResourceCreators.add(new AfterDeploymentResourceCreator(cloudFormationFiles, nimbusState, processingEnv));

        List<FunctionInformation> allInformation = new LinkedList<>();
        for (FunctionResourceCreator creator : functionResourceCreators) {
            allInformation.addAll(creator.handle(roundEnv, functionEnvironmentService));
        }


        List<UsesResourcesHandler> usesResourcesHandlers = new LinkedList<>();
        usesResourcesHandlers.add(new UsesBasicServerlessFunctionClientHandler(cloudFormationFiles, processingEnv, nimbusState));
        usesResourcesHandlers.add(new UsesDocumentStoreHandler(cloudFormationFiles, processingEnv, nimbusState));
        usesResourcesHandlers.add(new UsesFileStorageClientHandler(cloudFormationFiles, nimbusState));
        usesResourcesHandlers.add(new UsesKeyValueStoreHandler(cloudFormationFiles, processingEnv, nimbusState));
        usesResourcesHandlers.add(new UsesNotificationTopicHandler(cloudFormationFiles, processingEnv, nimbusState));
        usesResourcesHandlers.add(new UsesQueueHandler(cloudFormationFiles, processingEnv, nimbusState));
        usesResourcesHandlers.add(new UsesRelationalDatabaseHandler(cloudFormationFiles, processingEnv, nimbusState));
        usesResourcesHandlers.add(new UsesServerlessFunctionWebSocketClientHandler(cloudFormationFiles));
        usesResourcesHandlers.add(new EnvironmentVariablesHandler(messager));
        usesResourcesHandlers.add(new ForceDependencyHandler());

        for (FunctionInformation functionInformation : allInformation) {
            for (UsesResourcesHandler handler : usesResourcesHandlers) {
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

