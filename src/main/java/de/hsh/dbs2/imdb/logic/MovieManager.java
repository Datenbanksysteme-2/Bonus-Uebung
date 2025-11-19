package de.hsh.dbs2.imdb.logic;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import de.hsh.dbs2.imdb.logic.dto.CharacterDTO;
import de.hsh.dbs2.imdb.logic.dto.MovieDTO;
import de.hsh.dbs2.imdb.util.DBConnection;

public class MovieManager {

    /**
     * Ermittelt alle Filme, deren Filmtitel den Suchstring enthaelt. Wenn der
     * String leer ist, sollen alle Filme zurueckgegeben werden. Der Suchstring
     * soll ohne Ruecksicht auf Gross-/Kleinschreibung verarbeitet werden.
     *
     * @param search Suchstring.
     * @return Liste aller passenden Filme als MovieDTO
     * @throws Exception Beschreibt evtl. aufgetretenen Fehler
     */
    public List<MovieDTO> getMovieList(String search) throws Exception {
        try {
            List<Movie> movies = MovieFactory.findByTitle(search);
            List<MovieDTO> movieDTOs = new ArrayList<>();
            for (Movie movie : movies) {
                // Für jeden gefundenen Film die vollständigen Details abrufen
                movieDTOs.add(getMovie((int) movie.getMovieId()));
            }
            return movieDTOs;
        } catch (SQLException e) {
            throw new Exception("Error fetching movie list from database", e);
        }
    }

    /**
     * Speichert die uebergebene Version des Films neu in der Datenbank oder
     * aktualisiert den existierenden Film. Dazu werden die Daten des Films
     * selbst (Titel, Jahr, Typ) beruecksichtigt, aber auch alle Genres, die dem
     * Film zugeordnet sind und die Liste der Charaktere auf den neuen Stand
     * gebracht.
     *
     * @param movieDTO Film-Objekt mit Genres und Charakteren.
     * @throws Exception Beschreibt evtl. aufgetretenen Fehler
     */
    public void insertUpdateMovie(MovieDTO movieDTO) throws Exception {
        Connection conn = DBConnection.getConnection();
        try {
            conn.setAutoCommit(false); // Start transaction

            // Step 1: Handle the Movie object (Insert or Update)
            Movie movie = new Movie();
            movie.setTitle(movieDTO.getTitle());
            movie.setYear(movieDTO.getYear());
            movie.setType(movieDTO.getType());

            if (movieDTO.getId() == null) {
                // Insert new movie
                movie.insert();
            } else {
                // Update existing movie
                movie.setMovieId(movieDTO.getId());
                movie.update();

                // Delete old dependencies (Genres and Characters) as requested
                try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM MovieGenre WHERE MovieID = ?")) {
                    stmt.setLong(1, movie.getMovieId());
                    stmt.executeUpdate();
                }
                try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM MovieCharacter WHERE MovieID = ?")) {
                    stmt.setLong(1, movie.getMovieId());
                    stmt.executeUpdate();
                }
            }

            // Step 2: Handle Genres
            for (String genreName : movieDTO.getGenres()) {
                // Find or create the Genre to get its ID
                long genreId;
                try (PreparedStatement stmt = conn.prepareStatement("SELECT GenreID FROM Genre WHERE Genre = ?")) {
                    stmt.setString(1, genreName);
                    ResultSet rs = stmt.executeQuery();
                    if (rs.next()) {
                        genreId = rs.getLong("GenreID");
                    } else {
                        // Genre does not exist, create it using the Active Record
                        Genre newGenre = new Genre();
                        newGenre.setGenre(genreName);
                        newGenre.insert();
                        genreId = newGenre.getGenreId();
                    }
                }

                // Create the link in the MovieGenre table
                MovieGenre movieGenre = new MovieGenre();
                movieGenre.setMovieId(movie.getMovieId());
                movieGenre.setGenreId(genreId);
                movieGenre.insert();
            }

            // Step 3: Handle Characters
            for (CharacterDTO charDTO : movieDTO.getCharacters()) {
                // Find or create the Person (player) to get their ID
                long personId;
                PersonManager personManager = new PersonManager();
                try {
                    // Assumes getPerson throws an exception if not found
                    personId = personManager.getPerson(charDTO.getPlayer());
                } catch (Exception e) {
                    // Person does not exist, create them using the Active Record
                    Person newPerson = new Person();
                    newPerson.setName(charDTO.getPlayer());
                    newPerson.insert();
                    personId = newPerson.getPersonId();
                }

                // Create the MovieCharacter link using the Active Record
                MovieCharacter movieChar = new MovieCharacter();
                movieChar.setMovieId(movie.getMovieId());
                movieChar.setPlayerId(personId);
                movieChar.setCharacter(charDTO.getCharacter());
                movieChar.setAlias(charDTO.getAlias());
                movieChar.insert();
            }

            conn.commit(); // Commit the transaction
        } catch (SQLException e) {
            conn.rollback(); // Rollback on error
            throw new Exception("Error inserting/updating movie in database", e);
        }
    }

    /**
     * Loescht einen Film aus der Datenbank. Es werden auch alle abhaengigen
     * Objekte geloescht, d.h. alle Charaktere und alle Genre-Zuordnungen.
     *
     * @param movieId id des zu löschenden Films
     * @throws Exception Beschreibt evtl. aufgetretenen Fehler
     */
    public void deleteMovie(int movieId) throws Exception {
        Connection conn = DBConnection.getConnection();
        try {
            conn.setAutoCommit(false); // Start transaction

            // Dependencies are deleted by database constraints (ON DELETE CASCADE)
            Movie movie = new Movie();
            movie.setMovieId(movieId);
            movie.delete();

            conn.commit(); // Commit the transaction
        } catch (SQLException e) {
            conn.rollback(); // Rollback on error
            throw new Exception("Error deleting movie with ID " + movieId, e);
        }
    }

    /**
     * Ermittelt alle Daten zu einem Movie (d.h. auch Genres und Charaktere) und
     * trägt diese Daten in einem MovieDTO-Objekt ein.
     *
     * @param movieId ID des Films der eingelesen wird.
     * @return MovieDTO-Objekt mit allen Informationen zu dem Film
     * @throws Exception Z.B. bei Datenbank-Fehlern oder falls der Movie nicht
     * existiert.
     */
    public MovieDTO getMovie(int movieId) throws Exception {
        Connection conn = DBConnection.getConnection();
        MovieDTO movieDTO = new MovieDTO();

        // Lấy thông tin cơ bản của phim
        String movieSql = "SELECT MovieID, Title, Year, Type FROM Movie WHERE MovieID = ?";
        try (PreparedStatement stmt = conn.prepareStatement(movieSql)) {
            stmt.setInt(1, movieId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    movieDTO.setId(rs.getInt("MovieID"));
                    movieDTO.setTitle(rs.getString("Title"));
                    movieDTO.setYear(rs.getInt("Year"));
                    movieDTO.setType(rs.getString("Type"));
                } else {
                    throw new Exception("Movie with ID " + movieId + " not found.");
                }
            }
        }

        // Lấy các thể loại của phim
        String genreSql = "SELECT g.Genre FROM Genre g JOIN MovieGenre mg ON g.GenreID = mg.GenreID WHERE mg.MovieID = ?";
        try (PreparedStatement stmt = conn.prepareStatement(genreSql)) {
            stmt.setInt(1, movieId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    movieDTO.addGenre(rs.getString("Genre"));
                }
            }
        }

        // Lấy các nhân vật trong phim
        String characterSql = "SELECT p.Name, mc.Character, mc.Alias FROM Person p JOIN MovieCharacter mc ON p.PersonID = mc.PlayerID WHERE mc.MovieID = ?";
        try (PreparedStatement stmt = conn.prepareStatement(characterSql)) {
            stmt.setInt(1, movieId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    CharacterDTO characterDTO = new CharacterDTO();
                    characterDTO.setPlayer(rs.getString("Name"));
                    characterDTO.setCharacter(rs.getString("Character"));
                    characterDTO.setAlias(rs.getString("Alias"));
                    movieDTO.addCharacter(characterDTO);
                }
            }
        } catch (SQLException e) {
            throw new Exception("Error fetching movie details from database", e);
        }

        return movieDTO;
    }

}
