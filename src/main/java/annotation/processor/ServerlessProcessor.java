package annotation.processor;

import annotation.models.CloudFormationTemplate;
import annotation.models.resource.*;
import com.google.auto.service.AutoService;
import org.json.JSONObject;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

@SupportedAnnotationTypes("annotation.annotations.ServerlessFunction")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@AutoService(Processor.class)
public class ServerlessProcessor extends AbstractProcessor {

    //TODO: Add memoization so that on repeat processes do not have to run code again

    @Override
    public synchronized void init(final ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        ResourceCollection resources = new ResourceCollection();

        for (TypeElement annotation : annotations) {
            Set<? extends Element> annotatedElements = roundEnv.getElementsAnnotatedWith(annotation);

            Policy lambdaPolicy = new Policy("lambda");

            //Should be a handler
            for (Element element : annotatedElements) {
                if (element.getKind() == ElementKind.METHOD) {
                    String methodName = element.getSimpleName().toString();
                    Element enclosing = element.getEnclosingElement();
                    String name = enclosing.getSimpleName().toString();

                    PackageElement packageElem = (PackageElement) enclosing.getEnclosingElement();
                    String qualifiedName = packageElem.getQualifiedName().toString();

                    String handler;
                    if (qualifiedName.isEmpty()) {
                        handler = enclosing.getSimpleName().toString() + "::" + methodName;
                    } else {
                        handler = qualifiedName + "." + name + "::" + methodName;
                    }


                    Resource function = new FunctionResource(handler, name);
                    Resource logGroup = new LogGroupResource(methodName);
                    Resource bucket   = new NimbusBucketResource();

                    lambdaPolicy.addAllowStatement("logs:CreateLogStream", logGroup, ":*");
                    lambdaPolicy.addAllowStatement("logs:PutLogEvents", logGroup, ":*:*");

                    resources.addResource(function);
                    resources.addResource(logGroup);
                    resources.addResource(bucket);

                }
            }
            System.out.println("AHA IT IS NEW");
            IamRoleResource iamRole = new IamRoleResource(lambdaPolicy);
            resources.addResource(iamRole);
        }

        CloudFormationTemplate template = new CloudFormationTemplate(resources);

        if (template.valid()) {
            try {
                Path path = Paths.get(".nimbus/cloudformation-stack-update.json");
                path.toFile().getParentFile().mkdirs();
                byte[] strToBytes = template.toString().getBytes();
                System.out.println(path.toAbsolutePath().toString());
                Files.write(path, strToBytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return true;
    }
}
