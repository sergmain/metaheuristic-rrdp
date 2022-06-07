package ai.metaheuristic.rrdp_disk_storage;

import lombok.SneakyThrows;

import java.io.BufferedReader;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Sergio Lissner
 * Date: 6/3/2022
 * Time: 7:09 PM
 */
public class ChecksumManager {

    public static final int CHECKSUM_PATH_LENGTH = 2;

    public static void calc() {
        Path metadataDataPath;
    }

    // Load the current checksums
    @SneakyThrows
    public static Map<String, ChecksumPath> load(Path metadataDataPath, String subPathName) {
        Path specificDataPath = metadataDataPath.resolve(subPathName);
        if (Files.notExists(specificDataPath)) {
            Files.createDirectory(specificDataPath);
        }
        System.out.println("Load checksum from " + specificDataPath);
        final Map<String, ChecksumPath> map = new HashMap<>();
        for (int i = 0; i < 16; i++) {
            for (int j = 0; j < 16; j++) {
                String name = Integer.toString(i, 16) + Integer.toString(j, 16);
                Path md5Path = PersistenceUtils.resolveSubPath(specificDataPath, name);
                if (!Files.isDirectory(md5Path)) {
                    throw new IllegalStateException("(!Files.isDirectory(subDataPath)), " + md5Path);
                }
//                System.out.println("\t" + md5Path);
                loadSubPath(md5Path, map);
            }
        }
        return map;
    }

    @SneakyThrows
    private static void loadSubPath(Path subDataPath, final Map<String, ChecksumPath> checksumPaths) {
        String json = ChechsumUtils.getChecksum(subDataPath);
        if (json==null || json.length()==0) {
            return;
        }
        try (StringReader sr = new StringReader(json); BufferedReader br = new BufferedReader(sr)) {
            br.lines().map(ChecksumManager::toChecksumPath).forEach(c->checksumPaths.put(c.path, c));
        }
        System.out.println(""+subDataPath+": " + checksumPaths.size());
    }

    @SneakyThrows
    private static ChecksumPath toChecksumPath(String json) {
        ChecksumPath checksumPath = JsonUtils.getMapper().readValue(json, ChecksumPath.class);
        return checksumPath;
    }


}
