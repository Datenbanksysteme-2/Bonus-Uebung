package com.mycompany.app;

import java.sql.*;

public class MovieCharacter {
    private long movieId;
    private long playerId;
    private String character;
    private String alias;
    private Integer position;

    //Getter und Setter
    public long getMovieId() {
        return movieId;
    }

    public void setMovieId(long movieId) {
        this.movieId = movieId;
    }

    public long getPlayerId() {
        return playerId;
    }

    public void setPlayerId(long playerId) {
        this.playerId = playerId;
    }

    public String getCharacter() {
        return character;
    }

    public void setCharacter(String character) {
        this.character = character;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    //Insert-Methode
    public void insert() throws SQLException {
        Connection conn = DbConnection.getConnection();

        //Keine Sequenz nötig - Primärschlüssel ist zusammengesetzt
        String sql = "INSERT INTO MovieCharacter (MovieID, PlayerID, Character, Alias, Position) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, this.movieId);
            pstmt.setLong(2, this.playerId);
            pstmt.setString(3, this.character);

            if (this.alias != null) {
                pstmt.setString(4, this.alias);
            } else {
                pstmt.setNull(4, Types.VARCHAR);
            }

            if (this.position != null) {
                pstmt.setInt(5, this.position);
            } else {
                pstmt.setNull(5, Types.INTEGER);
            }

            pstmt.executeUpdate();
        }
    }
}