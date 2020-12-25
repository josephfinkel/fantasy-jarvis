package com.finkfast.fantasy.basketball.FantasyBasketballAssistant;

import com.finkfast.fantasy.basketball.FantasyBasketballAssistant.adaptor.GoogleSheetsAdaptor;
import com.finkfast.fantasy.basketball.FantasyBasketballAssistant.config.GoogleSheetsConfig;
import com.finkfast.fantasy.basketball.FantasyBasketballAssistant.data.BoxScoreEntry;
import com.finkfast.fantasy.basketball.FantasyBasketballAssistant.data.Teams;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;

@SpringBootApplication
public class FantasyBasketballAssistantApplication {

	public static void main(String[] args) {
		SpringApplication.run(FantasyBasketballAssistantApplication.class, args);
		try {
			GoogleSheetsAdaptor googleSheetsAdaptor = new GoogleSheetsAdaptor();
			BoxScoreEntry boxScoreEntry = new BoxScoreEntry("Trae Young", LocalDate.of(2020,12,23), LocalTime.of(19,0), Teams.ATL,Teams.CHI,
					true,25.0+58/60,10,12,12,14,5,6,
					37,6,7,0,0,4,0,30);
			googleSheetsAdaptor.writeBoxScores(Collections.singletonList(boxScoreEntry));
		} catch (IOException e) {
			e.printStackTrace();
		} catch (GeneralSecurityException e) {
			e.printStackTrace();
		}
	}

}
