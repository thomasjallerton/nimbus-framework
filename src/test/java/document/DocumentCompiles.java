package document;

import com.nimbusframework.nimbuscore.annotation.processor.NimbusAnnotationProcessor;
import com.nimbusframework.nimbuscore.annotation.services.FileReader;
import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;


import org.junit.jupiter.api.Test;


import static com.google.testing.compile.Compiler.javac;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class DocumentCompiles {

    private FileReader fileService = new FileReader();

    @Test
    public void correctCompiles() {
        String fileText = fileService.getResourceFileText("document/handlers/DocumentStoreHandlers.java");

        Compilation compilation = javac().withProcessors(new NimbusAnnotationProcessor())
                .compile(JavaFileObjects.forSourceString("document.handlers.DocumentStoreHandlers", fileText));
        assertEquals(Compilation.Status.SUCCESS, compilation.status());
    }

    @Test
    public void insertTwoEventArgumentsFailsCompilation() {
        String fileText = fileService.getResourceFileText("document/handlers/BadDocumentStoreHandlers.java");

        Compilation compilation = javac().withProcessors(new NimbusAnnotationProcessor())
                .compile(JavaFileObjects.forSourceString("document.handlers.BadDocumentStoreHandlers", fileText));
        assertEquals(Compilation.Status.FAILURE, compilation.status());
    }
}
