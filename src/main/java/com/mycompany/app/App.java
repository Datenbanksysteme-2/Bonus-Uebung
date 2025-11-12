package com.mycompany.app;

import java.sql.SQLException;

public class App {

    //Test-Methode aus der Aufgabenstellung
    public static void testInsert() throws SQLException {
        boolean ok = false;
        try {
            Person person = new Person();
            person.setName("Karl Tester");
            person.insert();

            Movie movie = new Movie();
            movie.setTitle("Die tolle Komoedie");
            movie.setYear(2012);
            movie.setType("C");
            movie.insert();

            MovieCharacter chr = new MovieCharacter();
            chr.setMovieId(movie.getMovieId());
            chr.setPlayerId(person.getPersonId());
            chr.setCharacter("Hauptrolle");
            chr.setAlias(null);
            chr.setPosition(1);
            chr.insert();

            Genre genre = new Genre();
            genre.setGenre("Unklar");
            genre.insert();

            MovieGenre movieGenre = new MovieGenre();
            movieGenre.setGenreId(genre.getGenreId());
            movieGenre.setMovieId(movie.getMovieId());
            movieGenre.insert();

            DbConnection.getConnection().commit();
        } catch (Exception e) {
            DbConnection.getConnection().rollback();
            throw e;
        }
    }

    public static void main(String[] args) {
        try {
            System.out.println("================== Test Insert beginnt ==================");
            testInsert();
            //DbConnection.closeConnection();
            System.out.println("\nALLE TESTS ERFOLGREICH ABGESCHLOSSEN!\n");
            DbConnection.closeConnection();
            System.out.println("================== Test Insert endet ==================");

        } catch (SQLException e) {
            System.err.println("Fehler: " + e.getMessage());
        }
    }
}
