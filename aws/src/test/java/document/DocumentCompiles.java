package document;

import com.nimbusframework.nimbusaws.annotation.processor.NimbusAnnotationProcessor;
import com.nimbusframework.nimbusaws.annotation.services.FileReader;
import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;


import org.junit.jupiter.api.Test;


import static com.google.testing.compile.Compiler.javac;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class DocumentCompiles {

    private FileReader fileService = new FileReader();

    @Test
    public void correctCompiles() {
        String fileText = fileService.getResourceFileText("handlers/DocumentStoreHandlers.java");

        Compilation compilation = javac().withProcessors(new NimbusAnnotationProcessor())
                .compile(JavaFileObjects.forSourceString("handlers.DocumentStoreHandlers", fileText));
        assertEquals(Compilation.Status.SUCCESS, compilation.status());
    }

    @Test
    public void insertTwoEventArgumentsFailsCompilation() {
        String fileText = fileService.getResourceFileText("handlers/BadDocumentStoreHandlers.java");

        Compilation compilation = javac().withProcessors(new NimbusAnnotationProcessor())
                .compile(JavaFileObjects.forSourceString("handlers.BadDocumentStoreHandlers", fileText));
        assertEquals(Compilation.Status.FAILURE, compilation.status());
    }
}
