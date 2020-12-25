package com.finkfast.fantasy.basketball.FantasyBasketballAssistant.adaptor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finkfast.fantasy.basketball.FantasyBasketballAssistant.data.Game;
import com.finkfast.fantasy.basketball.FantasyBasketballAssistant.data.Teams;
import com.google.api.client.json.Json;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class NBAStatsAdaptor {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("h:mm a z");
    private static final String NBA_SCHEDULE_URL = "http://data.nba.net/prod/v2/{season}/schedule.json";

    public List<Game> fetchGamesFromDateRange(LocalDate startDate, LocalDate endDate, String season) throws IOException {
        List<Game> gamesList = new ArrayList<>();

        HttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(NBA_SCHEDULE_URL.replace("{season}", season));
        HttpResponse httpResponse = httpClient.execute(httpGet);
        JsonNode jsonNode = new ObjectMapper().readTree(EntityUtils.toString(httpResponse.getEntity())).get("league").get("standard");

        for(int i = 0; i < jsonNode.size(); i++) {
            JsonNode game = jsonNode.get(i);
            LocalDate date = parseDate((game.get("startDateEastern").asText()));
            if (isDateInRange(date, startDate, endDate)) {
                gamesList.add(extractGame(jsonNode.get(i), date));
            } else if (endDate.isBefore(date)) {
                return gamesList;
            }
        }
        return gamesList;
    }

    private LocalDate parseDate(String date) {
        return LocalDate.of(Integer.parseInt(date.substring(0, 4)), Integer.parseInt(date.substring(4, 6)), Integer.parseInt(date.substring(6,8)));
    }

    private Boolean isDateInRange(LocalDate date, LocalDate startDate, LocalDate endDate) {
        return ((startDate.isBefore(date) || startDate.isEqual(date)) && (endDate.isAfter(date) || endDate.isEqual(date)));
    }

    private Game extractGame(JsonNode game, LocalDate date) {
        String gameId = game.get("gameId").asText();
        LocalTime time = LocalTime.parse(game.get("startTimeEastern").asText(), FORMATTER).minusHours(3);
        String gameUrl = game.get("gameUrlCode").asText();
        return new Game(gameId, time, date, Teams.valueOf(gameUrl.substring(gameUrl.length() - 3)), Teams.valueOf(gameUrl.substring(gameUrl.length() - 6, gameUrl.length() - 3)));
    }
}
