package shelfwise.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AppConfigTest {

    @TempDir
    Path dir;

    private Path write(String content) throws IOException {
        Path file = dir.resolve("db.properties");
        Files.writeString(file, content, StandardCharsets.UTF_8);
        return file;
    }

    @Test
    void readsAllSettings() throws IOException {
        AppConfig config = AppConfig.load(write("""
                db.url=jdbc:mysql://db.example:3306/lib
                db.user=library
                db.password=secret
                """));
        assertEquals("jdbc:mysql://db.example:3306/lib", config.url());
        assertEquals("library", config.user());
        assertEquals("secret", config.password());
    }

    @Test
    void passwordWithEnyeIsReadAsUtf8() throws IOException {
        // The bug found while testing v1: "ñ" arrived at MySQL as "Ã±"
        AppConfig config = AppConfig.load(write("db.user=root\ndb.password=Contraseña#//\n"));
        assertEquals("Contraseña#//", config.password());
    }

    @Test
    void urlDefaultsToTheLocalShelfwiseDatabase() throws IOException {
        AppConfig config = AppConfig.load(write("db.user=root\n"));
        assertEquals(AppConfig.DEFAULT_URL, config.url());
        assertEquals("", config.password());
    }

    @Test
    void missingFileExplainsWhatToDo() {
        IOException ex = assertThrows(IOException.class, () -> AppConfig.load(dir.resolve("nope.properties")));
        assertTrue(ex.getMessage().contains("db.properties.example"));
    }

    @Test
    void missingUserIsAnError() throws IOException {
        Path file = write("db.password=x\n");
        assertThrows(IOException.class, () -> AppConfig.load(file));
    }

    @Test
    void toStringHidesThePassword() throws IOException {
        AppConfig config = AppConfig.load(write("db.user=root\ndb.password=TopSecret\n"));
        assertFalse(config.toString().contains("TopSecret"));
    }
}
