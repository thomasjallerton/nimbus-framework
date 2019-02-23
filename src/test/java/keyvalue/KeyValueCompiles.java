package keyvalue;

import annotation.processor.NimbusAnnotationProcessor;
import annotation.services.FileService;
import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import org.junit.jupiter.api.Test;

import static com.google.testing.compile.Compiler.javac;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class KeyValueCompiles {

    private FileService fileService = new FileService();

    @Test
    public void correctCompiles() {
        String fileText = fileService.getResourceFileText("document/handlers/KeyValueStoreHandlers.java");

        Compilation compilation = javac().withProcessors(new NimbusAnnotationProcessor())
                .compile(JavaFileObjects.forSourceString("document.handlers.KeyValueStoreHandlers", fileText));
        assertEquals(Compilation.Status.SUCCESS, compilation.status());
    }
}
