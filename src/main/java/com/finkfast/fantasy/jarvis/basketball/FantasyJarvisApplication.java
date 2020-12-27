package com.finkfast.fantasy.jarvis.basketball;

import com.finkfast.fantasy.jarvis.basketball.adaptor.GoogleSheetsAdaptor;
import com.finkfast.fantasy.jarvis.basketball.adaptor.NBAStatsAdaptor;
import com.finkfast.fantasy.jarvis.basketball.data.BoxScoreEntry;
import com.finkfast.fantasy.jarvis.basketball.data.Game;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDate;
import java.util.List;

@SpringBootApplication
public class FantasyJarvisApplication {

	public static void main(String[] args) {
		SpringApplication.run(FantasyJarvisApplication.class, args);
		try {
			GoogleSheetsAdaptor googleSheetsAdaptor = new GoogleSheetsAdaptor();

			NBAStatsAdaptor nbaStatsAdaptor = new NBAStatsAdaptor();
			List<Game> gamesList = nbaStatsAdaptor.fetchGamesFromDateRange(LocalDate.now().minusDays(4), LocalDate.now().minusDays(1), "2020");
			List<BoxScoreEntry> boxScoreEntryList = nbaStatsAdaptor.fetchBoxScores(gamesList);
			googleSheetsAdaptor.writeBoxScores(boxScoreEntryList);
		} catch (IOException | GeneralSecurityException | InterruptedException e) {
			e.printStackTrace();
		}
	}

}
