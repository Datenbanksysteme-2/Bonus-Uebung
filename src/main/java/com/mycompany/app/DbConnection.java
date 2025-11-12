package com.mycompany.app;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import io.github.cdimascio.dotenv.Dotenv;

public class DbConnection {

    // Statische Variable, um die einzige Verbindung zu halten (Singleton)
    private static Connection singleConnection;
    // lib dotenv um Benutzerdaten zu verstecken
    private static final Dotenv DOTENV = Dotenv.load();
    private static final String DB_URI = DOTENV.get("DB_URI");
    private static final String DB_USER = DOTENV.get("DB_USER");
    private static final String DB_PASSWORD = DOTENV.get("DB_PASSWORD");

    private DbConnection() {
        // Privater Konstruktor, um Instanziierung zu verhindern
    }

    public static Connection getConnection() throws SQLException {
        // Wenn die Verbindung noch nicht erstellt wurde oder geschlossen ist, erstelle eine neue Verbindung
        if (singleConnection == null || singleConnection.isClosed()) {
            singleConnection = DriverManager.getConnection(DB_URI, DB_USER, DB_PASSWORD);
            singleConnection.setAutoCommit(false); // Starte eine neue Transaktion
            System.out.println("DB erfolgreich verbunden (Singleton-Instanz).");
        }
        return singleConnection;
    }

    // Statische Methode zum Schließen der einzigen Verbindung
    public static void closeConnection() throws SQLException {
        if (singleConnection != null && !singleConnection.isClosed()) {
            singleConnection.close();
            singleConnection = null; // Zurücksetzen, damit der Test erneut ausgeführt werden kann
            System.out.println("DB-Verbindung geschlossen (Singleton-Instanz).");
        }
    }
}
