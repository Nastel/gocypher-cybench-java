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
import java.util.Set;

@SupportedAnnotationTypes(
        "org.junit.Test")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@AutoService(Processor.class)
public class TestToBenchmarkProcessor extends AbstractProcessor {
    private final BenchmarkGenerator generator = new TestScopeBenchmarkGenerator();

    @Override
    public SourceVersion getSupportedSourceVersion() {
        // We may claim to support the latest version, since we are not using
        // any version-specific extensions.
        return SourceVersion.latest();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (System.getProperty("generateBenchmarkFromTest") == null) {
            return false;
        }
        GeneratorSource source = new APGeneratorSource(roundEnv, processingEnv);
        GeneratorDestination destination = new APGeneratorDestinaton(roundEnv, processingEnv);
        if (!roundEnv.processingOver()) {
            generator.generate(source, destination);
        } else {
            generator.complete(source, destination);
        }
        return false;
    }
}
