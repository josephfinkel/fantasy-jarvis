package com.finkfast.fantasy.basketball.FantasyBasketballAssistant.adaptor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finkfast.fantasy.basketball.FantasyBasketballAssistant.data.BoxScoreEntry;
import com.finkfast.fantasy.basketball.FantasyBasketballAssistant.data.Game;
import com.finkfast.fantasy.basketball.FantasyBasketballAssistant.data.Teams;
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
    private static final String NBA_BOX_SCORE_URL = "http://data.nba.net/prod/v1/{date}/{gameId}_boxscore.json";
    private static final String NBA_SCHEDULE_URL = "http://data.nba.net/prod/v2/{season}/schedule.json";

    public List<Game> fetchGamesFromDateRange(LocalDate startDate, LocalDate endDate, String season) throws IOException, InterruptedException {
        List<Game> gamesList = new ArrayList<>();
        HttpResponse httpResponse = executeNBAGet(NBA_SCHEDULE_URL.replace("{season}", season));
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

    public List<BoxScoreEntry> fetchBoxScores(List<Game> gamesList) throws IOException, InterruptedException {
        List<BoxScoreEntry> boxScoreEntryList = new ArrayList<>();
        for(Game game : gamesList) {
            HttpResponse httpResponse = executeNBAGet(NBA_BOX_SCORE_URL.replace("{gameId}", game.getGameId()).replace("{date}",parseDate(game.getDate())));
            JsonNode jsonNode = new ObjectMapper().readTree(EntityUtils.toString(httpResponse.getEntity()));
            String homeTeamId = jsonNode.get("basicGameData").get("hTeam").get("teamId").asText();
            jsonNode = jsonNode.get("stats").get("activePlayers");
            int awayPlayerNumber = 0;
            int homePlayerNumber = 0;

            for(int i = 0; i < jsonNode.size(); i++) {
                BoxScoreEntry boxScoreEntry = extractBoxScoreEntry(jsonNode.get(i), game.getDate(), game.getStartTime(), game.getAwayTeam(), game.getHomeTeam(), homeTeamId,
                        awayPlayerNumber, homePlayerNumber);
                boxScoreEntryList.add(boxScoreEntry);
                if(boxScoreEntry.getTeam().equals(game.getHomeTeam())) {
                    homePlayerNumber++;
                }
                else {
                    awayPlayerNumber++;
                }
            }
        }
        return boxScoreEntryList;
    }

    private HttpResponse executeNBAGet(String uri) throws InterruptedException, IOException {
        HttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(uri);
        Thread.sleep(5000);
        return httpClient.execute(httpGet);
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

    private String parseDate (LocalDate date) {
        return String.valueOf(date.getYear()) + String.valueOf(date.getMonthValue()) + String.valueOf(date.getDayOfMonth());
    }

    private BoxScoreEntry extractBoxScoreEntry(JsonNode boxScore, LocalDate date, LocalTime time, Teams awayTeam, Teams homeTeam, String homeTeamId, int awayPlayerNumber,
                                               int homePlayerNumber) {
        String player = boxScore.get("firstName").asText() + " " + boxScore.get("lastName").asText();
        String teamId = boxScore.get("teamId").asText();
        Teams team;
        Teams opponent;
        boolean startedGame;
        if(teamId.equals(homeTeamId)) {
            team = homeTeam;
            opponent = awayTeam;
            startedGame = homePlayerNumber < 5;
        }
        else {
            team = awayTeam;
            opponent = homeTeam;
            startedGame = awayPlayerNumber < 5;
        }
        String minutes = boxScore.get("min").asText();
        Double minutesPlayed = minutes.isEmpty() ? 0.0 : Double.parseDouble(minutes.substring(0, minutes.length() - 3))
                + Double.parseDouble(minutes.substring(minutes.length() - 2))/60;
        Integer fieldGoalsMade = boxScore.get("fgm").asInt();
        Integer fieldGoalsAttempted = boxScore.get("fga").asInt();
        Integer freeThrowsMade = boxScore.get("ftm").asInt();
        Integer freeThrowsAttempted = boxScore.get("fta").asInt();
        Integer threePointersMade = boxScore.get("tpm").asInt();
        Integer threePointersAttempted = boxScore.get("tpa").asInt();
        Integer points = boxScore.get("points").asInt();
        Integer rebounds = boxScore.get("totReb").asInt();
        Integer assists = boxScore.get("assists").asInt();
        Integer steals = boxScore.get("steals").asInt();
        Integer blocks = boxScore.get("blocks").asInt();
        Integer turnovers = boxScore.get("turnovers").asInt();
        Integer plusMinus = boxScore.get("plusMinus").asInt();
        return new BoxScoreEntry(player, date, time, team, opponent, startedGame, minutesPlayed, fieldGoalsMade, fieldGoalsAttempted, freeThrowsMade, freeThrowsAttempted,
                threePointersMade, threePointersAttempted, points, rebounds, assists, steals, blocks, turnovers, 0, plusMinus);
    }
}
