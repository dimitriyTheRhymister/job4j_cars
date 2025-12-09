package ru.job4j.cars.demo;

import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;

import java.sql.Connection;
import java.sql.DriverManager;

public class LiquibaseRunner {
    public static void main(String[] args) {
        Connection connection = null;
        try {
            // 1. Подключаемся к БД
            Class.forName("org.h2.Driver");
            connection = DriverManager.getConnection(
                    "jdbc:h2:file:./db/job4j_cars;DB_CLOSE_DELAY=-1",
                    "sa",
                    ""
            );

            // 2. Создаем Liquibase
            Database database = DatabaseFactory.getInstance()
                    .findCorrectDatabaseImplementation(new JdbcConnection(connection));

            Liquibase liquibase = new Liquibase(
                    "db/changelog/db.changelog-master.xml",
                    new ClassLoaderResourceAccessor(),
                    database
            );

            // 3. Выполняем миграции
            System.out.println("Запуск Liquibase миграций...");
            liquibase.update(new Contexts(), new LabelExpression());
            System.out.println("✓ Миграции успешно выполнены!");

            // 4. Проверяем комментарии
            checkComments(connection);

            System.out.println("\n✓ Теперь можно запускать H2DatabaseDemo!");

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("✗ Ошибка при выполнении миграций!");
        } finally {
            if (connection != null) {
                try { connection.close(); } catch (Exception e) { e.printStackTrace(); }
            }
        }
    }

    private static void checkComments(Connection conn) throws Exception {
        var stmt = conn.createStatement();

        System.out.println("\n=== ПРОВЕРКА КОММЕНТАРИЕВ ===");

        // Проверяем комментарии к таблицам
        var rs = stmt.executeQuery(
                "SELECT TABLE_NAME, REMARKS " +
                        "FROM INFORMATION_SCHEMA.TABLES " +
                        "WHERE TABLE_SCHEMA = 'PUBLIC' AND REMARKS IS NOT NULL"
        );

        System.out.println("Таблицы с комментариями:");
        while (rs.next()) {
            System.out.printf("  %-20s: %s%n",
                    rs.getString("TABLE_NAME"),
                    rs.getString("REMARKS"));
        }
        rs.close();

        // Проверяем комментарии к колонкам таблицы CARS
        rs = stmt.executeQuery(
                "SELECT COLUMN_NAME, REMARKS " +
                        "FROM INFORMATION_SCHEMA.COLUMNS " +
                        "WHERE TABLE_NAME = 'CARS' AND REMARKS IS NOT NULL"
        );

        System.out.println("\nКомментарии к колонкам таблицы CARS:");
        boolean hasComments = false;
        while (rs.next()) {
            hasComments = true;
            System.out.printf("  %-20s: %s%n",
                    rs.getString("COLUMN_NAME"),
                    rs.getString("REMARKS"));
        }

        if (!hasComments) {
            System.out.println("  (комментариев нет)");
        }

        rs.close();
        stmt.close();
    }
}