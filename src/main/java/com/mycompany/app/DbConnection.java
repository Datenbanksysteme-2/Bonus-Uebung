package com.mycompany.app;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import io.github.cdimascio.dotenv.Dotenv;

public class DbConnection {

    // Statische Variable für die Singleton-Verbindung
    private static Connection singleConnection; 
    // Die Bibliothek dotenv wird verwendet, um Anmeldeinformationen zu verbergen
    private static final Dotenv DOTENV = Dotenv.load();
    private static final String DB_URI = DOTENV.get("DB_URI");
    private static final String DB_USER = DOTENV.get("DB_USER");
    private static final String DB_PASSWORD = DOTENV.get("DB_PASSWORD");

    private DbConnection() {
        // Privater Konstruktor, um die Instanziierung zu verhindern
    }

    public static Connection getConnection() throws SQLException {
        // Erstellt eine neue Verbindung, falls noch keine existiert oder die bestehende geschlossen ist
        if (singleConnection == null || singleConnection.isClosed()) {
            singleConnection = DriverManager.getConnection(DB_URI, DB_USER, DB_PASSWORD);
            singleConnection.setAutoCommit(false); // Startet eine neue Transaktion
            System.out.println("DB erfolgreich verbunden (Singleton-Instanz).");
        }
        return singleConnection;
    }

    // Statische Methode zum Schließen der Singleton-Verbindung
    public static void closeConnection() throws SQLException {
        if (singleConnection != null && !singleConnection.isClosed()) {
            singleConnection.close();
            singleConnection = null; // Zurücksetzen, damit Tests erneut ausgeführt werden können
            System.out.println("DB-Verbindung geschlossen (Singleton-Instanz).");
        }
    }
}
