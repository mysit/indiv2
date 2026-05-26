import java.io.*;
import java.sql.Array;
import java.util.*;

public class MaxElementFinder {
    private static final Object fileLock = new Object();

    static class FileTask implements Runnable {
        private final String inputFile;
        private final String outputFile;
        private Double maxValue;  // Результат обработки

        public FileTask(String inputFile, String outputFile) {
            this.inputFile = inputFile;
            this.outputFile = outputFile;
        }


        @Override
        public void run() {
            System.out.printf("обработка" + Thread.currentThread().getName(), inputFile);
            maxValue = findMax();

            if (maxValue != null) {
                synchronized (fileLock) {
                    writeRes();
                }
            }
        }

        private Double findMax() {
            Double max = null;
            try (Scanner scanner = new Scanner(new File(inputFile))) {
                scanner.useLocale(Locale.US);
                while (scanner.hasNext()) {
                    if (scanner.hasNextDouble()) {
                        double val = scanner.nextDouble();
                        if (max == null || val > max) max = val;
                    } else scanner.next();
                }
            } catch (FileNotFoundException e) {
                System.err.printf("Файл не найден");
            }
            return max;
        }

        public void writeRes() {
            try (FileWriter fw = new FileWriter(outputFile, true); PrintWriter pw = new PrintWriter(fw)) {
                pw.printf("%s: %.4f%n", inputFile, maxValue);
            } catch (IOException e) {
                System.err.println("vse ploho");
            }
        }

        public Double getMaxValue() {
            return maxValue;
        }

        public String getInputFile() {
            return inputFile;
        }
    }

    public static void processFiles(String[] inputFiles, String outputFile) {
        try (PrintWriter pw = new PrintWriter(outputFile)) {
            //создаём пустой файл
        } catch (FileNotFoundException e) {
            // Если не удалось создать файл (например, нет прав),
            // прекращаем работу всей программы
            System.err.println("Не удалось создать выходной файл");
            return;  // Выходим из метода
        }

        List<Thread> threads = new ArrayList<>();
        List<FileTask> ftasks = new ArrayList<>();

        for (String fileName : inputFiles) {
            FileTask task = new FileTask(fileName, outputFile);
            ftasks.add(task);

            Thread thread = new Thread(task);
            threads.add(thread);

            thread.start();
        }

        for (Thread thread : threads) {
            try {
                thread.join();

            } catch (InterruptedException e) {
                // Если главный поток был прерван во время ожидания
                Thread.currentThread().interrupt();
            }
        }
        // здесь ВСЕ потоки гарантированно завершили работу

        System.out.println("\nРезультаты обработки:");
        for (FileTask task : ftasks) {
            // проверяем, что файл не был пустым
            if (task.getMaxValue() != null) {
                // форматированный вывод: имя_файла -> число
                System.out.printf("  %s -> %.4f%n",
                        task.getInputFile(),
                        task.getMaxValue());
            } else {
                System.out.printf("  %s -> нет данных%n", task.getInputFile());
            }
        }
    }


    public static void main(String[] args) {


        // Этап 2: Обработка - запускаем многопоточную обработку
        String[] files = {"test1.txt", "test2.txt", "test3.txt"};
        processFiles(files, "results.txt");

        // Этап 3: Проверка - выводим содержимое выходного файла
        System.out.println("\nСодержимое results.txt:");
        try (Scanner scanner = new Scanner(new File("results.txt"))) {
            // Построчно читаем и выводим
            while (scanner.hasNextLine()) {
                System.out.println("  " + scanner.nextLine());
            }
        } catch (FileNotFoundException e) {
            System.err.println("Файл результатов не найден");
        }
    }
}
