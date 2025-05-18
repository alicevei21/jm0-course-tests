package com.faangschool.jm0.util;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ConsoleOutputCapturer {

    private final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private PrintStream printStream;

    public void start() {
        printStream = new PrintStream(byteArrayOutputStream);
        System.setOut(printStream);
    }

    public void stop() {
        if (printStream != null) {
            printStream.flush();
        }
        System.setOut(originalOut);
    }

    public String getOutput() {
        return byteArrayOutputStream.toString().replace("\r\n", "\n");
    }

    public List<String> getOutputLines() {
        String output = getOutput();
        if (output.isEmpty()) {
            return Collections.emptyList();
        }
        String[] lines = output.split("\n", -1);
        if (lines.length > 0 && lines[lines.length - 1].isEmpty() && output.endsWith("\n")) {
            if (output.length() == 1) {
                return Arrays.asList("");
            } else if (lines.length > 1) {
                return Arrays.asList(Arrays.copyOf(lines, lines.length -1));
            }
        }
        return Arrays.asList(lines);
    }

    public static List<String> captureMainOutput(String mainClassFQN, String[] args) {
        ConsoleOutputCapturer capturer = new ConsoleOutputCapturer();
        capturer.start();
        try {
            Class<?> mainClass = Class.forName(mainClassFQN);
            mainClass.getMethod("main", String[].class).invoke(null, (Object) args);
        } catch (Exception e) {
            System.err.println("Failed to execute main method of " + mainClassFQN + ": " + e.getMessage());
            capturer.stop();
            String currentOutput = capturer.getOutput();
            return Arrays.asList((currentOutput.isEmpty() ? "" : currentOutput + "\n") + "TEST_FRAMEWORK_ERROR: Could not run main: " + e.getMessage());
        } finally {
            if (System.out == capturer.printStream) {
                capturer.stop();
            }
        }
        return capturer.getOutputLines();
    }
    public static List<String> captureMainOutput(String mainClassFQN) {
        return captureMainOutput(mainClassFQN, new String[]{});
    }
}
