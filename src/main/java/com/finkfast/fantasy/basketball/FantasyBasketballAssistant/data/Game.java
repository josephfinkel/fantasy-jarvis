package com.finkfast.fantasy.basketball.FantasyBasketballAssistant.data;

import java.time.LocalDate;
import java.time.LocalTime;

public class Game {
    private final String gameId;
    private final LocalTime startTime;
    private final LocalDate date;
    private final Teams homeTeam;
    private final Teams awayTeam;

    public Game(String gameId, LocalTime startTime, LocalDate date, Teams homeTeam, Teams awayTeam) {
        this.gameId = gameId;
        this.startTime = startTime;
        this.date = date;
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
    }

    public String getGameId() {
        return gameId;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalDate getDate() {
        return date;
    }

    public Teams getHomeTeam() {
        return homeTeam;
    }

    public Teams getAwayTeam() {
        return awayTeam;
    }
}
