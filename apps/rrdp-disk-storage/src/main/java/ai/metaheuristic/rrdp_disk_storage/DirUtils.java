package ai.metaheuristic.rrdp_disk_storage;

import ai.metaheuristic.rrdp.exceptions.RrdpException;
import lombok.SneakyThrows;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Sergio Lissner
 * Date: 10/16/2022
 * Time: 9:04 PM
 */
public class DirUtils {

    public static Path getJavaIoTmpDir() {
        return Path.of(System.getProperty("java.io.tmpdir"));
    }

    @SneakyThrows
    @Nullable
    public static Path createMhRrdpTempPath(String prefix) {
        return createMhRrdpTempPath(getJavaIoTmpDir(), prefix);
    }

    @SneakyThrows
    @Nullable
    public static Path createMhRrdpTempPath(Path base, String prefix) {
        Path trgDir = base.resolve("metaheuristic-rrdp-temp");
        if (Files.notExists(trgDir)) {
            Files.createDirectories(trgDir);
        }
        return createTempPath(trgDir, prefix);
    }

    @Nullable
    public static Path createTempPath(Path trgDir, String prefix) {
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        String prefixDate = format.format(date);
        for (int i = 0; i < 5; i++) {
            Path newTempDir = trgDir.resolve( prefix + prefixDate + "-" + System.nanoTime());
            if (Files.exists(newTempDir)) {
                continue;
            }
            try {
                Files.createDirectories(newTempDir);
                if (Files.exists(newTempDir)) {
                    return newTempDir;
                }
            } catch (IOException e) {
                throw new RrdpException(String.format("#017.040 Can't create temporary dir %s, attempt #%d, error: %s", newTempDir.normalize(), i, e.getMessage()), e);
            }
        }
        return null;
    }
}
