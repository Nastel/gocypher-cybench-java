package com.gocypher.cybench.core.annotation;

import com.google.auto.service.AutoService;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.file.PathFileObject;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

@SupportedAnnotationTypes(
        "com.gocypher.cybench.core.annotation.BenchmarkTag")
@SupportedSourceVersion(SourceVersion.RELEASE_8)

@AutoService(Processor.class)
public class BenchmarkTagProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (TypeElement annotation : annotations) {
            Set<? extends Element> annotatedElements
                    = roundEnv.getElementsAnnotatedWith(annotation);


            annotatedElements.stream().forEach(element -> {
                checkTagAnnotation(element, processingEnv.getMessager());
                createFile(element);
            });
//                JavaFileObject builderFile = processingEnv.getFiler()

        }
        return false;
    }

    private void createFile(Element element) {
        try {
            if (element instanceof Symbol.MethodSymbol) {
                Filer filer = processingEnv.getFiler();
                Symbol.ClassSymbol classSymbol = ((Symbol.MethodSymbol) element).enclClass();
                JavaFileObject sourcefile = classSymbol.sourcefile;
                if (sourcefile instanceof PathFileObject) {
                    Path path = ((PathFileObject) sourcefile).getPath();

                    String fileContents = new String(Files.readAllBytes(path), "UTF-8");

                    String name = String.valueOf(classSymbol.getSimpleName());
                    String replaced = getReplaced(fileContents, name, name);

                    JavaFileObject classFile = filer.createSourceFile(classSymbol.getQualifiedName());
                    try (PrintWriter wr = new PrintWriter(classFile.openWriter())) {
                        wr.print(replaced);
                    }


                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private String getReplaced(String fileContents, String newName, String oldName) {
        String replaced = fileContents.replaceAll("@Benchmark", "@Benchmark");
        replaced = replaced.replaceAll(Pattern.quote(oldName), newName);
        return replaced;
    }

    private void checkTagAnnotation(Element element, Messager messager) {
        try {
            BenchmarkTag annotation = element.getAnnotation(BenchmarkTag.class);
            if (annotation != null) {
                UUID.fromString(annotation.tag());
            }
        } catch (IllegalArgumentException r) {
            String msg = "@BenchmarkTag is not UUID like. eg. " + UUID.randomUUID();
            messager.printMessage(Diagnostic.Kind.ERROR, msg);
            throw new RuntimeException(msg);
        }
    }
}
