package com.faangschool.jm0.task01_hogwarts;

import com.faangschool.jm0.util.ASTParserUtil;
import com.faangschool.jm0.util.ConsoleOutputCapturer;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Тесты для задачи 'Преподаватели Хогвартса'")
public class HogwartsTeachersTest {
    private static Path studentCodeFilePath;
    private static String studentMainClassFQN;
    private static String studentClassSimpleName = "Main";

    private static final String DEFAULT_STUDENT_CODE_PATH_STR = "src/main/java/com/faangschool/jm0/task01_hogwarts/Main.java";
    private static final String DEFAULT_STUDENT_MAIN_CLASS_FQN = "com.faangschool.jm0.task01_hogwarts.Main";

    private static Optional<CompilationUnit> cuOpt;
    private static boolean studentFileExistsAndParsed;

    @BeforeAll
    static void setup() {
        String pathStr = System.getProperty("student.code.path", DEFAULT_STUDENT_CODE_PATH_STR);
        studentCodeFilePath = Paths.get(pathStr);

        studentMainClassFQN = System.getProperty("student.main.class.fqn", DEFAULT_STUDENT_MAIN_CLASS_FQN);

        if (Files.exists(studentCodeFilePath)) {
            cuOpt = ASTParserUtil.parseStudentCode(studentCodeFilePath);
            studentFileExistsAndParsed = cuOpt.isPresent();
            if (!studentFileExistsAndParsed) {
                System.err.println("HogwartsTeachersTest: Ошибка парсинга файла студента: " + studentCodeFilePath.toAbsolutePath() + ". Проверьте файл на синтаксические ошибки.");
            }
        } else {
            System.err.println("HogwartsTeachersTest: Файл студента не найден: " + studentCodeFilePath.toAbsolutePath());
            studentFileExistsAndParsed = false;
            cuOpt = Optional.empty();
        }
    }

    private void ensureStudentFileExistsAndParsed() {
        assertTrue(studentFileExistsAndParsed,
                "Файл решения студента не найден по пути: " + studentCodeFilePath.toAbsolutePath() +
                        " или содержит синтаксические ошибки, не позволяющие его проанализировать. " +
                        "Пожалуйста, убедитесь, что файл " + studentCodeFilePath.getFileName().toString() +
                        " существует, корректен и находится в ожидаемом пакете (" +
                        (studentMainClassFQN.contains(".") ? studentMainClassFQN.substring(0, studentMainClassFQN.lastIndexOf('.')) : "default package") + ".");
    }

    private CompilationUnit getCompilationUnit() {
        ensureStudentFileExistsAndParsed();
        return cuOpt.get();
    }

    @Test
    @DisplayName("№1: Проверка, что решение находится в классе Main и использует стандартный метод main.")
    void test01_MainClassAndMethodStructure() {
        ensureStudentFileExistsAndParsed();
        CompilationUnit cu = getCompilationUnit();

        Optional<ClassOrInterfaceDeclaration> classOpt = cu.getClassByName(studentClassSimpleName);

        assertTrue(classOpt.isPresent(),
                "Ваше решение должно быть оформлено в классе " +
                        studentClassSimpleName +
                        " и содержать стандартный метод public static void main(String[] args).");

        ClassOrInterfaceDeclaration mainClass = classOpt.get();
        assertTrue(mainClass.isPublic(),
                "Ваше решение должно быть оформлено в классе " +
                        studentClassSimpleName +
                        " и содержать стандартный метод public static void main(String[] args). (Класс " + studentClassSimpleName + " должен быть public)");

        Optional<MethodDeclaration> mainMethodOpt = ASTParserUtil.findMainMethod(cu);
        assertTrue(mainMethodOpt.isPresent(),
                "Ваше решение должно быть оформлено в классе " +
                        studentClassSimpleName +
                        " и содержать стандартный метод public static void main(String[] args). (Метод main не найден или имеет неверную сигнатуру: проверьте имя, public static void, и параметры String[] args)");
    }

    @Test
    @DisplayName("№2: Проверка, что все объявленные переменные для имен преподавателей (dumbledore, mcgonagall, snape, lupin, hagrid) и заголовка (teachers) имеют тип String.")
    void test02_VariableTypesAreString() {
        ensureStudentFileExistsAndParsed();
        CompilationUnit cu = getCompilationUnit();
        List<String> expectedVarNames = Arrays.asList("dumbledore", "mcgonagall", "snape", "lupin", "hagrid", "teachers");
        String studentMessage = "Убедитесь, что все переменные, хранящие имена преподавателей и заголовок, объявлены с типом данных String.";

        for (String varName : expectedVarNames) {
            Optional<VariableDeclarator> varOpt = ASTParserUtil.findVariableInMain(cu, varName);
            assertTrue(varOpt.isPresent(), "Переменная '" + varName + "' не найдена в методе main. " + studentMessage);
            assertEquals("String", varOpt.get().getType().asString(), "Переменная '" + varName + "' должна иметь тип String. " + studentMessage);
        }
    }

    @Test
    @DisplayName("№3: Проверка существования и корректного значения переменной-заголовка teachers.")
    void test03_TeachersHeaderVariableAndFirstOutput() {
        ensureStudentFileExistsAndParsed();
        CompilationUnit cu = getCompilationUnit();
        String expectedVarName = "teachers";
        String expectedValue = "School teachers:";
        String studentMessage = "Убедитесь, что вы создали строковую переменную с именем teachers и присвоили ей значение 'School teachers:'. Эта строка должна быть выведена первой.";

        Optional<VariableDeclarator> varOpt = ASTParserUtil.findVariableInMain(cu, expectedVarName);
        assertTrue(varOpt.isPresent(), "Переменная '" + expectedVarName + "' не найдена. " + studentMessage);

        VariableDeclarator teachersVar = varOpt.get();
        assertEquals("String", teachersVar.getType().asString(), "Переменная '" + expectedVarName + "' должна быть типа String. " + studentMessage);

        Optional<StringLiteralExpr> initializerOpt = ASTParserUtil.getStringVariableInitializer(teachersVar);
        assertTrue(initializerOpt.isPresent(), "Переменной '" + expectedVarName + "' должно быть присвоено строковое значение (например, \"School teachers:\"). " + studentMessage);
        assertEquals(expectedValue, initializerOpt.get().getValue(), "Значение переменной '" + expectedVarName + "' некорректно. " + studentMessage);

        List<String> outputLines = ConsoleOutputCapturer.captureMainOutput(studentMainClassFQN);
        if (!outputLines.isEmpty() && outputLines.get(0).startsWith("TEST_FRAMEWORK_ERROR:")) {
            fail("Ошибка при выполнении кода студента: " + outputLines.get(0));
        }

        assertFalse(outputLines.isEmpty(), "Программа ничего не вывела на консоль. " + studentMessage);
        assertEquals(expectedValue, outputLines.get(0), "Первая строка вывода неверна. " + studentMessage);
    }

    @Test
    @DisplayName("№4: Проверка существования и корректного именования переменной для Albus Dumbledore.")
    void test04_DumbledoreVariableExistenceAndValue() {
        ensureStudentFileExistsAndParsed();
        CompilationUnit cu = getCompilationUnit();
        String varName = "dumbledore";
        String expectedFullName = "Albus Dumbledore";
        String studentMessage = "Пожалуйста, проверьте, что вы создали переменную с именем dumbledore для хранения имени 'Albus Dumbledore', и что ей присвоено корректное значение.";

        Optional<VariableDeclarator> varOpt = ASTParserUtil.findVariableInMain(cu, varName);
        assertTrue(varOpt.isPresent(), "Переменная '" + varName + "' не найдена. " + studentMessage);
        assertEquals("String", varOpt.get().getType().asString(), "Переменная '" + varName + "' должна быть типа String. " + studentMessage);

        Optional<StringLiteralExpr> initializerOpt = ASTParserUtil.getStringVariableInitializer(varOpt.get());
        assertTrue(initializerOpt.isPresent(), "Переменной '" + varName + "' не присвоено строковое значение. " + studentMessage);
        assertEquals(expectedFullName, initializerOpt.get().getValue(), "Значение переменной '" + varName + "' некорректно. " + studentMessage);
    }

    @Test
    @DisplayName("№5: Проверка существования и корректного именования переменной для 'Minerva McGonagall'")
    void test05_McGonagallVariableExistenceAndValue() {
        ensureStudentFileExistsAndParsed();
        CompilationUnit cu = getCompilationUnit();
        String varName = "mcgonagall";
        String expectedFullName = "Minerva McGonagall";
        String studentMessage = "Пожалуйста, проверьте, что вы создали переменную с именем mcgonagall для хранения имени 'Minerva McGonagall', и что ей присвоено корректное значение.";

        Optional<VariableDeclarator> varOpt = ASTParserUtil.findVariableInMain(cu, varName);
        assertTrue(varOpt.isPresent(), "Переменная '" + varName + "' не найдена. " + studentMessage);
        assertEquals("String", varOpt.get().getType().asString(), "Переменная '" + varName + "' должна быть типа String. " + studentMessage);

        Optional<StringLiteralExpr> initializerOpt = ASTParserUtil.getStringVariableInitializer(varOpt.get());
        assertTrue(initializerOpt.isPresent(), "Переменной '" + varName + "' не присвоено строковое значение. " + studentMessage);
        assertEquals(expectedFullName, initializerOpt.get().getValue(), "Значение переменной '" + varName + "' некорректно. " + studentMessage);
    }

    @Test
    @DisplayName("№6: Проверка существования и корректного именования переменной для 'Severus Snape'")
    void test06_SnapeVariableExistenceAndValue() {
        ensureStudentFileExistsAndParsed();
        CompilationUnit cu = getCompilationUnit();
        String varName = "snape";
        String expectedFullName = "Severus Snape";
        String studentMessage = "Пожалуйста, проверьте, что вы создали переменную с именем snape для хранения имени 'Severus Snape', и что ей присвоено корректное значение.";

        Optional<VariableDeclarator> varOpt = ASTParserUtil.findVariableInMain(cu, varName);
        assertTrue(varOpt.isPresent(), "Переменная '" + varName + "' не найдена. " + studentMessage);
        assertEquals("String", varOpt.get().getType().asString(), "Переменная '" + varName + "' должна быть типа String. " + studentMessage);

        Optional<StringLiteralExpr> initializerOpt = ASTParserUtil.getStringVariableInitializer(varOpt.get());
        assertTrue(initializerOpt.isPresent(), "Переменной '" + varName + "' не присвоено строковое значение. " + studentMessage);
        assertEquals(expectedFullName, initializerOpt.get().getValue(), "Значение переменной '" + varName + "' некорректно. " + studentMessage);
    }

    @Test
    @DisplayName("№7: Проверка существования и корректного именования переменной для 'Remus Lupin'")
    void test07_LupinVariableExistenceAndValue() {
        ensureStudentFileExistsAndParsed();
        CompilationUnit cu = getCompilationUnit();
        String varName = "lupin";
        String expectedFullName = "Remus Lupin";
        String studentMessage = "Пожалуйста, проверьте, что вы создали переменную с именем lupin для хранения имени 'Remus Lupin', и что ей присвоено корректное значение.";

        Optional<VariableDeclarator> varOpt = ASTParserUtil.findVariableInMain(cu, varName);
        assertTrue(varOpt.isPresent(), "Переменная '" + varName + "' не найдена. " + studentMessage);
        assertEquals("String", varOpt.get().getType().asString(), "Переменная '" + varName + "' должна быть типа String. " + studentMessage);

        Optional<StringLiteralExpr> initializerOpt = ASTParserUtil.getStringVariableInitializer(varOpt.get());
        assertTrue(initializerOpt.isPresent(), "Переменной '" + varName + "' не присвоено строковое значение. " + studentMessage);
        assertEquals(expectedFullName, initializerOpt.get().getValue(), "Значение переменной '" + varName + "' некорректно. " + studentMessage);
    }

    @Test
    @DisplayName("№8: Проверка существования и корректного именования переменной для 'Rubeus Hagrid'")
    void test08_HagridVariableExistenceAndValue() {
        ensureStudentFileExistsAndParsed();
        CompilationUnit cu = getCompilationUnit();
        String varName = "hagrid";
        String expectedFullName = "Rubeus Hagrid";
        String studentMessage = "Пожалуйста, проверьте, что вы создали переменную с именем hagrid для хранения имени 'Rubeus Hagrid', и что ей присвоено корректное значение.";

        Optional<VariableDeclarator> varOpt = ASTParserUtil.findVariableInMain(cu, varName);
        assertTrue(varOpt.isPresent(), "Переменная '" + varName + "' не найдена. " + studentMessage);
        assertEquals("String", varOpt.get().getType().asString(), "Переменная '" + varName + "' должна быть типа String. " + studentMessage);

        Optional<StringLiteralExpr> initializerOpt = ASTParserUtil.getStringVariableInitializer(varOpt.get());
        assertTrue(initializerOpt.isPresent(), "Переменной '" + varName + "' не присвоено строковое значение. " + studentMessage);
        assertEquals(expectedFullName, initializerOpt.get().getValue(), "Значение переменной '" + varName + "' некорректно. " + studentMessage);
    }

    @Test
    @DisplayName("№9: Проверка, что имена преподавателей выводятся именно те, что хранятся в переменных.")
    void test09_OutputUsesVariablesForTeacherNames() {
        ensureStudentFileExistsAndParsed();
        CompilationUnit cu = getCompilationUnit();
        List<MethodCallExpr> printlnCalls = ASTParserUtil.findSystemOutPrintlnCallsInMain(cu);
        String studentMessage = "Убедитесь, что для вывода имен преподавателей вы используете ранее созданные переменные, а не просто печатаете строки с их именами напрямую в System.out.println().";

        assertTrue(printlnCalls.size() >= 6, "Ожидается как минимум 6 вызовов System.out.println(). " + studentMessage);

        List<String> teacherVarNames = Arrays.asList("dumbledore", "mcgonagall", "snape", "lupin", "hagrid");

        List<MethodCallExpr> teacherNamePrintlnCalls = printlnCalls.stream().skip(1).limit(5).collect(Collectors.toList());
        assertEquals(5, teacherNamePrintlnCalls.size(), "Ожидается ровно 5 вызовов System.out.println() для имен преподавателей после вывода заголовка. " + studentMessage);

        for (int i = 0; i < teacherNamePrintlnCalls.size(); i++) {
            MethodCallExpr call = teacherNamePrintlnCalls.get(i);
            assertEquals(1, call.getArguments().size(), "System.out.println() для вывода имени " + (i+1) + "-го преподавателя должен иметь один аргумент. " + studentMessage);

            Expression arg = call.getArgument(0);
            assertTrue(arg.isNameExpr(),
                    "Аргумент System.out.println() для вывода " + (i+1) + "-го имени преподавателя (ожидаемо, переменная '" + teacherVarNames.get(i) +
                            "') должен быть переменной, а не строковым литералом. " + studentMessage);

            assertTrue(teacherVarNames.contains(arg.asNameExpr().getNameAsString()),
                    "В System.out.println() для " + (i+1) + "-го преподавателя используется переменная '" + arg.asNameExpr().getNameAsString() +
                            "', которой нет в списке ожидаемых переменных (" + String.join(", ", teacherVarNames) + "). " + studentMessage);
        }
    }

    @Test
    @DisplayName("№10: Количество строковых переменных для имен преподавателей должно быть ровно 5")
    void test10_CorrectNumberOfTeacherStringVariables() {
        ensureStudentFileExistsAndParsed();
        CompilationUnit cu = getCompilationUnit();
        List<String> teacherVarNames = Arrays.asList("dumbledore", "mcgonagall", "snape", "lupin", "hagrid");

        long count = ASTParserUtil.getVariablesInMain(cu, "String").stream()
                .filter(var -> teacherVarNames.contains(var.getNameAsString()))
                .count();

        assertEquals(5, count, "Пожалуйста, убедитесь, что вы создали ровно 5 строковых переменных для имен преподавателей (dumbledore, mcgonagall, snape, lupin, hagrid), как указано в задаче.");
    }

    @Test
    @DisplayName("№11: Проверка отсутствия лишнего вывода на консоль")
    void test11_NoExtraOutput() {
        ensureStudentFileExistsAndParsed();
        List<String> outputLines = ConsoleOutputCapturer.captureMainOutput(studentMainClassFQN);
        if (!outputLines.isEmpty() && outputLines.get(0).startsWith("TEST_FRAMEWORK_ERROR:")) {
            fail("Ошибка при выполнении кода студента: " + outputLines.get(0));
        }

        assertEquals(6, outputLines.size(), "Проверьте, пожалуйста, ваш вывод. Он должен содержать ровно 6 строк: заголовок и 5 имен преподавателей. Лишние строки или информация не допускаются. Ваш вывод содержит строк: " + outputLines.size());
    }

    @Test
    @DisplayName("№12: Проверка точного соответствия значения переменной dumbledore и соответствующей строки вывода")
    void test12_DumbledoreExactOutput() {
        ensureStudentFileExistsAndParsed();
        String expectedName = "Albus Dumbledore";
        int outputLineIndex = 1;
        String studentMessage = "Убедитесь, что значение, присвоенное переменной dumbledore, в точности 'Albus Dumbledore', включая регистр букв и отсутствие лишних пробелов, и что оно корректно выводится.";

        List<String> outputLines = ConsoleOutputCapturer.captureMainOutput(studentMainClassFQN);
        if (!outputLines.isEmpty() && outputLines.get(0).startsWith("TEST_FRAMEWORK_ERROR:")) {
            fail("Ошибка при выполнении кода студента: " + outputLines.get(0));
        }

        assertTrue(outputLines.size() > outputLineIndex, "Вывод программы слишком короткий. Ожидалось имя '" + expectedName + "' на строке " + (outputLineIndex + 1) + ". " + studentMessage);
        assertEquals(expectedName, outputLines.get(outputLineIndex), "Вторая строка вывода (Dumbledore) неверна. " + studentMessage);
    }

    @Test
    @DisplayName("№13: Проверка точного соответствия значения переменной mcgonagall и соответствующей строки вывода.")
    void test13_McGonagallExactOutput() {
        ensureStudentFileExistsAndParsed();
        String expectedName = "Minerva McGonagall";
        int outputLineIndex = 2;
        String studentMessage = "Убедитесь, что значение, присвоенное переменной mcgonagall, в точности 'Minerva McGonagall', включая регистр букв и отсутствие лишних пробелов, и что оно корректно выводится.";

        List<String> outputLines = ConsoleOutputCapturer.captureMainOutput(studentMainClassFQN);
        if (!outputLines.isEmpty() && outputLines.get(0).startsWith("TEST_FRAMEWORK_ERROR:")) {
            fail("Ошибка при выполнении кода студента: " + outputLines.get(0));
        }

        assertTrue(outputLines.size() > outputLineIndex, "Вывод программы слишком короткий. Ожидалось имя '" + expectedName + "' на строке " + (outputLineIndex + 1) + ". " + studentMessage);
        assertEquals(expectedName, outputLines.get(outputLineIndex), "Третья строка вывода (McGonagall) неверна. " + studentMessage);
    }

    @Test
    @DisplayName("№14: Проверка точного соответствия значения переменной snape и соответствующей строки вывода")
    void test14_SnapeExactOutput() {
        ensureStudentFileExistsAndParsed();
        String expectedName = "Severus Snape";
        int outputLineIndex = 3;
        String studentMessage = "Убедитесь, что значение, присвоенное переменной snape, в точности 'Severus Snape', включая регистр букв и отсутствие лишних пробелов, и что оно корректно выводится.";

        List<String> outputLines = ConsoleOutputCapturer.captureMainOutput(studentMainClassFQN);
        if (!outputLines.isEmpty() && outputLines.get(0).startsWith("TEST_FRAMEWORK_ERROR:")) {
            fail("Ошибка при выполнении кода студента: " + outputLines.get(0));
        }

        assertTrue(outputLines.size() > outputLineIndex, "Вывод программы слишком короткий. Ожидалось имя '" + expectedName + "' на строке " + (outputLineIndex + 1) + ". " + studentMessage);
        assertEquals(expectedName, outputLines.get(outputLineIndex), "Четвертая строка вывода (Snape) неверна. " + studentMessage);
    }

    @Test
    @DisplayName("№15: Проверка точного соответствия значения переменной lupin и соответствующей строки вывода")
    void test15_LupinExactOutput() {
        ensureStudentFileExistsAndParsed();
        String expectedName = "Remus Lupin";
        int outputLineIndex = 4;
        String studentMessage = "Убедитесь, что значение, присвоенное переменной lupin, в точности 'Remus Lupin', включая регистр букв и отсутствие лишних пробелов, и что оно корректно выводится.";

        List<String> outputLines = ConsoleOutputCapturer.captureMainOutput(studentMainClassFQN);
        if (!outputLines.isEmpty() && outputLines.get(0).startsWith("TEST_FRAMEWORK_ERROR:")) {
            fail("Ошибка при выполнении кода студента: " + outputLines.get(0));
        }

        assertTrue(outputLines.size() > outputLineIndex, "Вывод программы слишком короткий. Ожидалось имя '" + expectedName + "' на строке " + (outputLineIndex + 1) + ". " + studentMessage);
        assertEquals(expectedName, outputLines.get(outputLineIndex), "Пятая строка вывода (Lupin) неверна. " + studentMessage);
    }

    @Test
    @DisplayName("№16: Проверка точного соответствия значения переменной hagrid и соответствующей строки вывода")
    void test16_HagridExactOutput() {
        ensureStudentFileExistsAndParsed();
        String expectedName = "Rubeus Hagrid";
        int outputLineIndex = 5;
        String studentMessage = "Убедитесь, что значение, присвоенное переменной hagrid, в точности 'Rubeus Hagrid', включая регистр букв и отсутствие лишних пробелов, и что оно корректно выводится.";

        List<String> outputLines = ConsoleOutputCapturer.captureMainOutput(studentMainClassFQN);
        if (!outputLines.isEmpty() && outputLines.get(0).startsWith("TEST_FRAMEWORK_ERROR:")) {
            fail("Ошибка при выполнении кода студента: " + outputLines.get(0));
        }

        assertTrue(outputLines.size() > outputLineIndex, "Вывод программы слишком короткий. Ожидалось имя '" + expectedName + "' на строке " + (outputLineIndex + 1) + ". " + studentMessage);
        assertEquals(expectedName, outputLines.get(outputLineIndex), "Шестая строка вывода (Hagrid) неверна. " + studentMessage);
    }

    @Test
    @DisplayName("№17: Полный корректный вывод программы")
    void test17_FullOutputCorrectness() {
        ensureStudentFileExistsAndParsed();
        List<String> expectedOutput = Arrays.asList(
                "School teachers:",
                "Albus Dumbledore",
                "Minerva McGonagall",
                "Severus Snape",
                "Remus Lupin",
                "Rubeus Hagrid"
        );
        String studentMessage = "Кажется, общий вывод вашей программы не совпадает с ожидаемым. Пожалуйста, проверьте, что вы выводите заголовок, а затем имена всех пяти преподавателей, каждое на новой строке и в правильном порядке.";

        List<String> actualOutputLines = ConsoleOutputCapturer.captureMainOutput(studentMainClassFQN);
        if (!actualOutputLines.isEmpty() && actualOutputLines.get(0).startsWith("TEST_FRAMEWORK_ERROR:")) {
            fail("Ошибка при выполнении кода студента: " + actualOutputLines.get(0) + ". " + studentMessage);
        }


        assertEquals(expectedOutput.size(), actualOutputLines.size(),
                "Количество строк в выводе не совпадает с ожидаемым (ожидалось " + expectedOutput.size() + ", получено " + actualOutputLines.size() + "). " + studentMessage);

        for (int i = 0; i < expectedOutput.size(); i++) {
            assertEquals(expectedOutput.get(i), actualOutputLines.get(i),
                    "Строка " + (i + 1) + " вывода не совпадает. Ожидалось: '" + expectedOutput.get(i) + "', но было: '" + actualOutputLines.get(i) + "'. " + studentMessage);
        }
    }
}