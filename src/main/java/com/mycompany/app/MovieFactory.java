package com.mycompany.app;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MovieFactory {

    /**
     * Findet einen Film anhand seiner ID
     *
     * @param id Die ID des gesuchten Films
     * @return Das {@code Movie}-Objekt oder {@code null}, wenn es nicht
     * gefunden wurde.
     */
    public static Movie findById(long id) throws SQLException {
        Connection conn = DbConnection.getConnection();

        String sql = "SELECT MovieID, Title, Year, Type FROM Movie WHERE MovieID = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Movie movie = new Movie();
                    movie.setMovieId(rs.getLong("MovieID"));
                    movie.setTitle(rs.getString("Title"));
                    movie.setYear(rs.getInt("Year"));
                    movie.setType(rs.getString("Type"));
                    return movie;
                }
            }
        }

        return null;
    }

    /**
     * Findet alle Filme, deren Titel den Suchstring enthält
     *
     * @param title Der gesuchte Titel (oder Teilstring)
     * @return Eine Liste aller gefundenen Filme.
     */
    public static List<Movie> findByTitle(String title) throws SQLException {
        List<Movie> movies = new ArrayList<>();
        Connection conn = DbConnection.getConnection();

        // LIKE für eine Suche ohne Berücksichtigung der Groß- und Kleinschreibung in PostgreSQL
        String sql = "SELECT MovieID, Title, Year, Type FROM Movie WHERE Title LIKE ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // % für die Wildcard-Suche (Teilstring-Abgleich)
            pstmt.setString(1, "%" + title + "%");

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Movie movie = new Movie();
                    movie.setMovieId(rs.getLong("MovieID"));
                    movie.setTitle(rs.getString("Title"));
                    movie.setYear(rs.getInt("Year"));
                    movie.setType(rs.getString("Type"));
                    movies.add(movie);
                }
            }
        }

        return movies;
    }
}
