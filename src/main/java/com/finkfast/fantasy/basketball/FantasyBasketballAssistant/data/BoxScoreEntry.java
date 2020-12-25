package com.finkfast.fantasy.basketball.FantasyBasketballAssistant.data;

import java.time.LocalDate;
import java.time.LocalTime;

public class BoxScoreEntry {
    private final String player;
    private final LocalDate date;
    private final LocalTime time;
    private final Teams team;
    private final Teams opponent;
    private final Boolean startedGame;
    private final Double minutesPlayed;
    private final Integer fieldGoalsMade;
    private final Integer fieldGoalsAttempted;
    private final Integer freeThrowsMade;
    private final Integer freeThrowsAttempted;
    private final Integer threePointersMade;
    private final Integer threePointersAttempted;
    private final Integer points;
    private final Integer rebounds;
    private final Integer assists;
    private final Integer steals;
    private final Integer blocks;
    private final Integer turnovers;
    private final Integer technicalFouls;
    private final Integer plusMinus;

    public BoxScoreEntry(String player, LocalDate date, LocalTime time, Teams team, Teams opponent, Boolean startedGame, Double minutesPlayed, Integer fieldGoalsMade,
                         Integer fieldGoalsAttempted, Integer freeThrowsMade, Integer freeThrowsAttempted, Integer threePointersMade, Integer threePointersAttempted,
                         Integer points, Integer rebounds, Integer assists, Integer steals, Integer blocks, Integer turnovers, Integer technicalFouls, Integer plusMinus) {
        this.player = player;
        this.date = date;
        this.time = time;
        this.team = team;
        this.opponent = opponent;
        this.startedGame = startedGame;
        this.minutesPlayed = minutesPlayed;
        this.fieldGoalsMade = fieldGoalsMade;
        this.fieldGoalsAttempted = fieldGoalsAttempted;
        this.freeThrowsMade = freeThrowsMade;
        this.freeThrowsAttempted = freeThrowsAttempted;
        this.threePointersMade = threePointersMade;
        this.threePointersAttempted = threePointersAttempted;
        this.points = points;
        this.rebounds = rebounds;
        this.assists = assists;
        this.steals = steals;
        this.blocks = blocks;
        this.turnovers = turnovers;
        this.technicalFouls = technicalFouls;
        this.plusMinus = plusMinus;
    }

    public String getPlayer() {
        return player;
    }

    public LocalDate getDate() {
        return date;
    }

    public LocalTime getTime() {
        return time;
    }

    public Teams getTeam() {
        return team;
    }

    public Teams getOpponent() {
        return opponent;
    }

    public Boolean getStartedGame() {
        return startedGame;
    }

    public Double getMinutesPlayed() {
        return minutesPlayed;
    }

    public Integer getFieldGoalsMade() {
        return fieldGoalsMade;
    }

    public Integer getFieldGoalsAttempted() {
        return fieldGoalsAttempted;
    }

    public Integer getFreeThrowsMade() {
        return freeThrowsMade;
    }

    public Integer getFreeThrowsAttempted() {
        return freeThrowsAttempted;
    }

    public Integer getThreePointersMade() {
        return threePointersMade;
    }

    public Integer getThreePointersAttempted() {
        return threePointersAttempted;
    }

    public Integer getPoints() {
        return points;
    }

    public Integer getRebounds() {
        return rebounds;
    }

    public Integer getAssists() {
        return assists;
    }

    public Integer getSteals() {
        return steals;
    }

    public Integer getBlocks() {
        return blocks;
    }

    public Integer getTurnovers() {
        return turnovers;
    }

    public Integer getTechnicalFouls() {
        return technicalFouls;
    }

    public Integer getPlusMinus() {
        return plusMinus;
    }
}
