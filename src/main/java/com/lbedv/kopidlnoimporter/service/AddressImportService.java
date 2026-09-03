package com.lbedv.kopidlnoimporter.service;

import com.lbedv.kopidlnoimporter.dto.CastObceDto;
import com.lbedv.kopidlnoimporter.dto.ObecDto;
import com.lbedv.kopidlnoimporter.dto.ParseResult;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

@Service
public class AddressImportService {

    private final JdbcTemplate jdbcTemplate;

    public AddressImportService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public void importData(ParseResult parseResult) {
        jdbcTemplate.execute("DELETE FROM cast_obce");
        jdbcTemplate.execute("DELETE FROM obec");

        if (parseResult.obce() != null && !parseResult.obce().isEmpty()) {
            insertObce(parseResult.obce());
        }
        if (parseResult.castiObce() != null && !parseResult.castiObce().isEmpty()) {
            insertCastiObce(parseResult.castiObce());
        }
    }

    private void insertObce(List<ObecDto> obce) {
        String sql = "INSERT INTO obec (kod, nazev) VALUES (?, ?)";

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ObecDto dto = obce.get(i);
                ps.setLong(1, dto.kod());
                ps.setString(2, dto.nazev());
            }

            @Override
            public int getBatchSize() {
                return obce.size();
            }
        });
    }

    private void insertCastiObce(List<CastObceDto> castiObce) {
        String sql = "INSERT INTO cast_obce (kod, nazev, obec_kod) VALUES (?, ?, ?)";

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                CastObceDto dto = castiObce.get(i);
                ps.setLong(1, dto.kod());
                ps.setString(2, dto.nazev());
                ps.setLong(3, dto.obecKod());
            }

            @Override
            public int getBatchSize() {
                return castiObce.size();
            }
        });
    }
}
