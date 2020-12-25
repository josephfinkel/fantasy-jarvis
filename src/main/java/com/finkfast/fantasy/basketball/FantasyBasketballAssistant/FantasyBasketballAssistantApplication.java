package com.finkfast.fantasy.basketball.FantasyBasketballAssistant;

import com.finkfast.fantasy.basketball.FantasyBasketballAssistant.adaptor.GoogleSheetsAdaptor;
import com.finkfast.fantasy.basketball.FantasyBasketballAssistant.adaptor.NBAStatsAdaptor;
import com.finkfast.fantasy.basketball.FantasyBasketballAssistant.data.BoxScoreEntry;
import com.finkfast.fantasy.basketball.FantasyBasketballAssistant.data.Game;
import com.finkfast.fantasy.basketball.FantasyBasketballAssistant.data.Teams;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
public class FantasyBasketballAssistantApplication {

	public static void main(String[] args) {
		SpringApplication.run(FantasyBasketballAssistantApplication.class, args);
		try {
			GoogleSheetsAdaptor googleSheetsAdaptor = new GoogleSheetsAdaptor();

			NBAStatsAdaptor nbaStatsAdaptor = new NBAStatsAdaptor();
			List<Game> gamesList = nbaStatsAdaptor.fetchGamesFromDateRange(LocalDate.of(2020, 12, 22), LocalDate.of(2020, 12, 22),
					"2020");
			List<BoxScoreEntry> boxScoreEntryList = nbaStatsAdaptor.fetchBoxScores(gamesList);
			googleSheetsAdaptor.writeBoxScores(boxScoreEntryList);
		} catch (IOException | GeneralSecurityException | InterruptedException e) {
			e.printStackTrace();
		}
	}

}
