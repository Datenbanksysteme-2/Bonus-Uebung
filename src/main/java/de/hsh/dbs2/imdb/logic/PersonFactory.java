package de.hsh.dbs2.imdb.logic;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import de.hsh.dbs2.imdb.util.DBConnection;

public class PersonFactory {

    /**
     * Findet eine Person anhand ihres exakten Namens.
     *
     * @param name Der exakte Name der gesuchten Person.
     * @return Das {@code Person}-Objekt oder {@code null}, wenn es nicht
     * gefunden wurde.
     * @throws SQLException
     */
    public static Person findByName(String name) throws SQLException {
        Connection conn = DBConnection.getConnection();
        String sql = "SELECT PersonID, Name FROM Person WHERE Name = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Person person = new Person();
                    person.setPersonId(rs.getLong("PersonID"));
                    person.setName(rs.getString("Name"));
                    return person;
                }
            }
        }
        return null; // Person nicht gefunden
    }
}
