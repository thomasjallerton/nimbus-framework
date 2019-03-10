package keyvalue;

import annotation.processor.NimbusAnnotationProcessor;
import annotation.services.FileReader;
import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import org.junit.jupiter.api.Test;

import static com.google.testing.compile.Compiler.javac;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class KeyValueCompiles {

    private FileReader fileService = new FileReader();

    @Test
    public void correctCompiles() {
        String fileText = fileService.getResourceFileText("document/handlers/KeyValueStoreHandlers.java");

        Compilation compilation = javac().withProcessors(new NimbusAnnotationProcessor())
                .compile(JavaFileObjects.forSourceString("document.handlers.KeyValueStoreHandlers", fileText));
        assertEquals(Compilation.Status.SUCCESS, compilation.status());
    }
}
