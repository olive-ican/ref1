package com.example.legacy.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

@Component
public class QueryExplainRunner implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(QueryExplainRunner.class);

    @Autowired
    private DataSource dataSource;

    @Override
    public void run(String... args) throws Exception {
        logger.info("===================== EXPLAIN PLAN =====================");
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {

            String tunedQuery = "EXPLAIN SELECT p.*, pr.* " +
                                "FROM TB_PROMOTION p " +
                                "INNER JOIN TB_PRODUCT pr ON p.PROD_CD = pr.PROD_CD " +
                                "WHERE pr.USE_YN = 'Y' AND CURRENT_DATE BETWEEN p.START_DT AND p.END_DT";

            ResultSet rs = statement.executeQuery(tunedQuery);
            StringBuilder sb = new StringBuilder("Explain plan for tuned query:\n");
            while (rs.next()) {
                sb.append(rs.getString(1)).append("\n");
            }
            logger.info(sb.toString());
        } catch (Exception e) {
            logger.error("Error while explaining query", e);
        }
        logger.info("========================================================");
    }
}
