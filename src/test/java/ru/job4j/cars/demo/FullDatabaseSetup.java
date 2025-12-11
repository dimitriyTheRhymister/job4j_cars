package ru.job4j.cars.demo;

import org.h2.tools.Server;
import java.util.Scanner;

public class FullDatabaseSetup {
    private static Server h2WebServer;

    public static void main(String[] args) throws Exception {
        System.out.println("=".repeat(50));
        System.out.println("   ПОЛНАЯ НАСТРОЙКА БАЗЫ ДАННЫХ");
        System.out.println("=".repeat(50));

        Scanner scanner = new Scanner(System.in);

        try {
            // 1. Миграции
            System.out.println("\n[1/4] Миграции базы данных...");
            DatabaseMigration.main(args);

            // 2. Тестовые данные
            System.out.println("\n[2/4] Создание тестовых данных...");
            TestDataGenerator.main(args);

            // 3. Тесты
            System.out.println("\n[3/4] Запуск тестов...");
            RepositoryTestSuite.main(args);

            // 4. H2 Console
            System.out.println("\n[4/4] Запуск H2 Web Console...");
            startH2Server();

            System.out.println("\n✅ ВСЁ ГОТОВО!");
            System.out.println("H2 Console: http://localhost:8082");
            System.out.println("\nНажмите Enter для выхода...");
            scanner.nextLine();

        } finally {
            stopH2Server();
            scanner.close();
            System.out.println("\nПрограмма завершена.");
        }
    }

    private static void startH2Server() throws Exception {
        h2WebServer = Server.createWebServer("-web", "-webPort", "8082").start();
    }

    private static void stopH2Server() {
        if (h2WebServer != null && h2WebServer.isRunning(true)) {
            h2WebServer.stop();
        }
    }
}