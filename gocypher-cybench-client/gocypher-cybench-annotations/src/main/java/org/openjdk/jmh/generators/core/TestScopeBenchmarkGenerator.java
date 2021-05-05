package org.openjdk.jmh.generators.core;

import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.runner.BenchmarkList;
import org.openjdk.jmh.runner.BenchmarkListEntry;
import org.openjdk.jmh.runner.options.TimeValue;
import org.openjdk.jmh.util.HashMultimap;
import org.openjdk.jmh.util.Multimap;
import org.openjdk.jmh.util.Optional;

import javax.annotation.processing.ProcessingEnvironment;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class TestScopeBenchmarkGenerator extends BenchmarkGenerator {
    protected static final String JMH_GENERATED_SUBPACKAGE = "jmh_generated";
    private static final String JMH_STUB_SUFFIX = "_jmhStub";
    private static final String JMH_TESTCLASS_SUFFIX = "_jmhTest";

    private static final int NUMBER_OF_FORKS = 1;
    private static final int NUMBER_OF_WARMUPS = 0;
    private static final int NUMBER_OF_MEASUREMENTS = 1;
    private static final Mode BENCHMARK_MODE = Mode.AverageTime;
    private static final TimeValue MEASUREMENT_TIME = TimeValue.seconds(3);
    private static final TimeValue WARMUPS_TIME = TimeValue.seconds(0);

    private final Set<BenchmarkInfo> benchmarkInfos;
    private final CompilerControlPlugin compilerControl;
    private final Set<String> processedBenchmarks;
    private final BenchmarkGeneratorSession session;
    private ProcessingEnvironment processingEnv;

    public TestScopeBenchmarkGenerator() {
        benchmarkInfos = new HashSet<>();
        processedBenchmarks = new HashSet<>();
        compilerControl = new CompilerControlPlugin();
        session = new BenchmarkGeneratorSession();
    }

    static Object _getAndRunPrivateMethod(Object obj, String name, Object... parameters) throws Exception {
        Class<?>[] parameterClasses = Arrays.stream(parameters).map(p -> p.getClass()).toArray(size -> new Class[size]);
        Method method = Arrays.stream(obj.getClass().getSuperclass().getDeclaredMethods())
                .filter(m -> m.getName().equals(name)).findFirst().get();
        method.setAccessible(true);

        return method.invoke(obj, parameters);
    }

    private static Annotation getAnnotation(MethodInfo mi, Class<? extends Annotation>... aClasses) {
        for (Class<? extends Annotation> aClass : aClasses) {
            Annotation annotation = mi.getAnnotation(aClass);

            if (annotation != null) {
                return annotation;
            }
        }

        return null;
    }

    @Override
    public void generate(GeneratorSource source, GeneratorDestination destination) {
        try {
            Multimap<ClassInfo, MethodInfo> clazzes = buildAnnotatedSet(source);

            for (ClassInfo clazz : clazzes.keys()) {
                if (!processedBenchmarks.add(clazz.getQualifiedName())) {
                    continue;
                }
                try {
                    validateBenchmark(clazz, clazzes.get(clazz));
                    Collection<BenchmarkInfo> infos = makeBenchmarkInfo(clazz, clazzes.get(clazz));
                    for (BenchmarkInfo info : infos) {
                        generateClass(destination, clazz, info);
                    }
                    benchmarkInfos.addAll(infos);
                } catch (GenerationException ge) {
                    destination.printWarning(ge.getMessage(), ge.getElement());
                }
            }

            for (Mode mode : Mode.values()) {
                compilerControl.alwaysDontInline("*", "*_" + mode.shortLabel() + JMH_STUB_SUFFIX);
            }

            compilerControl.process(source, destination);
        } catch (Throwable t) {
            destination.printError("Annotation generator had thrown the exception.", t);
        }
    }

    @Override
    public void complete(GeneratorSource source, GeneratorDestination destination) {
        compilerControl.finish(source, destination);

        Set<BenchmarkListEntry> entries = new HashSet<>();

        Multimap<String, BenchmarkListEntry> entriesByQName = new HashMultimap<>();
        try (InputStream stream = destination.getResource(BenchmarkList.BENCHMARK_LIST.substring(1))) {
            for (BenchmarkListEntry ble : BenchmarkList.readBenchmarkList(stream)) {
                entries.add(ble);
                entriesByQName.put(ble.getUserClassQName(), ble);
            }
        } catch (IOException e) {
            // okay, move on
        } catch (UnsupportedOperationException e) {
            destination.printError("Unable to read the existing benchmark list.", e);
        }

        // Generate new benchmark entries
        for (BenchmarkInfo info : benchmarkInfos) {
            try {
                MethodGroup group = info.methodGroup;
                for (Mode m : group.getModes()) {
                    BenchmarkListEntry br = new BenchmarkListEntry(
                            info.userClassQName,
                            info.generatedClassQName,
                            group.getName(),
                            m,
                            group.getTotalThreadCount(),
                            group.getGroupThreads(),
                            group.getGroupLabels(),
                            Optional.of(NUMBER_OF_WARMUPS),
                            group.getWarmupTime(),
                            group.getWarmupBatchSize(),
                            Optional.of(NUMBER_OF_MEASUREMENTS),
                            group.getMeasurementTime(),
                            group.getMeasurementBatchSize(),
                            Optional.of(NUMBER_OF_FORKS),
                            group.getWarmupForks(),
                            group.getJvm(),
                            group.getJvmArgs(),
                            group.getJvmArgsPrepend(),
                            group.getJvmArgsAppend(),
                            group.getParams(),
                            group.getOutputTimeUnit(),
                            group.getOperationsPerInvocation(),
                            group.getTimeout()
                    );

                    if (entriesByQName.keys().contains(info.userClassQName)) {
                        destination.printNote("Benchmark entries for " + info.userClassQName + " already exist, overwriting");
                        entries.removeAll(entriesByQName.get(info.userClassQName));
                        entriesByQName.remove(info.userClassQName);
                    }

                    entries.add(br);
                }
            } catch (GenerationException ge) {
                destination.printError(ge.getMessage(), ge.getElement());
            }
        }

        try (OutputStream stream = destination.newResource(BenchmarkList.BENCHMARK_LIST.substring(1))) {
            BenchmarkList.writeBenchmarkList(stream, entries);
        } catch (IOException ex) {
            destination.printError("Error writing benchmark list", ex);
        }
    }

    private Multimap<ClassInfo, MethodInfo> buildAnnotatedSet(GeneratorSource source) {
        Multimap<ClassInfo, MethodInfo> result = new HashMultimap<>();
        for (ClassInfo currentClass : source.getClasses()) {
            if (currentClass.isAbstract()) {
                continue;
            }
            for (MethodInfo mi : currentClass.getMethods()) {
                Annotation ann = getAnnotation(mi, org.junit.Test.class, org.junit.jupiter.api.Test.class);
                if (ann != null) {
                    result.put(currentClass, mi);
                }
            }
        }

        return result;
    }

    private Object getAndRunPrivateMethod(String name, Object... params) throws Exception {
        try {
            return _getAndRunPrivateMethod(this, name, params);
        } catch (Exception e) {
            throw (Exception) e.getCause();
        }
    }

    private void validateBenchmark(ClassInfo clazz, Collection<MethodInfo> methods) throws Exception {
        getAndRunPrivateMethod("validateBenchmark", clazz, methods);
    }

    @SuppressWarnings("unchecked")
    private Collection<BenchmarkInfo> makeBenchmarkInfo(ClassInfo clazz, Collection<MethodInfo> methods) throws Exception {
        return (Collection<BenchmarkInfo>) getAndRunPrivateMethod("makeBenchmarkInfo", clazz, methods);
    }

    private void generateClass(GeneratorDestination destination, ClassInfo classInfo, BenchmarkInfo info) throws IOException {
        try {
            getAndRunPrivateMethod("generateClass", destination, classInfo, info);
        } catch (Exception io) {
            if (io instanceof IOException) {
                throw (IOException) io;
            }
        }
    }

    public void setProcessingEnv(ProcessingEnvironment processingEnv) {
        this.processingEnv = processingEnv;
    }
}
