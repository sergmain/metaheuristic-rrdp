package ai.metaheuristic.rrdp_disk_storage;

import ai.metaheuristic.rrdp.paths.MetadataPath;
import ai.metaheuristic.rrdp.paths.SessionPath;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

/**
 * @author Sergio Lissner
 * Date: 6/3/2022
 * Time: 10:36 PM
 */
public class ChecksumManagerTest {

    @Disabled
    @Test
    public void test() {
        final Path rootPath = PersistenceUtils.resolveSubPath(Path.of("result"), "metadata");
        MetadataPath metadataPath = new MetadataPath(PersistenceUtils.resolveSubPath(rootPath, "edition"));
        SessionPath sessionPath = new SessionPath(metadataPath.path.resolve("12345"));

        Map<String, ChecksumPath> map = ChecksumManager.load(sessionPath);

    }
}
