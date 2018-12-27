package annotation.processor;

import annotation.models.CloudFormationTemplate;
import annotation.models.resource.FunctionResource;
import annotation.models.resource.Resource;
import com.google.auto.service.AutoService;
import org.json.JSONObject;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

@SupportedAnnotationTypes("annotation.annotations.ServerlessFunction")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@AutoService(Processor.class)
public class ServerlessProcessor extends AbstractProcessor {


    @Override
    public synchronized void init(final ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        JSONObject resources = new JSONObject();

        for (TypeElement annotation : annotations) {
            Set<? extends Element> annotatedElements = roundEnv.getElementsAnnotatedWith(annotation);
            //Should be a handler
            for (Element element : annotatedElements) {
                if (element.getKind() == ElementKind.METHOD) {
                    String name = element.getSimpleName().toString();
                    Element enclosing = element.getEnclosingElement();

                    PackageElement packageElem = (PackageElement) enclosing.getEnclosingElement();
                    String qualifiedName = packageElem.getQualifiedName().toString();

                    String handler;
                    if (qualifiedName.isEmpty()) {
                        handler = enclosing.getSimpleName().toString() + "::" + name;
                    } else {
                        handler = qualifiedName + "." + enclosing.getSimpleName().toString() + "::" + name;
                    }
                    Resource function = new FunctionResource(handler, enclosing.getSimpleName().toString());
                    resources.put(enclosing.getSimpleName().toString() + "Function", function.toCloudFormation());
                }
            }
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
