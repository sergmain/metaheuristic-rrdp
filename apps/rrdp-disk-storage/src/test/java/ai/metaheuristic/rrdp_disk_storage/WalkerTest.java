package ai.metaheuristic.rrdp_disk_storage;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

import static ai.metaheuristic.rrdp_disk_storage.FileChecksumProcessor.*;

/**
 * @author Sergio Lissner
 * Date: 6/3/2022
 * Time: 6:13 PM
 */
public class WalkerTest {

    @Test
    public void process() throws IOException {
//        final String startPoint = "D:\\test-files-edition\\edition\\stat2";
        final String startPoint = "D:\\test-files-edition";
        final Path actualDataPath = Path.of(startPoint);
        final Path metadataPath = PersistenceUtils.resolveSubPath(Path.of("result"), "metadata");

        final ProcessorParams p = new ProcessorParams("http://localhost:8080/data-sync/", "http://localhost:8080/data-sync");
        processPath(metadataPath, actualDataPath, p);
    }

}
