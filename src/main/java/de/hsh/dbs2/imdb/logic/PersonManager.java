package de.hsh.dbs2.imdb.logic;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import de.hsh.dbs2.imdb.util.DBConnection;

public class PersonManager {

    /**
     * Liefert eine Liste aller Personen, deren Name den Suchstring enthaelt.
     *
     * @param name Suchstring
     * @return Liste mit passenden Personennamen, die in der Datenbank
     * eingetragen sind.
     * @throws Exception Beschreibt evtl. aufgetretenen Fehler
     */
    public List<String> getPersonList(String name) throws Exception {
        List<String> personNames = new ArrayList<>();
        // Sử dụng ILIKE cho tìm kiếm không phân biệt chữ hoa chữ thường trong PostgreSQL
        String sql = "SELECT Name FROM Person WHERE Name ILIKE ?";

        Connection conn = DBConnection.getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + name.trim() + "%");

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    personNames.add(rs.getString("Name"));
                }
            }
        } catch (SQLException e) {
            throw new Exception("Error fetching person list from database", e);
        }

        return personNames;
    }

    /**
     * Liefert die ID einer Person, deren Name genau name ist. Wenn die Person
     * nicht existiert, wird eine Exception geworfen.
     *
     * @param name Exakter Name der Person
     * @return ID der Person
     * @throws Exception Beschreibt evtl. aufgetretenen Fehler
     */
    public int getPerson(String name) throws Exception {
        try {
            Person person = PersonFactory.findByName(name);
            if (person != null) {
                return (int) person.getPersonId();
            } else {
                throw new Exception("Person with name '" + name + "' not found in database.");
            }
        } catch (SQLException e) {
            throw new Exception("Error fetching person from database", e);
        }
    }
}
