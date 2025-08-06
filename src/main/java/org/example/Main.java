package org.example;

import org.apache.commons.cli.*;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

public class Main {
    private static final Map<String, List<String>> data = new HashMap<>();

    public static void main(String[] args) {

        CommandLine cmd = parseArguments(args);
        if (cmd == null) return;


        List<String> inputFiles = Arrays.asList(cmd.getArgs());
        if (inputFiles.isEmpty()) {
            System.err.println("Ошибка: Файл с таким именем не найден");
            return;
        }

        List<String> lines = readLinesFromFiles(inputFiles);
        if (lines.isEmpty()) {
            System.err.println("Ошибка: Файл пустой");
            return;
        }


        filterData(lines);


        String outputDir = cmd.getOptionValue("o", ".");
        String prefix = cmd.getOptionValue("p", "");
        boolean appendMode = cmd.hasOption("a");


        writeResults(outputDir, prefix, appendMode);


        if (cmd.hasOption("s") || cmd.hasOption("f")) {
            printStatistics(cmd.hasOption("f"));
        }
    }

    private static CommandLine parseArguments(String[] args) {
        Options options = new Options()
                .addOption("o", "output", true, "Путь для выходных файлов")
                .addOption("p", "prefix", true, "Префикс имен файлов")
                .addOption("a", "append", false, "Режим добавления в файлы")
                .addOption("s", "short-stats", false, "Краткая статистика")
                .addOption("f", "full-stats", false, "Полная статистика");

        try {
            return new DefaultParser().parse(options, args);
        } catch (ParseException e) {
            System.err.println("Ошибка парсинга аргументов: " + e.getMessage());
            return null;
        }
    }

    private static List<String> readLinesFromFiles(List<String> filePaths) {
        List<String> lines = new ArrayList<>();
        for (String filePath : filePaths) {
            try {
                lines.addAll(Files.readAllLines(Path.of(filePath), StandardCharsets.UTF_8));
            } catch (IOException e) {
                System.err.println("Ошибка чтения файла " + filePath + ": " + e.getMessage());
            }
        }
        return lines;
    }

    private static void filterData(List<String> lines) {
        data.put("integers", new ArrayList<>());
        data.put("floats", new ArrayList<>());
        data.put("strings", new ArrayList<>());

        for (String line : lines) {
            if (line == null || line.trim().isEmpty()) continue;

            String type = detectType(line.trim());
            data.get(type).add(line);
        }
    }

    public static String detectType(String line) {
        if (line.matches("^-?\\d+$")) {
            return "integers";
        } else if (line.matches("^-?\\d+(\\.\\d+)?([eE][+-]?\\d+)?$")) {
            return "floats";
        } else {
            return "strings";
        }
    }

    private static void writeResults(String outputDir, String prefix, boolean appendMode) {
        data.forEach((type, lines) -> {
            if (lines.isEmpty()) return;

            Path outputPath = Path.of(outputDir, prefix + type + ".txt");
            try {
                Files.createDirectories(outputPath.getParent());
                writeToFile(lines, outputPath, appendMode);
                System.out.println("Создан файл: " + outputPath.toAbsolutePath());
            } catch (IOException e) {
                System.err.println("Ошибка записи в файл " + outputPath + ": " + e.getMessage());
            }
        });
    }

    private static void writeToFile(List<String> lines, Path filePath, boolean appendMode) throws IOException {
        StandardOpenOption option = appendMode ? StandardOpenOption.APPEND : StandardOpenOption.TRUNCATE_EXISTING;

        try (BufferedWriter writer = Files.newBufferedWriter(
                filePath,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                option
        )) {
            for (String line : lines) {
                writer.write(line);
                writer.newLine();
            }
        }
    }

    private static void printStatistics(boolean fullStats) {
        data.forEach((type, lines) -> {
            System.out.println("\n=== " + type.toUpperCase() + " ===");
            System.out.println("Количество: " + lines.size());

            if (fullStats && !lines.isEmpty()) {
                if (type.equals("integers") || type.equals("floats")) {
                    printNumericStats(lines, type.equals("integers"));
                } else {
                    printStringStats(lines);
                }
            }
        });
    }

    private static void printNumericStats(List<String> numbers, boolean isInteger) {
        List<Double> nums = numbers.stream()
                .map(Double::parseDouble)
                .collect(Collectors.toList());

        DoubleSummaryStatistics stats = nums.stream()
                .mapToDouble(Double::doubleValue)
                .summaryStatistics();

        System.out.println("Минимальное: " + (isInteger ? (int) stats.getMin() : stats.getMin()));
        System.out.println("Максимальное: " + (isInteger ? (int) stats.getMax() : stats.getMax()));
        System.out.println("Сумма: " + (isInteger ? (int) stats.getSum() : stats.getSum()));
        System.out.println("Среднее: " + stats.getAverage());
    }

    private static void printStringStats(List<String> strings) {
        IntSummaryStatistics stats = strings.stream()
                .mapToInt(String::length)
                .summaryStatistics();

        System.out.println("Самая короткая: " + stats.getMin() + " символов");
        System.out.println("Самая длинная: " + stats.getMax() + " символов");
        System.out.println("Средняя длина: " + stats.getAverage() + " символов");
    }
}