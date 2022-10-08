package ai.metaheuristic.rrdp_disk_storage;

import ai.metaheuristic.rrdp.paths.MetadataPath;
import ai.metaheuristic.rrdp.paths.SessionPath;
import lombok.SneakyThrows;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static java.nio.file.StandardOpenOption.*;

/**
 * @author Sergio Lissner
 * Date: 6/3/2022
 * Time: 7:09 PM
 */
public class ChecksumManager {

    public static final String CHECKSUM_JSON = "checksum.json";

    // Load the current checksums
    @SneakyThrows
    public static Map<String, ChecksumPath> load(SessionPath sessionPath) {
        Path checksumPath = MetadataUtils.getChecksumPath(sessionPath);

        System.out.println("Load checksum from " + checksumPath);
        final Map<String, ChecksumPath> map = new HashMap<>();
        for (int i = 0; i < 16; i++) {
            for (int j = 0; j < 16; j++) {
                String name = Integer.toString(i, 16) + Integer.toString(j, 16);
                Path md5Path = PersistenceUtils.resolveSubPath(checksumPath, name);
                if (!Files.isDirectory(md5Path)) {
                    throw new IllegalStateException("(!Files.isDirectory(subDataPath)), " + md5Path);
                }
                loadSubPath(md5Path, map);
            }
        }
        System.out.println((map.isEmpty() ? "No entries in " : "Loaded " + map.size() + " entries from ")+ checksumPath);
        return map;
    }

    @SneakyThrows
    public static void persist(SessionPath sessionPath, Map<String, ChecksumPath> map) {
        Path checksumPath = MetadataUtils.getChecksumPath(sessionPath);

        Map<String, Path> jsonPaths = new HashMap<>(260);
        for (ChecksumPath value : map.values()) {
            Path jsonPath = jsonPaths.computeIfAbsent(value.md5First2Chars,
                    (o)->PersistenceUtils.resolveSubPath(checksumPath, value.md5First2Chars)).resolve(CHECKSUM_JSON);

            try (BufferedWriter bw = Files.newBufferedWriter(jsonPath, WRITE, CREATE, APPEND)) {
                bw.write(asString(value));
                bw.write("\n");
            }
        }
    }

    @SneakyThrows
    private static void loadSubPath(Path subDataPath, final Map<String, ChecksumPath> checksumPaths) {
        final Path checksumJson = subDataPath.resolve(CHECKSUM_JSON);
        if (Files.notExists(checksumJson)) {
            return;
        }
        String json = Files.readString(checksumJson);
        if (json==null || json.length()==0) {
            return;
        }
        try (StringReader sr = new StringReader(json); BufferedReader br = new BufferedReader(sr)) {
            br.lines().filter(l->l!=null && !l.isBlank())
                    .map(ChecksumManager::toChecksumPath)
                    .forEach(c->checksumPaths.put(c.path, c));
        }
//        System.out.println(""+subDataPath+": " + checksumPaths.size());
    }

    @SneakyThrows
    private static ChecksumPath toChecksumPath(String json) {
        ChecksumPath checksumPath = JsonUtils.getMapper().readValue(json, ChecksumPath.class);
        return checksumPath;
    }

    @SneakyThrows
    private static String asString(ChecksumPath checksumPath) {
        String json = JsonUtils.getMapper().writeValueAsString(checksumPath);
        if (json.contains("\n")) {
            throw new IllegalStateException("(json.contains(\"\\n\"))");
        }
        return json;
    }


}
