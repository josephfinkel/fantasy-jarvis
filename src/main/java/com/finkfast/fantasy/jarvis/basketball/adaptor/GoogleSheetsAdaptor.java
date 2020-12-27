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

    private final static LocalDate GOOGLE_SHEETS_DAY_ZERO = LocalDate.of(1899, 12, 30);
    private final static LocalTime MINUTE_ZERO = LocalTime.of(0, 0);
    private final static double MINUTES_IN_A_DAY = 1440;
    private final static String RANGE = "Box Scores!B:G";

    public GoogleSheetsAdaptor() throws IOException, GeneralSecurityException {
        sheetsClient = GoogleSheetsConfig.sheetsClient();
    }

    public void writeBoxScores(List<BoxScoreEntry> boxScoreEntryList) throws IOException, InterruptedException {
        List<Request> requests = new ArrayList<>();
        int rowsAppended = 0;
        ValueRange existingValues = sheetsClient.spreadsheets().values()
                .get(spreadsheetId, RANGE)
                .execute();

        int maxRow = existingValues.getValues().size();

        for(BoxScoreEntry boxScoreEntry : boxScoreEntryList) {
            int row = getRow(boxScoreEntry.getPlayerId(), boxScoreEntry.getGameId(), rowsAppended, existingValues);
            if(row >= maxRow) {
                rowsAppended++;
            }
            requests.add(new Request()
                    .setUpdateCells(new UpdateCellsRequest()
                            .setStart(new GridCoordinate()
                                    .setSheetId(boxScoreSheetId)
                                    .setRowIndex(row)
                                    .setColumnIndex(0))
                            .setRows(Collections.singletonList(
                                    new RowData().setValues(buildBoxScoreRow(boxScoreEntry))))
                            .setFields("userEnteredValue,userEnteredFormat.backgroundColor")));

        }

        BatchUpdateSpreadsheetRequest batchUpdateRequest = new BatchUpdateSpreadsheetRequest()
                .setRequests(requests);
        sheetsClient.spreadsheets().batchUpdate(spreadsheetId, batchUpdateRequest)
                .execute();
    }

    private Integer getRow(String playerId, String gameId, int rowsAppended, ValueRange existingValues) {
        for(int i = 0; i < existingValues.getValues().size(); i++) {
            if(playerId.equals(existingValues.getValues().get(i).get(5)) && gameId.equals(existingValues.getValues().get(i).get(0))) {
                return i;
            }
        }
        return existingValues.getValues().size() + rowsAppended;
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
}