package com.finkfast.fantasy.jarvis.basketball.adaptor;

import com.finkfast.fantasy.jarvis.basketball.config.GoogleSheetsConfig;
import com.finkfast.fantasy.jarvis.basketball.data.BoxScoreEntry;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.*;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GoogleSheetsAdaptor {

    final Sheets sheetsClient;
    //TODO read from config
    final String spreadsheetId = "1pLZk8ul5aa7h1SZaxT65QQKKRX9jwGDvEWbF6AZQ5jc";
    final Integer boxScoreSheetId = 1923047521;
    final Integer ownershipSheetId = 60364491;
    final Integer leagueSize = 8;
    final Integer scoringPeriods = 146;

    private final static String BOX_SCORE_RANGE = "Box Scores!C:H";
    private final static String CLAIM = "Claim";
    private final static String CLAIM_RANGE = "Transaction History!J2:P";
    private final static String DROP = "Drop";
    private final static LocalDate GOOGLE_SHEETS_DAY_ZERO = LocalDate.of(1899, 12, 30);
    private final static double MINUTES_IN_A_DAY = 1440;
    private final static LocalTime MINUTE_ZERO = LocalTime.of(0, 0);
    private final static String OWNERSHIP_RANGE = "Ownership!L:AG";
    private final static LocalDate SEASON_DAY_ZERO = LocalDate.of(2020, 12, 21);
    private final static String TRADE_RANGE = "Transaction History!R2:X";

    public GoogleSheetsAdaptor() throws IOException, GeneralSecurityException {
        sheetsClient = GoogleSheetsConfig.sheetsClient();
    }

    public void writeBoxScores(List<BoxScoreEntry> boxScoreEntryList) throws IOException, InterruptedException {
        List<Request> requests = new ArrayList<>();
        int rowsAppended = 0;
        ValueRange boxScoreValues = sheetsClient.spreadsheets().values()
                .get(spreadsheetId, BOX_SCORE_RANGE)
                .execute();

        int maxRow = boxScoreValues.getValues().size();

        for(BoxScoreEntry boxScoreEntry : boxScoreEntryList) {
            int row = getBoxScoreRow(boxScoreEntry.getPlayerId(), boxScoreEntry.getGameId(), rowsAppended, boxScoreValues);
            if(row >= maxRow) {
                rowsAppended++;
            }
            requests.add(new Request()
                    .setUpdateCells(new UpdateCellsRequest()
                            .setStart(new GridCoordinate()
                                    .setSheetId(boxScoreSheetId)
                                    .setRowIndex(row)
                                    .setColumnIndex(1))
                            .setRows(Collections.singletonList(
                                    new RowData().setValues(buildBoxScoreRow(boxScoreEntry))))
                            .setFields("userEnteredValue,userEnteredFormat.backgroundColor")));

        }

        BatchUpdateSpreadsheetRequest batchUpdateRequest = new BatchUpdateSpreadsheetRequest()
                .setRequests(requests);
        sheetsClient.spreadsheets().batchUpdate(spreadsheetId, batchUpdateRequest)
                .execute();
    }

    public void writeOwnershipMatrix() throws IOException {
        List<Request> requests = new ArrayList<>();
        ValueRange ownershipValues = sheetsClient.spreadsheets().values()
                .get(spreadsheetId, OWNERSHIP_RANGE)
                .execute();

        ValueRange claimValues = sheetsClient.spreadsheets().values()
                .get(spreadsheetId, CLAIM_RANGE)
                .execute();

        ValueRange tradeValues = sheetsClient.spreadsheets().values()
                .get(spreadsheetId, TRADE_RANGE)
                .execute();

        int row = getOwnershipRow(ownershipValues, ownershipValues.getValues().size());
        if(row < 0) {
            return;
        }

        for(int i = 0; i < leagueSize; i++) {
            List<String> currentTeam = getCurrentTeam(ownershipValues, i, row - 1);
            List<List<String>> transactionLists = new ArrayList<>();
            transactionLists.add(currentTeam);
            List<String> danglingClaims = new ArrayList<>();
            transactionLists.add(danglingClaims);
            String teamName = String.valueOf(ownershipValues.getValues().get(row * leagueSize + i + 1).get(1));

            for(int j = row; j <= ChronoUnit.DAYS.between(SEASON_DAY_ZERO, LocalDate.now()) && j <= scoringPeriods; j++) {
                transactionLists = executeClaims(transactionLists, claimValues, j, teamName);
                currentTeam = executeTrades(transactionLists, tradeValues, j, teamName);

                requests.add(new Request()
                        .setUpdateCells(new UpdateCellsRequest()
                                .setStart(new GridCoordinate()
                                        .setSheetId(ownershipSheetId)
                                        .setRowIndex((j) * leagueSize + i + 1)
                                        .setColumnIndex(14))
                                .setRows(Collections.singletonList(
                                        new RowData().setValues(buildOwnershipRow(currentTeam))))
                                .setFields("userEnteredValue,userEnteredFormat.backgroundColor")));
            }
        }

        BatchUpdateSpreadsheetRequest batchUpdateRequest = new BatchUpdateSpreadsheetRequest()
                .setRequests(requests);
        sheetsClient.spreadsheets().batchUpdate(spreadsheetId, batchUpdateRequest)
                .execute();
    }

    private Integer getBoxScoreRow(String playerId, String gameId, int rowsAppended, ValueRange boxScoreValues) {
        for(int i = 0; i < boxScoreValues.getValues().size(); i++) {
            if(playerId.equals(boxScoreValues.getValues().get(i).get(5)) && gameId.equals(boxScoreValues.getValues().get(i).get(0))) {
                return i;
            }
        }
        return boxScoreValues.getValues().size() + rowsAppended;
    }

    private List<CellData> buildBoxScoreRow(BoxScoreEntry boxScoreEntry) {
        List<CellData> values = new ArrayList<>();
        values.add(new CellData()
                .setUserEnteredValue(new ExtendedValue()
                        .setStringValue(boxScoreEntry.getPlayer())));
        values.add(new CellData()
                .setUserEnteredValue(new ExtendedValue()
                        .setStringValue(boxScoreEntry.getGameId())));
        values.add(new CellData()
                .setUserEnteredValue(new ExtendedValue()
                        .setNumberValue((double) ChronoUnit.DAYS.between(GOOGLE_SHEETS_DAY_ZERO, boxScoreEntry.getDate())))
                .setUserEnteredFormat(new CellFormat().setNumberFormat(new NumberFormat().setType("DATE"))));
        values.add(new CellData()
                .setUserEnteredValue(new ExtendedValue()
                        .setNumberValue(ChronoUnit.MINUTES.between(MINUTE_ZERO, boxScoreEntry.getTime())/MINUTES_IN_A_DAY))
                .setUserEnteredFormat(new CellFormat().setNumberFormat(new NumberFormat().setType("TIME"))));
        values.add(new CellData()
                .setUserEnteredValue(new ExtendedValue()
                        .setStringValue(boxScoreEntry.getTeam().name())));
        values.add(new CellData()
                .setUserEnteredValue(new ExtendedValue()
                        .setStringValue(boxScoreEntry.getOpponent().name())));
        values.add(new CellData()
                .setUserEnteredValue(new ExtendedValue()
                        .setStringValue(boxScoreEntry.getPlayerId())));
        values.add(new CellData()
                .setUserEnteredValue(new ExtendedValue()
                        .setNumberValue(boxScoreEntry.getStartedGame() ? 1.0 : 0.0)));
        values.add(new CellData()
                .setUserEnteredValue(new ExtendedValue()
                        .setNumberValue(boxScoreEntry.getMinutesPlayed())));
        values.add(new CellData()
                .setUserEnteredValue(new ExtendedValue()
                        .setNumberValue(Double.valueOf(boxScoreEntry.getFieldGoalsMade()))));
        values.add(new CellData()
                .setUserEnteredValue(new ExtendedValue()
                        .setNumberValue(Double.valueOf(boxScoreEntry.getFieldGoalsAttempted()))));
        values.add(new CellData()
                .setUserEnteredValue(new ExtendedValue()
                        .setNumberValue(Double.valueOf(boxScoreEntry.getFreeThrowsMade()))));
        values.add(new CellData()
                .setUserEnteredValue(new ExtendedValue()
                        .setNumberValue(Double.valueOf(boxScoreEntry.getFreeThrowsAttempted()))));
        values.add(new CellData()
                .setUserEnteredValue(new ExtendedValue()
                        .setNumberValue(Double.valueOf(boxScoreEntry.getThreePointersMade()))));
        values.add(new CellData()
                .setUserEnteredValue(new ExtendedValue()
                        .setNumberValue(Double.valueOf(boxScoreEntry.getThreePointersAttempted()))));
        values.add(new CellData()
                .setUserEnteredValue(new ExtendedValue()
                        .setNumberValue(Double.valueOf(boxScoreEntry.getPoints()))));
        values.add(new CellData()
                .setUserEnteredValue(new ExtendedValue()
                        .setNumberValue(Double.valueOf(boxScoreEntry.getRebounds()))));
        values.add(new CellData()
                .setUserEnteredValue(new ExtendedValue()
                        .setNumberValue(Double.valueOf(boxScoreEntry.getAssists()))));
        values.add(new CellData()
                .setUserEnteredValue(new ExtendedValue()
                        .setNumberValue(Double.valueOf(boxScoreEntry.getSteals()))));
        values.add(new CellData()
                .setUserEnteredValue(new ExtendedValue()
                        .setNumberValue(Double.valueOf(boxScoreEntry.getBlocks()))));
        values.add(new CellData()
                .setUserEnteredValue(new ExtendedValue()
                        .setNumberValue(Double.valueOf(boxScoreEntry.getTurnovers()))));
        values.add(new CellData()
                .setUserEnteredValue(new ExtendedValue()
                        .setNumberValue(Double.valueOf(boxScoreEntry.getTechnicalFouls()))));
        values.add(new CellData()
                .setUserEnteredValue(new ExtendedValue()
                        .setNumberValue(Double.valueOf(boxScoreEntry.getPlusMinus()))));
        return values;
    }

    private Integer getOwnershipRow(ValueRange ownershipValues, int size) {
        for(int i = 2; i < size/leagueSize; i++) {
            if(ownershipValues.getValues().get(i * leagueSize + 1).size() < 4) {
                return i - 1;
            }
        }
        return -1;
    }

    private List<String> getCurrentTeam(ValueRange ownershipValues, int team, int scoringPeriod) {
        List<String> currentTeam = new ArrayList<>();
        for(int i = 3; i < ownershipValues.getValues().get(scoringPeriod * leagueSize + team + 1).size(); i++) {
            currentTeam.add(String.valueOf(ownershipValues.getValues().get(scoringPeriod * leagueSize + team + 1).get(i)));
        }
        return currentTeam;
    }

    private List<List<String>> executeClaims(List<List<String>> transactionLists, ValueRange claimValues, int scoringPeriod, String teamName) {
        List<String> currentTeam = transactionLists.get(0);
        List<String> danglingClaims = transactionLists.get(1);

        for(int i = claimValues.getValues().size() -1; i >= 0 && Integer.parseInt((String) claimValues.getValues().get(i).get(6)) <= scoringPeriod; i--) {
            if(Integer.parseInt((String) claimValues.getValues().get(i).get(6)) == scoringPeriod && String.valueOf(claimValues.getValues().get(i).get(4)).equals(teamName)) {
                if(String.valueOf(claimValues.getValues().get(i).get(3)).equals(CLAIM)) {
                    currentTeam.add(String.valueOf(claimValues.getValues().get(i).get(0)));
                }
                else if(String.valueOf(claimValues.getValues().get(i).get(3)).equals(DROP)) {
                    if(currentTeam.contains(String.valueOf(claimValues.getValues().get(i).get(0)))) {
                        currentTeam.remove(String.valueOf(claimValues.getValues().get(i).get(0)));
                    }
                    else {
                        danglingClaims.add(String.valueOf(claimValues.getValues().get(i).get(0)));
                    }
                }
            }
        }
        return transactionLists;
    }

    private List<String> executeTrades(List<List<String>> transactionLists, ValueRange tradeValues, int scoringPeriod, String teamName) {
        List<String> currentTeam = transactionLists.get(0);
        List<String> danglingClaims = transactionLists.get(1);

        for(int i = tradeValues.getValues().size() -1; i >= 0 && Integer.parseInt((String)  tradeValues.getValues().get(i).get(6)) <= scoringPeriod;  i--) {
            if(danglingClaims.contains(String.valueOf(tradeValues.getValues().get(i).get(0)))) {
                danglingClaims.remove(String.valueOf(tradeValues.getValues().get(i).get(0)));
            }
            else if(Integer.parseInt((String) tradeValues.getValues().get(i).get(6)) == scoringPeriod && String.valueOf(tradeValues.getValues().get(i).get(4)).equals(teamName)) {
                currentTeam.add(String.valueOf(tradeValues.getValues().get(i).get(0)));
            }
            else if(Integer.parseInt((String)  tradeValues.getValues().get(i).get(6)) == scoringPeriod && String.valueOf(tradeValues.getValues().get(i).get(3)).equals(teamName)) {
                currentTeam.remove(String.valueOf(tradeValues.getValues().get(i).get(0)));
            }
        }
        return currentTeam;
    }

    private List<CellData> buildOwnershipRow(List<String> currentTeam) {
        List<CellData> values = new ArrayList<>();
        for(String player : currentTeam) {
            values.add(new CellData()
                    .setUserEnteredValue(new ExtendedValue()
                            .setStringValue(player)));
        }
        return values;
    }
}
