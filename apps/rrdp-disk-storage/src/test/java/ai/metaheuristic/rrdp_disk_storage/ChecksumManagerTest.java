package ai.metaheuristic.rrdp_disk_storage;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * @author Sergio Lissner
 * Date: 6/3/2022
 * Time: 10:36 PM
 */
public class ChecksumManagerTest {

    @Test
    public void test() throws IOException {
        final Path metadataPath = PersistenceUtils.resolveSubPath(Path.of("result"), "metadata");
        final Path metadataDataPath = MetadataUtils.getDataPath(metadataPath);

        Map<String, ChecksumPath> map = ChecksumManager.load(metadataDataPath, "edition");

    }
}
