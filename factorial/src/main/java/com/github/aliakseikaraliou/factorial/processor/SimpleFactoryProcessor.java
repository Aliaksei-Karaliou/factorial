package com.github.aliakseikaraliou.factorial.processor;

import com.github.aliakseikaraliou.factorial.SimpleFactory;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.*;
import javax.lang.model.util.ElementFilter;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

import static javax.lang.model.element.ElementKind.CONSTRUCTOR;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.tools.Diagnostic.Kind.ERROR;

@SupportedAnnotationTypes("com.github.aliakseikaraliou.factorial.SimpleFactory")
@AutoService(Processor.class)
public class SimpleFactoryProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations,
                           RoundEnvironment roundEnv) {
        if (annotations.isEmpty()) {
            return false;
        }

        List<FactoryMethod> factoryMethods = new ArrayList<>();

        try {
            roundEnv
                    .getElementsAnnotatedWith(SimpleFactory.class)
                    .forEach(element -> {
                        try {
                            switch (element.getKind()) {
                                case CLASS -> {
                                    factoryMethods.addAll(
                                            parseAsClass(((TypeElement) element))
                                    );
                                }
                                case INTERFACE -> printError("Cannot create factory for interface", element);
                                default -> printError("Unknown factory annotation usage", element);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
        } catch (Exception e) {
            printError("Exception occurred" + e.getMessage(), null);
        }

        factoryMethods
                .stream()
                .collect(Collectors.groupingBy(FactoryMethod::getRoot))
                .entrySet();
//                .forEach(entry->printToFile(entry));

        return true;
    }

    private List<FactoryMethod> parseAsClass(TypeElement typeElement) throws IOException {
        if (typeElement.getNestingKind().isNested()) {
            printError("Cannot create factory for nested class", typeElement);
            return Collections.emptyList();
        }

        List<FactoryMethod> factoryMethods = new ArrayList<>();

        typeElement
                .getEnclosedElements()
                .forEach(enclosedElement -> {
                    if (enclosedElement.getKind() == CONSTRUCTOR) {
                        factoryMethods.add(
                                parseAsConstructor(((ExecutableElement) enclosedElement))
                        );
                    }
                });

        return factoryMethods;
    }

    private FactoryMethod parseAsConstructor(ExecutableElement enclosedElement) {
        return FactoryMethod.builder()
                .setRoot(((TypeElement) enclosedElement.getEnclosingElement()).getQualifiedName().toString())
                .setParams(enclosedElement.getParameters())
                .setElement(enclosedElement)
                .build();
    }

    private void printToFile(Map.Entry<String, List<FactoryMethod>> factory, SimpleFactory simpleFactoryAnnotation) {
        String root = factory.getKey();
        String packageName = root.substring(0, root.lastIndexOf("."));
        String className = root.substring(root.lastIndexOf(".") + 1) + "Factory";

        JavaFileObject sourceFile;

        try {
            sourceFile = processingEnv
                    .getFiler()
                    .createSourceFile(root + "Factory");
        } catch (IOException e) {
            printError("Cannot create source file", null);
            return;
        }

        try (PrintWriter out = new PrintWriter(sourceFile.openWriter())) {
            TypeSpec.Builder factoryBuilder = TypeSpec
                    .classBuilder(className)
//                    .add
                    .addModifiers(PUBLIC);

            for (FactoryMethod factoryMethod : factory.getValue()) {
//                Const
            }

            JavaFile
                    .builder(packageName, factoryBuilder.build())
                    .build()
                    .writeTo(out);

        } catch (IOException e) {
            printError("Cannot write to source file", null);
        }
    }

    public void printError(String message, Element element) {
        processingEnv
                .getMessager()
                .printMessage(ERROR, message, element);
    }

}
