package com.finkfast.fantasy.jarvis.basketball.data;

import java.time.LocalDate;
import java.time.LocalTime;

public class Game {
    private final String gameId;
    private final LocalTime startTime;
    private final LocalDate date;
    private final Teams homeTeam;
    private final Teams awayTeam;
    private final Integer periods;

    public Game(String gameId, LocalTime startTime, LocalDate date, Teams homeTeam, Teams awayTeam, Integer periods) {
        this.gameId = gameId;
        this.startTime = startTime;
        this.date = date;
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.periods = periods;
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

    public Integer getPeriods() {
        return periods;
    }
}
