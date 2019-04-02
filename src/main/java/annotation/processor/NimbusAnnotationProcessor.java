package annotation.processor;

import annotation.services.CloudformationWriter;
import annotation.services.FunctionEnvironmentService;
import annotation.services.ReadUserConfigService;
import annotation.services.functions.*;
import annotation.services.resources.*;
import annotation.services.useresources.*;
import cloudformation.CloudFormationDocuments;
import cloudformation.CloudFormationTemplate;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auto.service.AutoService;
import persisted.NimbusState;
import persisted.UserConfig;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
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
            nimbusState = new NimbusState(userConfig.getProjectName(), compilationTime, new HashMap<>(), new HashMap<>(), new HashMap<>());
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


        List<UsesResourcesHandler> usesResourcesHandlers = new LinkedList<>();
        usesResourcesHandlers.add(new UsesBasicServerlessFunctionClientHandler(cfDocuments, nimbusState));
        usesResourcesHandlers.add(new UsesDocumentStoreHandler(cfDocuments, processingEnv, nimbusState));
        usesResourcesHandlers.add(new UsesFileStorageClientHandler(cfDocuments, nimbusState));
        usesResourcesHandlers.add(new UsesKeyValueStoreHandler(cfDocuments, processingEnv, nimbusState));
        usesResourcesHandlers.add(new UsesNotificationTopicHandler(cfDocuments, processingEnv, nimbusState));
        usesResourcesHandlers.add(new UsesQueueHandler(cfDocuments, processingEnv));
        usesResourcesHandlers.add(new UsesRelationalDatabaseHandler(cfDocuments, processingEnv, nimbusState));
        usesResourcesHandlers.add(new UsesServerlessFunctionWebSocketClientHandler(cfDocuments));

        for (FunctionInformation functionInformation : allInformation) {
            for (UsesResourcesHandler handler : usesResourcesHandlers) {
                handler.handleUseResources(functionInformation.getElement(), functionInformation.getResource());
            }
        }

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
                cloudformationWriter.saveJsonFile("nimbus-state", mapper.writerWithDefaultPrettyPrinter().writeValueAsString(nimbusState));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
        return true;
    }
}

