package com.bjss.nhsd.a2si.casedataapi.services;

import com.bjss.nhsd.a2si.casedataapi.domain.DispoGroupByTimePeriod;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class DispoGroupByTimePeriodRowMapper implements RowMapper {
    @Override
    public DispoGroupByTimePeriod mapRow(ResultSet resultSet, int i) throws SQLException {
        return new DispoGroupByTimePeriod(
                resultSet.getString("dispoBroadGroup"),
                resultSet.getString("fromTime"),
                resultSet.getString("toTime"),
                resultSet.getInt("total")
        );
    }
}
