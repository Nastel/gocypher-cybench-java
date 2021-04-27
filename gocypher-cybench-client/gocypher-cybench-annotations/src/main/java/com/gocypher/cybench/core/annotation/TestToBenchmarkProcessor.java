package com.gocypher.cybench.core.annotation;

import com.google.auto.service.AutoService;
import org.openjdk.jmh.generators.annotations.APGeneratorDestinaton;
import org.openjdk.jmh.generators.annotations.APGeneratorSource;
import org.openjdk.jmh.generators.core.BenchmarkGenerator;
import org.openjdk.jmh.generators.core.GeneratorDestination;
import org.openjdk.jmh.generators.core.GeneratorSource;
import org.openjdk.jmh.generators.core.TestScopeBenchmarkGenerator;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;

@SupportedAnnotationTypes({"org.junit.Test", "org.junit.jupiter.api.Test"})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@AutoService(Processor.class)
public class TestToBenchmarkProcessor extends AbstractProcessor {
    private final BenchmarkGenerator generator = new TestScopeBenchmarkGenerator();

    static File getAlreadyCreatedFile(ProcessingEnvironment processingEnv, String name) {

        Filer filer = processingEnv.getFiler();

        FileObject dummySourceFile = null;
        try {
            dummySourceFile = filer.createResource(StandardLocation.CLASS_OUTPUT, "", "dummy" + System.currentTimeMillis());
        } catch (IOException e) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Cannot create dummy file: \n\t" + e.getMessage());
        }
        String dummySourceFilePath = dummySourceFile.toUri().toString();

        if (dummySourceFilePath.startsWith("file:")) {
            if (!dummySourceFilePath.startsWith("file://")) {
                dummySourceFilePath = "file://" + dummySourceFilePath.substring("file:".length());
            }
        } else {
            dummySourceFilePath = "file://" + dummySourceFilePath;
        }

        URI cleanURI = null;
        try {
            cleanURI = new URI(dummySourceFilePath);
        } catch (URISyntaxException e) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, e.getMessage());
        }

        File dummyFile = new File(cleanURI);
        return dummyFile.getParentFile().toPath().resolve(name).toFile();
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        // We may claim to support the latest version, since we are not using
        // any version-specific extensions.
        return SourceVersion.latest();
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        ((TestScopeBenchmarkGenerator) generator).setProcessingEnv(processingEnv);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (System.getProperty("generateBenchmarkFromTest") == null) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, "A");
            return false;

        }

        GeneratorSource source = new APGeneratorSource(roundEnv, processingEnv);
        GeneratorDestination destination = new APGeneratorDestinaton(roundEnv, processingEnv) {
            @Override
            public OutputStream newResource(String resourcePath) throws IOException {
                try {
                    return super.newResource(resourcePath);
                } catch (FilerException e) {
                    return new FileOutputStream(getAlreadyCreatedFile(processingEnv, resourcePath), true);
                }

            }
        };
        if (!roundEnv.processingOver()) {
            generator.generate(source, destination);
        } else {
            generator.complete(source, destination);
        }

        return false;
    }
}
