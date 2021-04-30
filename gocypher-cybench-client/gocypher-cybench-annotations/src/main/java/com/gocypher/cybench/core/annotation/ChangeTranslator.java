package com.gocypher.cybench.core.annotation;

import com.sun.source.tree.Tree;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.Name;
import org.junit.Test;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

import javax.lang.model.element.Element;
import java.util.List;

public class ChangeTranslator extends TreeTranslator {
    private final CreateStatement createStatement;

    public ChangeTranslator(JavacProcessingEnvironment javacProcessingEnvironment, TreeMaker treeMaker) {
        createStatement = new CreateStatement(new GetElement(javacProcessingEnvironment), treeMaker);
    }

    @Override
    public void visitClassDef(JCTree.JCClassDecl jcClassDecl) {
        super.visitClassDef(jcClassDecl);
        if (!isContainsAnnotation(jcClassDecl.getModifiers()) && notContainBenchmark(jcClassDecl) && containsTest(jcClassDecl)) {
            // result is placed into the AST, replacing the current variable declaration.
            result = createStatement.apply(jcClassDecl);
        }
    }

    private boolean containsTest(JCTree.JCClassDecl jcClassDecl) {
        return  jcClassDecl.getMembers().stream()
                .filter(e -> e.getKind() == Tree.Kind.METHOD)
                .map(m -> (JCTree.JCMethodDecl) m)
                .anyMatch(m -> containsAnnotation(m.getModifiers(),"Test")); //FIXME please
    }


    private boolean notContainBenchmark(JCTree.JCClassDecl jcClassDecl) {
        return  jcClassDecl.getMembers().stream()
                .filter(e -> e.getKind() == Tree.Kind.METHOD)
                .map(m -> (JCTree.JCMethodDecl) m)
                .noneMatch(m -> containsAnnotation(m.getModifiers(), Benchmark.class.getName()));
    }



    private boolean isContainsAnnotation(JCTree.JCModifiers modifiers) {
        return containsAnnotation(modifiers,State.class.getName());
    }

    private boolean containsAnnotation(JCTree.JCModifiers modifiers, String qualifiedANme  ) {
        return modifiers.getAnnotations().stream().filter(a -> a.getAnnotationType().toString().equals(qualifiedANme)).findAny().isPresent();
    }


    public static class GetElement {
        private final JavacProcessingEnvironment javacProcessingEnvironment;

        public GetElement(JavacProcessingEnvironment javacProcessingEnvironment) {
            this.javacProcessingEnvironment = javacProcessingEnvironment;
        }

        public Symbol apply(Class<?> javaClass) {
            return (Symbol) getPackageElements(javaClass)
                    .stream()
                    .filter(element -> element.getSimpleName().toString().equals(javaClass.getSimpleName()))
                    .findAny()
                    .orElseThrow(() -> new AssertionError("Unable to get " + javaClass));
        }

        public Symbol apply(Enum javaEnum) {
            return (Symbol) getClassElements(javaEnum.getDeclaringClass())
                    .stream()
                    .filter(s->s.getSimpleName().contentEquals("Benchmark"))
                    .findAny()
                    .orElseThrow(() -> new AssertionError("Unable to get " + javaEnum));
        }

        private List<? extends Element> getPackageElements(Class<?> javaClass) {
            return javacProcessingEnvironment.getElementUtils().getPackageElement(javaClass.getPackage().getName()).getEnclosedElements();
        }

        private List<? extends Element> getClassElements(Class<?> javaClass) {
            return javacProcessingEnvironment.getElementUtils().getTypeElement(javaClass.getName()).getEnclosedElements();
        }

    }

    public class CreateStatement {
        private final GetElement getElement;
        private final TreeMaker treeMaker;

        public CreateStatement(GetElement getElement, TreeMaker treeMaker) {
            this.getElement = getElement;
            this.treeMaker = treeMaker;
        }

        //JCModifiers var1, Name var2, List<JCTypeParameter> var3, JCExpression var4, List<JCExpression> var5, List<JCTree> var6

        public JCTree.JCClassDecl apply(JCTree.JCClassDecl classDeclaration) {
            JCTree.JCModifiers modifiers1 = classDeclaration.getModifiers();

            com.sun.tools.javac.util.List<JCTree.JCExpression> symbols = com.sun.tools.javac.util.List.of(treeMaker.QualIdent(getElement.apply(Scope.Benchmark)));
            JCTree.JCAnnotation annotation = treeMaker.TypeAnnotation(treeMaker.QualIdent(getElement.apply(State.class)), symbols);
            modifiers1.annotations = modifiers1.annotations.append(annotation);

            Name name = classDeclaration.getSimpleName();
            com.sun.tools.javac.util.List<JCTree.JCTypeParameter> typeParameters = classDeclaration.getTypeParameters();
            JCTree.JCExpression extendsClause = classDeclaration.getExtendsClause();
            com.sun.tools.javac.util.List<JCTree.JCExpression> implementsClause = classDeclaration.getImplementsClause();
            com.sun.tools.javac.util.List<JCTree> members = classDeclaration.getMembers();

            JCTree.JCClassDecl newVariableDeclaration = treeMaker.ClassDef(modifiers1, name, typeParameters, extendsClause, implementsClause, members);
            return newVariableDeclaration;
        }
    }
}
