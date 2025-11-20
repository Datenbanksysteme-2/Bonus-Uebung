package de.hsh.dbs2.imdb.logic;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import de.hsh.dbs2.imdb.util.DBConnection;

public class GenreManager {

	/**
	 * Ermittelt eine vollstaendige Liste aller in der Datenbank abgelegten Genres
	 * Die Genres werden alphabetisch sortiert zurueckgeliefert.
	 * @return Alle Genre-Namen als String-Liste
	 * @throws Exception error describing e.g. the database problem
	 */
	public List<String> getGenres() throws Exception {
		List<String> genres = new ArrayList<>();

		Connection con = DBConnection.getConnection();
		if (con == null) {
			throw new Exception("Database connection is not open. Call DBConnection.open() before using managers.");
		}

		String sql = "SELECT Genre FROM Genre ORDER BY Genre";

		try (PreparedStatement stmt = con.prepareStatement(sql);
			 ResultSet rs = stmt.executeQuery()) {

			while (rs.next()) {
				genres.add(rs.getString(1));
			}

		} catch (SQLException e) {
			throw new Exception("Error reading genres from database", e);
		}

		return genres;
	}


}
