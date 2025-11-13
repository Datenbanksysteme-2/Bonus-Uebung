package com.mycompany.app;

import java.sql.SQLException;
import java.util.List;

public class App {

    //Test-Methode aus der Aufgabenstellung
    public static void testInsert() throws SQLException {
        boolean ok = false;
        try {

            //=================== 1. Film ===================//
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
            // =================== 2. Film ===================//
            Person person2 = new Person();
            person2.setName("Karl Tester 2");
            person2.insert();

            Movie movie2 = new Movie();
            movie2.setTitle("Die tolle Komoedie 2");
            movie2.setYear(2012);
            movie2.setType("C");
            movie2.insert();
            MovieCharacter chr2 = new MovieCharacter();
            chr2.setMovieId(movie2.getMovieId());
            chr2.setPlayerId(person2.getPersonId());
            chr2.setCharacter("Hauptrolle 2");
            chr2.setAlias(null);
            chr2.setPosition(1);
            chr2.insert();
            Genre genre2 = new Genre();
            genre2.setGenre("Unklar 2");
            genre2.insert();
            MovieGenre movieGenre2 = new MovieGenre();
            movieGenre2.setGenreId(genre2.getGenreId());
            movieGenre2.setMovieId(movie2.getMovieId());
            movieGenre2.insert();
            // =================== 3. Film ===================//
            Person person3 = new Person();
            person3.setName("Karl Tester 3");
            person3.insert();

            Movie movie3 = new Movie();
            movie3.setTitle("Die tolle Komoedie 3");
            movie3.setYear(2012);
            movie3.setType("C");
            movie3.insert();
            MovieCharacter chr3 = new MovieCharacter();
            chr3.setMovieId(movie3.getMovieId());
            chr3.setPlayerId(person3.getPersonId());
            chr3.setCharacter("Hauptrolle 3");
            chr3.setAlias(null);
            chr3.setPosition(1);
            chr3.insert();
            Genre genre3 = new Genre();
            genre3.setGenre("Unklar 3");
            genre3.insert();
            MovieGenre movieGenre3 = new MovieGenre();
            movieGenre3.setGenreId(genre3.getGenreId());
            movieGenre3.setMovieId(movie3.getMovieId());
            movieGenre3.insert();

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
            System.out.println("\nALLE DATEN ERFOLGREICH EINGEFÜGT\n");
            System.out.println("================== Test Insert endet ==================");

            System.out.println("================== Test FindById and FindByTitle beginnt ==================");
            Movie mv = MovieFactory.findById(3);
            System.out.println("Gefundener Film By Id: " + mv.getTitle());
            List<Movie> listByTitle = MovieFactory.findByTitle("Die tolle Komoedie");
            for (Movie m : listByTitle) {
                System.out.println("Gefundener Film mit Titel: " + m.getTitle());
            }
            DbConnection.closeConnection();

        } catch (SQLException e) {
            System.err.println("Fehler: " + e.getMessage());
        }
    }
}
