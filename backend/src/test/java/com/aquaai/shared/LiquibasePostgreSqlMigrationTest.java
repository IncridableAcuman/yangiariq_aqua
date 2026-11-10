package com.aquaai.shared;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.assertj.core.api.Assertions.assertThat;

class LiquibasePostgreSqlMigrationTest {

    private static final String PG_HOST = "localhost";
    private static final int PG_PORT = 5433;
    private static final String PG_URL = "jdbc:postgresql://" + PG_HOST + ":" + PG_PORT + "/aquaai_fresh_test";
    private static final String PG_USER = "aqua_user";
    private static final String PG_PASS = "aqua_secure_password_dev";

    private boolean isPostgreSqlReachable() {
        try (Socket socket = new Socket(PG_HOST, PG_PORT)) {
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Test
    @DisplayName("Verify fresh database initialization of complete Liquibase changelog against real PostgreSQL 16")
    void testFreshDatabaseInitializationOnPostgreSql() throws Exception {
        Assumptions.assumeTrue(isPostgreSqlReachable(), "PostgreSQL instance on port 5433 is not reachable from this environment.");

        try (Connection connection = DriverManager.getConnection(PG_URL, PG_USER, PG_PASS)) {
            Database database = DatabaseFactory.getInstance()
                    .findCorrectDatabaseImplementation(new JdbcConnection(connection));

            Liquibase liquibase = new Liquibase(
                    "db/changelog/db.changelog-master.xml",
                    new ClassLoaderResourceAccessor(),
                    database
            );

            // Execute full migration chain from scratch
            liquibase.update("");

            // Verify migration integrity
            try (Statement stmt = connection.createStatement()) {
                // 1. Verify changesets executed
                ResultSet rsChangelog = stmt.executeQuery("SELECT count(*) FROM databasechangelog;");
                rsChangelog.next();
                int changelogCount = rsChangelog.getInt(1);
                assertThat(changelogCount).isEqualTo(51);

                // 2. Verify roles seeded
                ResultSet rsRoles = stmt.executeQuery("SELECT count(*) FROM roles;");
                rsRoles.next();
                assertThat(rsRoles.getInt(1)).isEqualTo(4);

                // 3. Verify permissions seeded
                ResultSet rsPerms = stmt.executeQuery("SELECT count(*) FROM permissions;");
                rsPerms.next();
                assertThat(rsPerms.getInt(1)).isEqualTo(32);

                // 4. Verify role_permissions mappings seeded
                ResultSet rsRolePerms = stmt.executeQuery("SELECT count(*) FROM role_permissions;");
                rsRolePerms.next();
                assertThat(rsRolePerms.getInt(1)).isEqualTo(86);

                // 5. Verify pilot tables created (Changeset 027)
                ResultSet rsPilot = stmt.executeQuery("SELECT count(*) FROM information_schema.tables WHERE table_schema='public' AND table_name LIKE 'pilot_%';");
                rsPilot.next();
                assertThat(rsPilot.getInt(1)).isGreaterThanOrEqualTo(4);

                // 6. Verify security_audit_logs created (Changeset 028)
                ResultSet rsAudit = stmt.executeQuery("SELECT count(*) FROM information_schema.tables WHERE table_schema='public' AND table_name = 'security_audit_logs';");
                rsAudit.next();
                assertThat(rsAudit.getInt(1)).isEqualTo(1);

                // 7. Verify total public tables created
                ResultSet rsTotalTables = stmt.executeQuery("SELECT count(*) FROM information_schema.tables WHERE table_schema='public';");
                rsTotalTables.next();
                assertThat(rsTotalTables.getInt(1)).isEqualTo(67);
            }
        }
    }
}
