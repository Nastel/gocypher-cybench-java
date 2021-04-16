package org.openjdk.jmh.generators.core;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.util.HashMultimap;
import org.openjdk.jmh.util.Multimap;

import java.io.IOException;
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

