package shelfwise.db;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;

class SchemaInstallerTest {

    @Test
    void splitsStatementsAndDropsComments() {
        String script = """
                -- a comment
                CREATE TABLE a (id INT);
                   -- indented comment
                CREATE TABLE b (
                    id INT
                );

                """;
        assertEquals(List.of("CREATE TABLE a (id INT)", "CREATE TABLE b (\n    id INT\n)"),
                SchemaInstaller.splitStatements(script));
    }

    @Test
    void handlesWindowsLineBreaks() {
        assertEquals(List.of("SELECT 1", "SELECT 2"),
                SchemaInstaller.splitStatements("-- c\r\nSELECT 1;\r\nSELECT 2;\r\n"));
    }

    @Test
    void bundledSchemaCreatesTheFourTablesSafely() throws IOException {
        String script;
        try (InputStream in = SchemaInstaller.class.getResourceAsStream(SchemaInstaller.SCHEMA_RESOURCE)) {
            assertNotNull(in, "schema.sql must be packaged with the app");
            script = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
        List<String> statements = SchemaInstaller.splitStatements(script);
        assertEquals(4, statements.size());
        // Running it on every start is only safe if nothing is dropped or recreated
        for (String sql : statements) {
            assertTrue(sql.startsWith("CREATE TABLE IF NOT EXISTS"), sql);
        }
    }
}
