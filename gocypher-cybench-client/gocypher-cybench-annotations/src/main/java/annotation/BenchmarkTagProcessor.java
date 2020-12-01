/*
 * Copyright (C) 2020, K2N.IO.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301  USA
 */

package annotation;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.google.auto.service.AutoService;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.util.Name;
import org.openjdk.jmh.annotations.Benchmark;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@SupportedAnnotationTypes(
        "org.openjdk.jmh.annotations.Benchmark")
@SupportedSourceVersion(SourceVersion.RELEASE_8)

@AutoService(Processor.class)
public class BenchmarkTagProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        List<FileObject> createdFiles = new ArrayList<>();
        for (TypeElement annotation : annotations) {
            Set<? extends Element> annotatedElements
                    = roundEnv.getElementsAnnotatedWith(annotation);

            annotatedElements.stream().forEach(element -> {
                checkTagAnnotation(element, processingEnv.getMessager());
                if (element.getAnnotation(BenchmarkTag.class) == null) {
                    createdFiles.add(createFile(element));
                }
            });
//                JavaFileObject builderFile = processingEnv.getFiler()

            try {
                File srcDir = new File("./target/generated-test-sources/test-annotations/");
                if (srcDir.exists()) {
                    copyDirectory(srcDir, new File("./src/test/java"));
                }


            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        return false;
    }

    private void copyDirectory(File srcDir, File dst) throws IOException {
        Path srcPath = srcDir.toPath();
        Files.walkFileTree(srcPath, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (file.toString().endsWith(".generated")) {
                    Path resolve = srcPath.relativize(file);
                    Path generatedFile = dst.toPath().resolve(resolve);
                    Path javaFile = Paths.get(generatedFile.getFileName().toString().replaceAll(".generated", ".java"));
                    Path parent = generatedFile.getParent();

                    Files.copy(file, parent.resolve(javaFile), StandardCopyOption.REPLACE_EXISTING);
                }

                return super.visitFile(file, attrs);
            }
        });
    }

    private FileObject createFile(Element element) {
        try {
            if (element instanceof Symbol.MethodSymbol) {
                Filer filer = processingEnv.getFiler();
                Symbol.ClassSymbol classSymbol = ((Symbol.MethodSymbol) element).enclClass();
                JavaFileObject sourcefile = classSymbol.sourcefile;
                Path path = Paths.get(sourcefile.toUri());

                String fileContents = new String(Files.readAllBytes(path), "UTF-8");

                String name = String.valueOf(classSymbol.getSimpleName());
                String replaced = getReplaced(fileContents, name, name);


                Name pck = classSymbol.getQualifiedName().subName(0, classSymbol.getQualifiedName().lastIndexOf((byte) '.'));
                Name nm = classSymbol.getQualifiedName().subName(classSymbol.getQualifiedName().lastIndexOf((byte) '.') + 1, classSymbol.getQualifiedName().length());
                FileObject classFile = filer.createResource(StandardLocation.SOURCE_OUTPUT, pck, nm + ".generated");
                try (PrintWriter wr = new PrintWriter(classFile.openWriter())) {
                    wr.print(replaced);
                }
                return classFile;


            }
        } catch (IOException e) {
            e.printStackTrace();

        }
        return null;
    }

    private String getReplaced(String fileContents, String newName, String oldName) {
        CompilationUnit cu = StaticJavaParser.parse(fileContents);
        List<MethodDeclaration> allMethods = cu.findAll(MethodDeclaration.class);
        if (!cu.getImports().contains(StaticJavaParser.parseImport("import " + BenchmarkTag.class.getCanonicalName() + ";"))) {
            cu.addImport(BenchmarkTag.class);
        }

        List<MethodDeclaration> methodsNeedTag = allMethods.stream().filter(
                methodDeclaration -> !methodDeclaration.getAnnotationByClass(BenchmarkTag.class).isPresent() &&
                        methodDeclaration.getAnnotationByClass(Benchmark.class).isPresent()
        ).collect(Collectors.toList());

        methodsNeedTag.stream().forEach(methodDeclaration -> methodDeclaration.addAnnotation(StaticJavaParser.parseAnnotation("@BenchmarkTag(tag=\"" + UUID.randomUUID() + "\")")));

        return cu.toString();
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
