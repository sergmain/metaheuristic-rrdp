package ai.metaheuristic.rrdp;

import ai.metaheuristic.rrdp.core.paths.MetadataPath;
import ai.metaheuristic.rrdp.core.paths.SessionPath;
import ai.metaheuristic.rrdp.disk_storage.ChecksumManager;
import ai.metaheuristic.rrdp.disk_storage.ChecksumPath;
import ai.metaheuristic.rrdp.disk_storage.PersistenceUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.Map;

/**
 * @author Sergio Lissner
 * Date: 6/3/2022
 * Time: 10:36 PM
 */
public class ChecksumManagerTest {

    // disabled because it's not an actual test
    @Disabled("disabled because it's not an actual test")
    @Test
    public void test() {
        final Path rootPath = PersistenceUtils.resolveSubPath(Path.of("result"), "metadata");
        MetadataPath metadataPath = new MetadataPath(PersistenceUtils.resolveSubPath(rootPath, "edition"));
        SessionPath sessionPath = new SessionPath(metadataPath.path.resolve("12345"));

        Map<String, ChecksumPath> map = ChecksumManager.load(sessionPath);

    }
}
