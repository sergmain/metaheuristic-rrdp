package ai.metaheuristic.rrdp_disk_storage;

import ai.metaheuristic.rrdp.RrdpEnums;
import ai.metaheuristic.rrdp.paths.MetadataPath;

import java.nio.file.Path;

import static ai.metaheuristic.rrdp_disk_storage.SessionUtils.getSession;

/**
 * @author Sergio Lissner
 * Date: 6/2/2022
 * Time: 8:50 PM
 */

public class RrdpDiskStorage {

    public final RrdpEnums.ChecksumAlgo checksumAlgo;
    public final Path data;
    public final MetadataPath metadataPath;

    public RrdpDiskStorage(RrdpEnums.ChecksumAlgo checksumAlgo, Path data, MetadataPath metadataPath) {
        this.checksumAlgo = checksumAlgo;
        this.data = data;
        this.metadataPath = metadataPath;
        final String metadataAbs = metadataPath.path.toFile().getAbsolutePath();
        final String dataAbs = data.toFile().getAbsolutePath();
        if (metadataAbs.startsWith(dataAbs)) {
            throw new IllegalStateException(" path for metedata can't be inside data path. data: "+dataAbs+", metadata: " +metadataAbs);
        }
    }

    public void produce() {
        String session = getSession(metadataPath);
    }

}
