package ai.metaheuristic.rrdp.disk_storage;

import lombok.SneakyThrows;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * @author Sergio Lissner
 * Date: 6/3/2022
 * Time: 12:19 AM
 */
public class PersistenceUtils {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static List<Path> getPaths(Path path) throws IOException {
        List<Path> paths = new ArrayList<>();
        try (final Stream<Path> list = Files.list(path) ) {
            list.forEach(paths::add);
        }
        return paths;
    }

    @SneakyThrows
    public static Path resolveSubPath(Path path, String subPathName) {
        Path subPath = path.resolve(subPathName);
        if (Files.notExists(subPath)) {
            Files.createDirectories(subPath);
        }
        return subPath;
    }

    @Nullable
    @SneakyThrows
    public static String getLatestContent(
            Path specificMetadataPath, Function<String, Boolean> verifyContentFunc) {

        Path latestContentFile = getLatestContentFile(specificMetadataPath, null);
        String content = null;
        if (latestContentFile!=null) {
            content = Files.readString(latestContentFile);
            if (!verifyContentFunc.apply(content)) {
                throw new IllegalStateException("Wrong content: " + content);
            }
        }
        return content;
    }

    @Nullable
    @SneakyThrows
    public static String persistContent(
            Path specificMetadataPath, Supplier<String> contentFunc,
            Function<String, Boolean> vefiryContentFunc, Supplier<LocalDate> localDateFunc ) {

        Path latestContentFile = getLatestContentFile(specificMetadataPath, localDateFunc);
        String content;
        if (latestContentFile!=null) {
            content = Files.readString(latestContentFile);
            if (!vefiryContentFunc.apply(content)) {
                throw new IllegalStateException("Wrong content: " + content);
            }
        }
        if ((content = contentFunc.get())!=null){
            persistContentInternal(content, specificMetadataPath, localDateFunc);
        }
        return content;
    }

    @SneakyThrows
    private static void persistContentInternal(String content, Path specificMetadataPath, Supplier<LocalDate> localDateFunc) {
        Path datePath = getDatePath(specificMetadataPath, localDateFunc);
        int maxFileNumber = getCurrMaxFileNumber(datePath);
        Path actualLatestContentFile = getFilenameForNumber(datePath, maxFileNumber!=-1 ? maxFileNumber + 1 : 1);
        Files.writeString(actualLatestContentFile, content);
    }

    public static String asUri(@Nullable String base, String part) {
        final String newPart = part.replace('\\', '/');
        final String uri;
        if (base != null) {
            uri = base.charAt(base.length()-1)=='/' ? base + newPart : base + '/' + newPart;
        }
        else {
            uri = newPart;
        }
        return uri;
    }

    public static Path getFilenameForNumber(Path parentPath, int filenameNumber) {
        return getFilenameForNumber(parentPath, filenameNumber, ".txt");
    }

    public static Path getFilenameForNumber(Path parentPath, int filenameNumber, String ext) {
        return parentPath.resolve(formatFilename(filenameNumber, ext));
    }

    public static String formatFilename(int filenameNumber, String ext) {
        return String.format("%06d%s", filenameNumber, ext);
    }

    @SneakyThrows
    private static int getCurrMaxFileNumber(@Nullable Path datePath) {
        if (datePath==null) {
            return -1;
        }
        int maxFileNumber;
        try (final Stream<Path> list = Files.list(datePath) ) {
            maxFileNumber = list.mapToInt(f-> {
                final String name = f.getFileName().toString();
                if (!name.endsWith(".txt")) {
                    throw new IllegalStateException("File " + name + " must end with '.txt'");
                }
                String filename = name.substring(0, name.length()-4);
                return Integer.parseInt(filename);
            }).max().orElse(-1);
        }
        return maxFileNumber;
    }

    private static Path getDatePath(Path specificMetadataPath, Supplier<LocalDate> localDateFunc) throws IOException {
        final String currDate = DATE_FORMATTER.format(localDateFunc.get());
        Path datePath = specificMetadataPath.resolve(currDate);
        if (Files.notExists(datePath)) {
            Files.createDirectory(datePath);
        }
        return datePath;
    }

    @Nullable
    @SneakyThrows
    public static Path getLatestContentFile(Path specificMetadataPath, @Nullable Supplier<LocalDate> localDateFunc) {
        LocalDate lastDate;
        try (final Stream<Path> list = Files.list(specificMetadataPath) ) {
            lastDate = list.map(f->{
                final String name = f.getFileName().toString();
                return LocalDate.parse(name, DATE_FORMATTER);
            }).max(LocalDate::compareTo).orElse(null);
        }
        Path datePath=null;
        if (lastDate==null) {
            if (localDateFunc!=null) {
                datePath = specificMetadataPath.resolve(DATE_FORMATTER.format(localDateFunc.get()));
                Files.createDirectory(datePath);
            }
        }
        else {
            datePath = specificMetadataPath.resolve(DATE_FORMATTER.format(lastDate));
        }
        if (datePath==null) {
            return null;
        }
        int maxFileNumber = getCurrMaxFileNumber(datePath);
        if (maxFileNumber==-1) {
            return null;
        }
        Path actualLatestContentFile = getFilenameForNumber(datePath, maxFileNumber);
        if (Files.notExists(actualLatestContentFile)) {
            throw new IllegalStateException("File " + actualLatestContentFile + " doesn't exist");
        }
        return actualLatestContentFile;
    }

}
