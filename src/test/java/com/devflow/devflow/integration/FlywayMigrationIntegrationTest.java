package com.devflow.devflow.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class FlywayMigrationIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void testFlywayMigrationsAppliedCleanlyAgainstDatabase() {
        List<String> tables = jdbcTemplate.queryForList(
                "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE LOWER(TABLE_SCHEMA) = 'public' OR LOWER(TABLE_SCHEMA) = 'devflow_test_db'",
                String.class).stream().map(String::toLowerCase).toList();

        assertTrue(tables.contains("users"), "Table 'users' should exist");

        assertTrue(tables.contains("organizations"), "Table 'organizations' should exist");
        assertTrue(tables.contains("organization_members"), "Table 'organization_members' should exist");
        assertTrue(tables.contains("projects"), "Table 'projects' should exist");
        assertTrue(tables.contains("project_members"), "Table 'project_members' should exist");
        assertTrue(tables.contains("tasks"), "Table 'tasks' should exist");
        assertTrue(tables.contains("comments"), "Table 'comments' should exist");
        assertTrue(tables.contains("comment_mentions"), "Table 'comment_mentions' should exist");
        assertTrue(tables.contains("attachments"), "Table 'attachments' should exist");
        assertTrue(tables.contains("notifications"), "Table 'notifications' should exist");
        assertTrue(tables.contains("activity_logs"), "Table 'activity_logs' should exist");
        assertTrue(tables.contains("flyway_schema_history"), "Flyway schema history table should exist");

        Integer appliedMigrations = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM flyway_schema_history WHERE success = 1", Integer.class);
        assertTrue(appliedMigrations != null && appliedMigrations >= 12, "At least 12 migrations should be successfully applied");
    }
}
