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

package com.gocypher.cybench.core.annotation;

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
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;

@SupportedAnnotationTypes(
        "org.openjdk.jmh.annotations.Benchmark")
@SupportedSourceVersion(SourceVersion.RELEASE_8)

@AutoService(Processor.class)
public class BenchmarkTagProcessor extends AbstractProcessor {

    private static Map<String, Element> knownAnnotationTags = new HashMap();

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        List<FromSourceToGenerated> createdFiles = new ArrayList<>();
        for (TypeElement annotation : annotations) {
            Set<? extends Element> annotatedElements
                    = roundEnv.getElementsAnnotatedWith(annotation);

            annotatedElements.stream().forEach(element -> {
                checkTagAnnotation(element, processingEnv.getMessager());
                if (element.getAnnotation(BenchmarkTag.class) == null) {
                    FromSourceToGenerated file = createFile(element);
                    if (file != null) {
                        createdFiles.add(file);
                    }
                }
            });

            createdFiles.forEach(fromSourceToGenerated -> {
                try {
                    Files.copy(fromSourceToGenerated.generated, fromSourceToGenerated.source, StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, e.getLocalizedMessage());
                }
            });

            if (createdFiles.size() > 0)
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Annotations created and files are updated. You need to recompile.");

        }

        return false;
    }


    private FromSourceToGenerated createFile(Element element) {
        try {
            if (element instanceof Symbol.MethodSymbol) {
                Filer filer = processingEnv.getFiler();
                Symbol.ClassSymbol classSymbol = ((Symbol.MethodSymbol) element).enclClass();
                JavaFileObject sourceFile = classSymbol.sourcefile;
                Path path = Paths.get(sourceFile.toUri());

                String fileContents = new String(Files.readAllBytes(path), "UTF-8");

                String name = String.valueOf(classSymbol.getSimpleName());


                Name pck = classSymbol.getQualifiedName().subName(0, classSymbol.getQualifiedName().lastIndexOf((byte) '.'));
                Name nm = classSymbol.getQualifiedName().subName(classSymbol.getQualifiedName().lastIndexOf((byte) '.') + 1, classSymbol.getQualifiedName().length());
                FileObject classFile = null;
                try {
                    classFile = filer.getResource(StandardLocation.SOURCE_OUTPUT, pck, nm + ".generated");
                } catch (FilerException e) {
                    // complaining about reopening/ do nothing
                }
                if (classFile != null && classFile.getLastModified() == 0) {
                    classFile = filer.createResource(StandardLocation.SOURCE_OUTPUT, pck, nm + ".generated");
                    try (PrintWriter wr = new PrintWriter(classFile.openWriter())) {
                        String replaced = getReplaced(fileContents, name, name);
                        wr.print(replaced);
                    }
                } else {
                    return null;
                }

                FromSourceToGenerated fromSourceToGenerated = new FromSourceToGenerated(path, Paths.get(classFile.toUri()));
                processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, fromSourceToGenerated.toString());

                return fromSourceToGenerated;


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
        BenchmarkTag annotation = element.getAnnotation(BenchmarkTag.class);
        if (annotation == null) return;
        try {
            if (annotation != null) {
                UUID.fromString(annotation.tag());
            }

        } catch (IllegalArgumentException r) {
            String msg = "@BenchmarkTag is not UUID like. eg. " + UUID.randomUUID();
            messager.printMessage(Diagnostic.Kind.ERROR, msg);
            throw new RuntimeException(msg);
        }

        Element put = knownAnnotationTags.put(annotation.tag(), element);
        if (put != null) {
            String msg = "@BenchmarkTag " + annotation.tag() + " is not unique. Used in: " + element.getSimpleName().toString() + " and " + put.getSimpleName().toString();
            messager.printMessage(Diagnostic.Kind.ERROR, msg);
            throw new RuntimeException(msg);
        }
    }


    private static class FromSourceToGenerated {
        Path source;
        Path generated;

        public FromSourceToGenerated(Path source, Path generated) {
            this.source = source;
            this.generated = generated;
        }

        @Override
        public String toString() {
            return "Generated new file from " + source.toAbsolutePath() + " to " + generated.toAbsolutePath() + ".";
        }
    }
}
