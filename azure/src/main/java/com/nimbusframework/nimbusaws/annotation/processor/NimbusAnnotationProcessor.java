package com.nimbusframework.nimbusaws.annotation.processor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auto.service.AutoService;
import com.nimbusframework.nimbusaws.annotation.services.AzureResourceManagerTemplateWriter;
import com.nimbusframework.nimbusaws.annotation.services.ReadUserConfigService;
import com.nimbusframework.nimbuscore.persisted.CloudProvider;
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
@SupportedSourceVersion(SourceVersion.RELEASE_11)
@AutoService(Processor.class)
public class NimbusAnnotationProcessor extends AbstractProcessor {

    private NimbusState nimbusState = null;

    private AzureResourceManagerTemplateWriter azureResourceManagerTemplateWriter;

    private UserConfig userConfig;

    //private Map<String, AzureResourceManagerTemplateWriter> cloudFormationFiles = new HashMap<>();

    private Messager messager;

    @Override
    public synchronized void init(final ProcessingEnvironment processingEnv) {
        super.init(processingEnv);

        messager = processingEnv.getMessager();

        azureResourceManagerTemplateWriter = new AzureResourceManagerTemplateWriter(processingEnv.getFiler());
        userConfig = new ReadUserConfigService().readUserConfig();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        if (nimbusState == null) {
            Calendar cal = Calendar.getInstance();
            SimpleDateFormat simpleDateFormat =
                    new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSzzz", Locale.US);

            String compilationTime = simpleDateFormat.format(cal.getTime());
            nimbusState = new NimbusState(userConfig.getProjectName(), CloudProvider.AZURE, compilationTime, new HashMap<>(), new HashMap<>(), new HashMap<>(), new HashSet<>(), userConfig.getAssemble());
        }

        if (roundEnv.processingOver()) {

//            for (Map.Entry<String, CloudFormationFiles> entry : cloudFormationFiles.entrySet()) {
//                String stage = entry.getKey();
//                CloudFormationFiles cloudFormationFiles= entry.getValue();
//
//                cloudformationWriter.saveTemplate("cloudformation-stack-update-" + stage, cloudFormationFiles.getUpdateTemplate());
//                cloudformationWriter.saveTemplate("cloudformation-stack-create-" + stage, cloudFormationFiles.getCreateTemplate());
//            }

            ObjectMapper mapper = new ObjectMapper();
            try {
                azureResourceManagerTemplateWriter.saveJsonFile("nimbus-state", mapper.writerWithDefaultPrettyPrinter().writeValueAsString(nimbusState));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
        return true;
    }
}

