package com.bjss.nhsd.a2si.casedataapi.services;

import com.bjss.nhsd.a2si.casedataapi.domain.DispoGroupByTimePeriod;
import com.bjss.nhsd.a2si.idt.casedata.IdtCaseCallDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

@Service
public class IdtCaseDataService {

    @Autowired
    private DataSource dataSource;

    private SimpleJdbcCall generateDistinctDispositionBroadGroupsStoredProcedureJdbCall;

    private SimpleJdbcCall generateTimePeriodsStoredProcedureJdbcCall;

    @PostConstruct
    public void init() {
        JdbcTemplate template = new JdbcTemplate(dataSource);

        generateDistinctDispositionBroadGroupsStoredProcedureJdbCall = new SimpleJdbcCall(template)
                .withProcedureName("generate_disposition_broad_groups")
                .declareParameters(
                        new SqlParameter("fromDate", Types.VARCHAR),
                        new SqlParameter("toDate", Types.VARCHAR));

        generateTimePeriodsStoredProcedureJdbcCall = new SimpleJdbcCall(template)
                .withProcedureName("generate_series_date_minute")
                .declareParameters(
                        new SqlParameter("n_first", Types.VARCHAR),
                        new SqlParameter("n_last", Types.VARCHAR),
                        new SqlParameter("n_increment", Types.INTEGER));
    }

    public void createIdtCaseCallDetails(List<IdtCaseCallDetails> idtCaseCallDetailsList) {

        String insert = "INSERT INTO idt_case_data " +
                "(callSite, locationCCG, callStartDateTime, callEndDateTime, thirdPartyCaller, " +
                "gender, ageGroup, dispositionGroup, dispositionBroadGroup) " +
                "VALUES " +
                "(?, ?, ?, ?, ?, ?, ?, ?, ?)";


        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        jdbcTemplate.batchUpdate(insert, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement preparedStatement, int i) throws SQLException {

                IdtCaseCallDetails idtCaseCallDetails = idtCaseCallDetailsList.get(i);

                preparedStatement.setString(1, idtCaseCallDetails.getCallSite());
                preparedStatement.setString(2, idtCaseCallDetails.getLocationCCG());
                preparedStatement.setString(3, idtCaseCallDetails.getCallStartDateTime());
                preparedStatement.setString(4, idtCaseCallDetails.getCallEndDateTime());
                preparedStatement.setBoolean(5, idtCaseCallDetails.isThirdPartyCaller());
                preparedStatement.setString(6, idtCaseCallDetails.getGenderEnumeration().toString());
                preparedStatement.setString(7, idtCaseCallDetails.getAgeGroupEnumeration().toString());
                preparedStatement.setString(8, idtCaseCallDetails.getDispositionGroup());
                preparedStatement.setString(9, idtCaseCallDetails.getDispositionBroadGroup());
            }

            @Override
            public int getBatchSize() {
                return idtCaseCallDetailsList.size();
            }
        });

    }

    public List<DispoGroupByTimePeriod> getForAllLocationCCGs(String from, String to) {

        // Call a Stored Procedure that creates a temporary table ( disposition_broad_groups ) containing
        // all the distinct broad groups that occurred with the time frame
        generateDistinctDispositionBroadGroups(from, to);

        // Generate time periods (15 minutes) for all times within the from and to dates.
        // NOTE that start date supplied as an input parameter MUST be correct, e.g. yyyy-mm-dd 03:15:00, 04:45:00 etc.
        generateTimePeriods(from, to);

        String query =
                "SELECT\n" +
                "  dispoBroadGroup,\n" +
                "  fromTime,\n" +
                "  toTime,\n" +
                "  SUM(total) as total\n" +
                "FROM (\n" +
                "       # First Query gets ALL disposition groups and time periods regardless of whether data exists\n" +
                "       SELECT\n" +
                "         disposition_broad_groups.dispositionBroadGroup  AS dispoBroadGroup,\n" +
                "         series_tmp.series                               AS FromTime,\n" +
                "         ADDDATE(series_tmp.series, INTERVAL 899 SECOND) AS ToTime,\n" +
                "         0                                               AS Total\n" +
                "       FROM disposition_broad_groups, series_tmp\n" +
                "\n" +
                "       UNION ALL\n" +
                "       # Second Query gets all disposition groups and time periods that have data\n" +
                "       SELECT\n" +
                "         dispositionBroadGroup           AS dispoBroadGroup,\n" +
                "         CONCAT(DATE_FORMAT(callEndDateTime, '%Y-%m-%d'), ' ',\n" +
                "                DATE_FORMAT(SEC_TO_TIME(TIME_TO_SEC(callEndDateTime) - (TIME_TO_SEC(callEndDateTime) MOD 900)),\n" +
                "                            '%H:%i:%S')) AS FromTime,\n" +
                "         CONCAT(DATE_FORMAT(callEndDateTime, '%Y-%m-%d'), ' ',\n" +
                "                DATE_FORMAT(SEC_TO_TIME(TIME_TO_SEC(callEndDateTime) + 899 - (TIME_TO_SEC(callEndDateTime) MOD 900)),\n" +
                "                            '%H:%i:%S')) AS ToTime,\n" +
                "         count(*)                        AS total\n" +
                "       FROM idt_case_data\n" +
                "       WHERE dispositionBroadGroup IS NOT NULL AND dispositionBroadGroup != '' AND\n" +
                "             callEndDateTime BETWEEN ? AND ?\n" +
                "       GROUP BY dispoBroadGroup, FromTime, ToTime\n" +
                "       ORDER BY dispoBroadGroup, FromTime, ToTime\n" +
                "     ) finalTable\n" +
                "GROUP BY dispoBroadGroup, FromTime, ToTime\n" +
                "ORDER BY dispoBroadGroup, FromTime, ToTime;";

        List<DispoGroupByTimePeriod> dispoGroupByTimePeriodList =
                new JdbcTemplate(dataSource).query(query, new DispoGroupByTimePeriodRowMapper(), from, to);

//        for (DispoGroupByTimePeriod dispoGroupByTimePeriod : dispoGroupByTimePeriodList) {
//            System.out.println(dispoGroupByTimePeriod);
//        }

        return dispoGroupByTimePeriodList;

    }

    public List<DispoGroupByTimePeriod> getForSpecificLocationCCG(String from, String to, String locationCCG) {


        generateDistinctDispositionBroadGroups(from, to);

        generateTimePeriods(from, to);


        String query =
                "SELECT\n" +
                        "  dispoBroadGroup,\n" +
                        "  fromTime,\n" +
                        "  toTime,\n" +
                        "  SUM(total) as total\n" +
                        "FROM (\n" +
                        "       # First Query gets ALL disposition groups and time periods regardless of whether data exists\n" +
                        "       SELECT\n" +
                        "         disposition_broad_groups.dispositionBroadGroup  AS dispoBroadGroup,\n" +
                        "         series_tmp.series                               AS FromTime,\n" +
                        "         ADDDATE(series_tmp.series, INTERVAL 899 SECOND) AS ToTime,\n" +
                        "         0                                               AS Total\n" +
                        "       FROM disposition_broad_groups, series_tmp\n" +
                        "\n" +
                        "       UNION ALL\n" +
                        "       # Second Query gets all disposition groups and time periods that have data\n" +
                        "       SELECT\n" +
                        "         dispositionBroadGroup           AS dispoBroadGroup,\n" +
                        "         CONCAT(DATE_FORMAT(callEndDateTime, '%Y-%m-%d'), ' ',\n" +
                        "                DATE_FORMAT(SEC_TO_TIME(TIME_TO_SEC(callEndDateTime) - (TIME_TO_SEC(callEndDateTime) MOD 900)),\n" +
                        "                            '%H:%i:%S')) AS FromTime,\n" +
                        "         CONCAT(DATE_FORMAT(callEndDateTime, '%Y-%m-%d'), ' ',\n" +
                        "                DATE_FORMAT(SEC_TO_TIME(TIME_TO_SEC(callEndDateTime) + 899 - (TIME_TO_SEC(callEndDateTime) MOD 900)),\n" +
                        "                            '%H:%i:%S')) AS ToTime,\n" +
                        "         count(*)                        AS total\n" +
                        "       FROM idt_case_data\n" +
                        "       WHERE locationCCG = ? AND\n" +
                        "             dispositionBroadGroup IS NOT NULL AND dispositionBroadGroup != '' AND\n" +
                        "             callEndDateTime BETWEEN ? AND ?\n" +
                        "       GROUP BY dispoBroadGroup, FromTime, ToTime\n" +
                        "       ORDER BY dispoBroadGroup, FromTime, ToTime\n" +
                        "     ) finalTable\n" +
                        "GROUP BY dispoBroadGroup, FromTime, ToTime\n" +
                        "ORDER BY dispoBroadGroup, FromTime, ToTime;";

        List<DispoGroupByTimePeriod> dispoGroupByTimePeriodList =
                new JdbcTemplate(dataSource).query(query, new DispoGroupByTimePeriodRowMapper(), locationCCG, from, to);

//        for (DispoGroupByTimePeriod dispoGroupByTimePeriod : dispoGroupByTimePeriodList) {
//            System.out.println(dispoGroupByTimePeriod);
//        }

        return dispoGroupByTimePeriodList;

    }

    private void generateDistinctDispositionBroadGroups(String fromDate, String toDate) {

        MapSqlParameterSource paramMap = new MapSqlParameterSource()
                .addValue("fromDate", fromDate)
                .addValue("toDate", toDate);
        generateDistinctDispositionBroadGroupsStoredProcedureJdbCall.execute(paramMap);

    }

    private void generateTimePeriods(String fromDate, String toDate) {

        MapSqlParameterSource paramMap = new MapSqlParameterSource()
                .addValue("n_first", fromDate)
                .addValue("n_last", toDate)
                .addValue("n_increment", 15);
        generateTimePeriodsStoredProcedureJdbcCall.execute(paramMap);

    }
}
