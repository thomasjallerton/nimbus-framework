package basic;

import static com.google.testing.compile.Compiler.javac;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import com.nimbusframework.nimbusaws.annotation.processor.NimbusAnnotationProcessor;
import com.nimbusframework.nimbusaws.annotation.services.FileReader;
import org.junit.jupiter.api.Test;

public class BasicCompiles {

  private FileReader fileService = new FileReader();

  @Test
  public void correctCompiles() {
    String fileText = fileService.getResourceFileText("handlers/BasicHandlers.java");

    Compilation compilation = javac().withProcessors(new NimbusAnnotationProcessor())
        .compile(JavaFileObjects.forSourceString("handlers.BasicHandlers", fileText));
    assertEquals(Compilation.Status.SUCCESS, compilation.status());
  }

}
