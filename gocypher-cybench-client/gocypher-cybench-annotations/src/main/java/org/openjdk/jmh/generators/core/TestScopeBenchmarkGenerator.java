package org.openjdk.jmh.generators.core;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.runner.BenchmarkList;
import org.openjdk.jmh.runner.BenchmarkListEntry;
import org.openjdk.jmh.util.HashMultimap;
import org.openjdk.jmh.util.Multimap;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class TestScopeBenchmarkGenerator extends BenchmarkGenerator {
    protected static final String JMH_GENERATED_SUBPACKAGE = "jmh_generated";
    private static final String JMH_STUB_SUFFIX = "_jmhStub";
    private static final String JMH_TESTCLASS_SUFFIX = "_jmhTest";
    private final Set<BenchmarkInfo> benchmarkInfos;
    private final CompilerControlPlugin compilerControl;
    private final Set<String> processedBenchmarks;
    private final BenchmarkGeneratorSession session;

    public TestScopeBenchmarkGenerator() {
        benchmarkInfos = new HashSet<>();
        processedBenchmarks = new HashSet<>();
        compilerControl = new CompilerControlPlugin();
        session = new BenchmarkGeneratorSession();
    }

    static Object _getAndRunPrivateMethod(Object obj, String name, Object... parameters) {

        Class[] parameterClasses = Arrays.stream(parameters).map(p -> p.getClass()).toArray(size -> new Class[size]);


        try {

            Method method = Arrays.stream(obj.getClass().getSuperclass().getDeclaredMethods()).filter(m -> m.getName().equals(name)).findFirst().get();
            method.setAccessible(true);
            return method.invoke(obj, parameters);

//        } catch (NoSuchMethodException e) {
//            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void generate(GeneratorSource source, GeneratorDestination destination) {
        try {
            Multimap<ClassInfo, MethodInfo> clazzes = buildAnnotatedSet(source);

            for (ClassInfo clazz : clazzes.keys()) {
                if (!processedBenchmarks.add(clazz.getQualifiedName())) continue;
                try {
                    validateBenchmark(clazz, clazzes.get(clazz));
                    Collection<BenchmarkInfo> infos = makeBenchmarkInfo(clazz, clazzes.get(clazz));
                    for (BenchmarkInfo info : infos) {
                        generateClass(destination, clazz, info);
                    }
                    benchmarkInfos.addAll(infos);
                } catch (GenerationException ge) {
                    destination.printError(ge.getMessage(), ge.getElement());
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
                            group.getWarmupIterations(),
                            group.getWarmupTime(),
                            group.getWarmupBatchSize(),
                            group.getMeasurementIterations(),
                            group.getMeasurementTime(),
                            group.getMeasurementBatchSize(),
                            group.getForks(),
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
            if (currentClass.isAbstract()) continue;
            for (MethodInfo mi : currentClass.getMethods()) {
                Object ann = mi.getAnnotation(org.junit.Test.class);
                if (ann != null) {
                    result.put(currentClass, mi);
                }
            }
        }
        return result;
    }

    private Object getAndRunPrivateMethod(String name, Object ... params) {
        return _getAndRunPrivateMethod(this, name, params);
    }

    private void validateBenchmark(ClassInfo clazz, Collection<MethodInfo> methods) {
        getAndRunPrivateMethod("validateBenchmark", clazz, methods);
    }

    private Collection<BenchmarkInfo> makeBenchmarkInfo(ClassInfo clazz, Collection<MethodInfo> methods) {
        return (Collection<BenchmarkInfo>) getAndRunPrivateMethod("makeBenchmarkInfo", clazz, methods);

    }

    private void generateClass(GeneratorDestination destination, ClassInfo classInfo, BenchmarkInfo info) throws IOException {
        getAndRunPrivateMethod("generateClass", destination, classInfo, info);
    }

}

