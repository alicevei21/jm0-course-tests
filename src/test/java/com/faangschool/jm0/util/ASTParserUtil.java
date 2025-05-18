package com.faangschool.jm0.util;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ASTParserUtil {

    /**
     * Парсит Java файл студента.
     * @param studentCodeFilePath Путь к файлу студента.
     * @return Optional с CompilationUnit или Optional.empty() в случае ошибки.
     */
    public static Optional<CompilationUnit> parseStudentCode(Path studentCodeFilePath) {
        File studentFile = studentCodeFilePath.toFile();
        if (!studentFile.exists()) {
            System.err.println("ASTParserUtil: Student code file not found at: " + studentCodeFilePath);
            return Optional.empty();
        }
        try {
            return Optional.of(StaticJavaParser.parse(studentFile));
        } catch (IOException e) {
            System.err.println("ASTParserUtil: Failed to read student code file " + studentCodeFilePath + ": " + e.getMessage());
            return Optional.empty();
        } catch (Exception e) {
            System.err.println("ASTParserUtil: Failed to parse student code in " + studentCodeFilePath + ": " + e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Находит метод main(String[] args) в CompilationUnit.
     */
    public static Optional<MethodDeclaration> findMainMethod(CompilationUnit cu) {
        return cu.findFirst(MethodDeclaration.class, md ->
                        md.isPublic() &&
                                md.isStatic() &&
                                md.getType().isVoidType() &&
                                md.getNameAsString().equals("main") &&
                                md.getParameters().size() == 1 &&
                                md.getParameter(0).getType().asString().equals("String[]")
        );
    }

    /**
     * Получает все объявления переменных указанного типа в методе main.
     */
    public static List<VariableDeclarator> getVariablesInMain(CompilationUnit cu, String typeName) {
        return findMainMethod(cu)
                .flatMap(MethodDeclaration::getBody)
                .map(blockStmt -> blockStmt.findAll(VariableDeclarator.class).stream()
                        .filter(var -> var.getType().asString().equals(typeName))
                        .collect(Collectors.toList()))
                .orElse(List.of());
    }

    /**
     * Находит объявление переменной по имени в методе main.
     */
    public static Optional<VariableDeclarator> findVariableInMain(CompilationUnit cu, String varName) {
        return findMainMethod(cu)
                .flatMap(MethodDeclaration::getBody)
                .flatMap(blockStmt -> blockStmt.findAll(VariableDeclarator.class).stream()
                        .filter(var -> var.getNameAsString().equals(varName))
                        .findFirst());
    }

    /**
     * Получает строковый литерал, которым инициализирована переменная, если это возможно.
     */
    public static Optional<StringLiteralExpr> getStringVariableInitializer(VariableDeclarator var) {
        if (var.getInitializer().isPresent() && var.getInitializer().get().isStringLiteralExpr()) {
            return Optional.of(var.getInitializer().get().asStringLiteralExpr());
        }
        return Optional.empty();
    }

    /**
     * Находит все вызовы System.out.println() в методе main.
     */
    public static List<MethodCallExpr> findSystemOutPrintlnCallsInMain(CompilationUnit cu) {
        return findMainMethod(cu)
                .flatMap(MethodDeclaration::getBody)
                .map(blockStmt -> blockStmt.findAll(MethodCallExpr.class).stream()
                        .filter(mc -> mc.getScope().isPresent() &&
                                mc.getScope().get().isFieldAccessExpr() &&
                                mc.getScope().get().asFieldAccessExpr().getScope().isNameExpr() &&
                                mc.getScope().get().asFieldAccessExpr().getScope().asNameExpr().getNameAsString().equals("System") &&
                                mc.getScope().get().asFieldAccessExpr().getNameAsString().equals("out") &&
                                mc.getNameAsString().equals("println"))
                        .collect(Collectors.toList()))
                .orElse(List.of());
    }
}
