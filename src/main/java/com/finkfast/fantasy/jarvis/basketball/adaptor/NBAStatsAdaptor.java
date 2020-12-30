package com.finkfast.fantasy.jarvis.basketball.adaptor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finkfast.fantasy.jarvis.basketball.data.BoxScoreEntry;
import com.finkfast.fantasy.jarvis.basketball.data.Game;
import com.finkfast.fantasy.jarvis.basketball.data.Teams;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class NBAStatsAdaptor {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("h:mm a z");
    private static final String NBA_BOX_SCORE_URL = "http://data.nba.net/prod/v1/{date}/{gameId}_boxscore.json";
    private static final String NBA_PBP_URL = "http://data.nba.net/prod/v1/{date}/{gameId}_pbp_{quarter}.json";
    private static final String NBA_SCHEDULE_URL = "http://data.nba.net/prod/v2/{season}/schedule.json";

    private final HttpClient httpClient;

    public NBAStatsAdaptor() {
        httpClient = HttpClients.custom()
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setCookieSpec(CookieSpecs.STANDARD).build())
                .build();
    }

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
            Map<String, Integer> technicalFoulMap = new HashMap<>();
            Map<String, Integer> doubleTechnicalFoulMap = new HashMap<>();
            for(int i = 1; i <= game.getPeriods(); i++) {
                HttpResponse httpResponse = executeNBAGet(NBA_PBP_URL
                        .replace("{gameId}", game.getGameId())
                        .replace("{date}",parseDate(game.getDate()))
                        .replace("{quarter}", String.valueOf(i)));
                JsonNode jsonNode = new ObjectMapper().readTree(EntityUtils.toString(httpResponse.getEntity())).get("plays");

                for(int j = 0; j < jsonNode.size(); j++) {
                    if(jsonNode.get(j).get("description").asText().contains("Technical") && jsonNode.get(j).get("eventMsgType").asText().equals("6")) {
                        String playerId = jsonNode.get(j).get("personId").asText();
                        if(technicalFoulMap.containsKey(playerId)) {
                            technicalFoulMap.put(playerId, technicalFoulMap.get(playerId) + 1);
                        }
                        else {
                            technicalFoulMap.put(playerId, 1);
                        }
                    }
                    if(jsonNode.get(j).get("description").asText().contains("Double Technical") && jsonNode.get(j).get("eventMsgType").asText().equals("6")) {
                        String description = jsonNode.get(j).get("description").asText();
                        String player = description.substring(description.indexOf(",") + 2);
                        player = player.substring(0, player.indexOf("(") - 1);
                        System.out.println("Double Technical for " + player);
                        if(doubleTechnicalFoulMap.containsKey(player)) {
                            doubleTechnicalFoulMap.put(player, technicalFoulMap.get(player) + 1);
                        }
                        else {
                            doubleTechnicalFoulMap.put(player, 1);
                        }
                    }
                }
            }

            HttpResponse httpResponse = executeNBAGet(NBA_BOX_SCORE_URL
                    .replace("{gameId}", game.getGameId())
                    .replace("{date}",parseDate(game.getDate())));
            JsonNode jsonNode = new ObjectMapper().readTree(EntityUtils.toString(httpResponse.getEntity()));

            String homeTeamId = jsonNode.get("basicGameData").get("hTeam").get("teamId").asText();
            if(Objects.nonNull(jsonNode.get("stats"))) {
                jsonNode = jsonNode.get("stats").get("activePlayers");
                int awayPlayerNumber = 0;
                int homePlayerNumber = 0;

                for(int i = 0; i < jsonNode.size(); i++) {
                    BoxScoreEntry boxScoreEntry = extractBoxScoreEntry(jsonNode.get(i), game.getGameId(), game.getDate(), game.getStartTime(), game.getAwayTeam(),
                            game.getHomeTeam(), homeTeamId, awayPlayerNumber, homePlayerNumber, technicalFoulMap, doubleTechnicalFoulMap);
                    boxScoreEntryList.add(boxScoreEntry);
                    if(boxScoreEntry.getTeam().equals(game.getHomeTeam())) {
                        homePlayerNumber++;
                    }
                    else {
                        awayPlayerNumber++;
                    }
                }
            }
        }
        return boxScoreEntryList;
    }

    private HttpResponse executeNBAGet(String uri) throws InterruptedException, IOException {
        System.out.println(uri);
        HttpGet httpGet = new HttpGet(uri);
        Thread.sleep(1000);
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
        Integer periods = game.get("period").get("current").asInt();
        return new Game(gameId, time, date, Teams.valueOf(gameUrl.substring(gameUrl.length() - 3)), Teams.valueOf(gameUrl.substring(gameUrl.length() - 6, gameUrl.length() - 3)),
                periods);
    }

    private String parseDate (LocalDate date) {
        return String.valueOf(date.getYear()) + String.valueOf(date.getMonthValue()) + String.valueOf(date.getDayOfMonth());
    }

    private BoxScoreEntry extractBoxScoreEntry(JsonNode boxScore, String gameId, LocalDate date, LocalTime time, Teams awayTeam, Teams homeTeam, String homeTeamId,
                                               int awayPlayerNumber, int homePlayerNumber, Map<String, Integer> technicalFoulMap,  Map<String, Integer> doubleTechnicalFoulMap) {
        String player = boxScore.get("firstName").asText() + " " + boxScore.get("lastName").asText();
        String teamId = boxScore.get("teamId").asText();
        String playerId = boxScore.get("personId").asText();
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
        Integer technicalFouls = technicalFoulMap.getOrDefault(playerId, 0) + doubleTechnicalFoulMap.getOrDefault(boxScore.get("lastName").asText(),0);
        Integer plusMinus = boxScore.get("plusMinus").asInt();
        return new BoxScoreEntry(player, gameId, date, time, team, opponent, playerId, startedGame, minutesPlayed, fieldGoalsMade, fieldGoalsAttempted, freeThrowsMade,
                freeThrowsAttempted, threePointersMade, threePointersAttempted, points, rebounds, assists, steals, blocks, turnovers, technicalFouls, plusMinus);
    }
}
