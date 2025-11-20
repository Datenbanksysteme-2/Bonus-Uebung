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
                // Für jeden gefundenen Film die vollständigen Details abrufen.
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
            conn.setAutoCommit(false); // Transaktion starten

            // Schritt 1: Das Movie-Objekt behandeln (Einfügen oder Aktualisieren)
            Movie movie = new Movie();
            movie.setTitle(movieDTO.getTitle());
            movie.setYear(movieDTO.getYear());
            movie.setType(movieDTO.getType());

            if (movieDTO.getId() == null) {
                // Neuen Film einfügen
                movie.insert();
            } else {
                // Bestehenden Film aktualisieren
                movie.setMovieId(movieDTO.getId());
                movie.update();

                // Alte Abhängigkeiten (Genres und Charaktere) wie gewünscht löschen
                try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM MovieGenre WHERE MovieID = ?")) {
                    stmt.setLong(1, movie.getMovieId());
                    stmt.executeUpdate();
                }
                try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM MovieCharacter WHERE MovieID = ?")) {
                    stmt.setLong(1, movie.getMovieId());
                    stmt.executeUpdate();
                }
            } 

            // Schritt 2: Genres behandeln
            for (String genreName : movieDTO.getGenres()) {
                // Das Genre suchen oder erstellen, um seine ID zu erhalten
                long genreId;
                try (PreparedStatement stmt = conn.prepareStatement("SELECT GenreID FROM Genre WHERE Genre = ?")) {
                    stmt.setString(1, genreName);
                    ResultSet rs = stmt.executeQuery();
                    if (rs.next()) {
                        genreId = rs.getLong("GenreID");
                    } else {
                        // Genre existiert nicht, also mit Active Record erstellen
                        Genre newGenre = new Genre();
                        newGenre.setGenre(genreName);
                        newGenre.insert();
                        genreId = newGenre.getGenreId();
                    }
                }

                // Die Verknüpfung in der MovieGenre-Tabelle erstellen
                MovieGenre movieGenre = new MovieGenre();
                movieGenre.setMovieId(movie.getMovieId());
                movieGenre.setGenreId(genreId);
                movieGenre.insert();
            }

            // Schritt 3: Charaktere behandeln
            for (CharacterDTO charDTO : movieDTO.getCharacters()) {
                // Die Person (Schauspieler) suchen oder erstellen, um ihre ID zu erhalten
                long personId;
                PersonManager personManager = new PersonManager();
                try {
                    // Geht davon aus, dass getPerson eine Ausnahme wirft, wenn sie nicht gefunden wird
                    personId = personManager.getPerson(charDTO.getPlayer());
                } catch (Exception e) {
                    // Person existiert nicht, also mit Active Record erstellen
                    Person newPerson = new Person();
                    newPerson.setName(charDTO.getPlayer());
                    newPerson.insert();
                    personId = newPerson.getPersonId();
                }

                // Die MovieCharacter-Verknüpfung mit Active Record erstellen
                MovieCharacter movieChar = new MovieCharacter();
                movieChar.setMovieId(movie.getMovieId());
                movieChar.setPlayerId(personId);
                movieChar.setCharacter(charDTO.getCharacter());
                movieChar.setAlias(charDTO.getAlias());
                movieChar.insert();
            }

            conn.commit(); // Transaktion bestätigen
        } catch (SQLException e) {
            conn.rollback(); // Bei Fehler zurücksetzen
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
            conn.setAutoCommit(false); // Transaktion starten

            // Abhängigkeiten werden durch Datenbank-Constraints (ON DELETE CASCADE) gelöscht
            Movie movie = new Movie();
            movie.setMovieId(movieId);
            movie.delete();

            conn.commit(); // Transaktion bestätigen
        } catch (SQLException e) {
            conn.rollback(); // Bei Fehler zurücksetzen
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

        // Grundlegende Filminformationen abrufen
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

        // Filmgenres abrufen
        String genreSql = "SELECT g.Genre FROM Genre g JOIN MovieGenre mg ON g.GenreID = mg.GenreID WHERE mg.MovieID = ?";
        try (PreparedStatement stmt = conn.prepareStatement(genreSql)) {
            stmt.setInt(1, movieId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    movieDTO.addGenre(rs.getString("Genre"));
                }
            }
        }

        // Filmcharaktere abrufen
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
