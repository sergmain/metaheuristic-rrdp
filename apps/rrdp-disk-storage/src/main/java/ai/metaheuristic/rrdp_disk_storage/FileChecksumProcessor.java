package ai.metaheuristic.rrdp_disk_storage;

import lombok.SneakyThrows;
import org.apache.commons.codec.digest.DigestUtils;

import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static java.nio.file.StandardOpenOption.READ;

/**
 * @author Sergio Lissner
 * Date: 6/3/2022
 * Time: 5:10 PM
 */
public class FileChecksumProcessor {

    @SneakyThrows
    public static void process(Path specificMetadataPath, Path dataPath, Consumer<Map<String, ChecksumPath>> consumeFunc) {
        if (Files.notExists(dataPath)) {
            return;
        }

        Path checksumPath = MetadataUtils.getChecksumPath(specificMetadataPath);
        Map<String, ChecksumPath> diff = processDiff(checksumPath, dataPath);
        consumeFunc.accept(diff);
    }

    @SneakyThrows
    private static Map<String, ChecksumPath> processDiff(Path checksumPath, Path dataPath) {
        final Map<String, ChecksumPath> calculatedMap = ChecksumManager.load(checksumPath);
        final Map<String, ChecksumPath> newMap = loadChecksumPath(dataPath, calculatedMap);

        int i=0;
        //                Files.writeString(checkSumPath, ""+relativeName+"\n", CREATE, APPEND, WRITE);
        return newMap;
    }

    // calc checksums for data source
    public static Map<String, ChecksumPath> loadChecksumPath(Path dataPath, Map<String, ChecksumPath> calculatedMap) throws IOException {
        final AtomicInteger count = new AtomicInteger();
        final Map<String, ChecksumPath> map = new HashMap<>(10000);
        Files.walkFileTree(dataPath, new SimpleFileVisitor<>() {
            @SuppressWarnings("ConstantConditions")
            @Override
            public FileVisitResult visitFile(Path p, BasicFileAttributes attrs) throws IOException {
                Path relativePath = dataPath.relativize(p);
                String relativeName = relativePath.toString();
                ChecksumPath cs = new ChecksumPath();
                cs.path = relativeName;
                cs.size = Files.size(p);
                String nameMd5 = DigestUtils.md5Hex(relativeName);
                cs.md5First2Chars = nameMd5.substring(0, 2);
                cs.sha1 = calcSha1(p);

                if (isDifferent(calculatedMap, cs)) {
                    map.put(relativeName, cs);
                }
                count.incrementAndGet();
                return FileVisitResult.CONTINUE;
            }
        });

        System.out.println(""+dataPath+", total: " + count + ", different: " + map.size());
        return map;
    }

    private static boolean isDifferent(Map<String, ChecksumPath> map, ChecksumPath cs) {
        ChecksumPath check = map.get(cs.path);
        if (check==null) {
            return true;
        }
        if (check.size!=cs.size) {
            return true;
        }
        if (!check.sha1.equals(cs.sha1)) {
            return true;
        }
        return false;
    }

    @Nullable
    private static String calcSha1(Path p) throws IOException {
        if (Files.size(p)==0) {
            return null;
        }
        try (InputStream is = Files.newInputStream(p, READ)) {
            return DigestUtils.sha1Hex(is);
        }
    }

    @Nullable
    private static String calcMd5For256Bytes(Path p) throws IOException {
        if (Files.size(p)==0) {
            return null;
        }
        byte[] bytes = new byte[256];
        int count;
        try (InputStream is = Files.newInputStream(p, READ)) {
            count = read(is, bytes, 0, bytes.length);
        }
        try (InputStream is = new ByteArrayInputStream(bytes, 0, count)) {
            return DigestUtils.md5Hex(is);
        }
    }

    // copied from org.apache.commons.io.IOUtils.read(java.io.InputStream, byte[], int, int)
    /**
     * Reads bytes from an input stream.
     * This implementation guarantees that it will read as many bytes
     * as possible before giving up; this may not always be the case for
     * subclasses of {@link InputStream}.
     *
     * @param input where to read input from
     * @param buffer destination
     * @param offset initial offset into buffer
     * @param length length to read, must be &gt;= 0
     * @return actual length read; may be less than requested if EOF was reached
     * @throws IOException if a read error occurs
     * @since 2.2
     */
    public static int read(final InputStream input, final byte[] buffer, final int offset, final int length)
            throws IOException {
        if (length < 0) {
            throw new IllegalArgumentException("Length must not be negative: " + length);
        }
        int remaining = length;
        while (remaining > 0) {
            final int location = length - remaining;
            final int count = input.read(buffer, offset + location, remaining);
            if (count==-1) { // EOF
                break;
            }
            remaining -= count;
        }
        return length - remaining;
    }
}
