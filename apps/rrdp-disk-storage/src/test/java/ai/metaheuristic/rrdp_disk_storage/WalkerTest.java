package ai.metaheuristic.rrdp_disk_storage;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

/**
 * @author Sergio Lissner
 * Date: 6/3/2022
 * Time: 6:13 PM
 */
public class WalkerTest {

    @Test
    public void process() throws IOException {
        final String startPoint = "D:\\test-files-edition";
        final Path metadataPath = PersistenceUtils.resolveSubPath(Path.of("result"), "metadata");

        FileChecksumProcessor.process(metadataPath, Path.of(startPoint));
    }
}
