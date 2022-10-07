package ai.metaheuristic.rrdp_disk_storage;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static ai.metaheuristic.rrdp_disk_storage.FileChecksumProcessor.ProcessorParams;
import static ai.metaheuristic.rrdp_disk_storage.FileChecksumProcessor.processPath;

/**
 * @author Sergio Lissner
 * Date: 6/3/2022
 * Time: 6:13 PM
 */
public class WalkerTest {

    @Disabled("Not actual test")
    @Test
    public void process() {
        final String startPoint = "D:\\test-files-edition";
        final Path actualDataPath = Path.of(startPoint);
        final Path metadataPath = PersistenceUtils.resolveSubPath(Path.of("result"), "metadata");

        final ProcessorParams p = new ProcessorParams("/rest/v1/replication/data/", "/rest/v1/replication/entry/");
        processPath(metadataPath, actualDataPath, "edition", List.of(), p);
    }

}
