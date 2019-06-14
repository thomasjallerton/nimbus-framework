package com.nimbusframework.nimbuscore.annotation.processor;

import com.nimbusframework.nimbuscore.annotation.services.CloudformationWriter;
import com.nimbusframework.nimbuscore.annotation.services.FunctionEnvironmentService;
import com.nimbusframework.nimbuscore.annotation.services.ReadUserConfigService;
import com.nimbusframework.nimbuscore.annotation.services.functions.*;
import com.nimbusframework.nimbuscore.annotation.services.resources.*;
import com.nimbusframework.nimbuscore.annotation.services.useresources.*;
import com.nimbusframework.nimbuscore.cloudformation.CloudFormationFiles;
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
        "com.nimbusframework.nimbuscore.annotation.annotations.function.HttpServerlessFunction",
        "com.nimbusframework.nimbuscore.annotation.annotations.function.repeatable.HttpServerlessFunctions",
        "com.nimbusframework.nimbuscore.annotation.annotations.function.QueueServerlessFunction",
        "com.nimbusframework.nimbuscore.annotation.annotations.function.repeatable.QueueServerlessFunctions",
        "com.nimbusframework.nimbuscore.annotation.annotations.function.DocumentStoreServerlessFunction",
        "com.nimbusframework.nimbuscore.annotation.annotations.function.repeatable.DocumentStoreServerlessFunctions",
        "com.nimbusframework.nimbuscore.annotation.annotations.function.NotificationServerlessFunction",
        "com.nimbusframework.nimbuscore.annotation.annotations.function.repeatable.NotificationServerlessFunctions",
        "com.nimbusframework.nimbuscore.annotation.annotations.function.BasicServerlessFunction",
        "com.nimbusframework.nimbuscore.annotation.annotations.function.repeatable.BasicServerlessFunctions",
        "com.nimbusframework.nimbuscore.annotation.annotations.function.KeyValueStoreServerlessFunction",
        "com.nimbusframework.nimbuscore.annotation.annotations.function.repeatable.KeyValueStoreServerlessFunctions",
        "com.nimbusframework.nimbuscore.annotation.annotations.function.FileStorageServerlessFunction",
        "com.nimbusframework.nimbuscore.annotation.annotations.function.repeatable.FileStorageServerlessFunctions",
        "com.nimbusframework.nimbuscore.annotation.annotations.function.WebSocketServerlessFunction",
        "com.nimbusframework.nimbuscore.annotation.annotations.function.repeatable.WebSocketServerlessFunctions",
        "com.nimbusframework.nimbuscore.annotation.annotations.dynamo.KeyValueStore",
        "com.nimbusframework.nimbuscore.annotation.annotations.dynamo.KeyValueStores",
        "com.nimbusframework.nimbuscore.annotation.annotations.dynamo.DocumentStore",
        "com.nimbusframework.nimbuscore.annotation.annotations.dynamo.DocumentStores",
        "com.nimbusframework.nimbuscore.annotation.annotations.database.RelationalDatabase",
        "com.nimbusframework.nimbuscore.annotation.annotations.database.RelationalDatabases",
        "com.nimbusframework.nimbuscore.annotation.annotations.deployment.FileUpload",
        "com.nimbusframework.nimbuscore.annotation.annotations.deployment.FileUploads",
        "com.nimbusframework.nimbuscore.annotation.annotations.deployment.AfterDeployment",
        "com.nimbusframework.nimbuscore.annotation.annotations.deployment.AfterDeployments",
        "com.nimbusframework.nimbuscore.annotation.annotations.deployment.ForceDependency",
        "com.nimbusframework.nimbuscore.annotation.annotations.deployment.ForceDependencies",
        "com.nimbusframework.nimbuscore.annotation.annotations.file.FileStorageBucket",
        "com.nimbusframework.nimbuscore.annotation.annotations.file.FileStorageBuckets"

})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@AutoService(Processor.class)
public class NimbusAnnotationProcessor extends AbstractProcessor {

    private NimbusState nimbusState = null;

    private CloudformationWriter cloudformationWriter;

    private UserConfig userConfig;

    private Map<String, CloudFormationFiles> cloudFormationFiles = new HashMap<>();

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

