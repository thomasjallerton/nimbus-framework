package annotation.processor;

import com.google.auto.service.AutoService;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
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
                    if (qualifiedName.isEmpty())  {
                        handler = enclosing.getSimpleName().toString() + "::" + name;
                    } else {
                        handler = qualifiedName + "." + enclosing.getSimpleName().toString() + "::" + name;
                    }
                    System.out.println(handler);
                }
            }
        }
        return true;
    }
}
