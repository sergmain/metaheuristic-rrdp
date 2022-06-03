package ai.metaheuristic.rrdp_disk_storage;

import lombok.SneakyThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * @author Sergio Lissner
 * Date: 6/2/2022
 * Time: 8:56 PM
 */
public class SessionUtils {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @SneakyThrows
    public static String getSession(Path metadataPath) {
        return getSession(metadataPath, LocalDate::now);
    }

    @SneakyThrows
    public static String getSession(Path metadataPath, Supplier<LocalDate> localDateFunc ) {
        Path sessionFile = getSessionFile(metadataPath, localDateFunc);
        String session;
        if (sessionFile!=null) {
            session = Files.readString(sessionFile);
            try {
                //noinspection unused
                UUID sessionUUD = UUID.fromString(session);
                return session;
            }
            catch (IllegalArgumentException e) {
                //
            }
        }
        session = UUID.randomUUID().toString();
        persistSession(session, metadataPath, localDateFunc);
        return session;
    }

    private static Path getSessionPath(Path metadataPath) throws IOException {
        Path sessionPath = metadataPath.resolve("session");
        if (!Files.exists(sessionPath)) {
            Files.createDirectory(sessionPath);
        }
        return sessionPath;
    }

    @SneakyThrows
    private static void persistSession(String session, Path metadataPath, Supplier<LocalDate> localDateFunc) {
        Path sessionPath = getSessionPath(metadataPath);
        Path datePath = getDatePath(sessionPath, localDateFunc);
        int maxFileNumber = getCurrMaxFileNumber(datePath);
        Path actualSessionFile = getActualSessionFile(datePath, maxFileNumber + 1);
        Files.writeString(actualSessionFile, session);
    }

    private static Path getActualSessionFile(Path datePath, int actualFilenamenumber) {
        return datePath.resolve(String.format("%06d.txt", actualFilenamenumber));
    }

    @SneakyThrows
    private static int getCurrMaxFileNumber(Path datePath) {
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

    private static Path getDatePath(Path sessionPath, Supplier<LocalDate> localDateFunc) throws IOException {
        final String currDate = DATE_FORMATTER.format(localDateFunc.get());
        Path datePath = sessionPath.resolve(currDate);
        if (!Files.exists(datePath)) {
            Files.createDirectory(datePath);
        }
        return datePath;
    }

    @SneakyThrows
    public static Path getSessionFile(Path metadataPath) {
        return getSessionFile(metadataPath, LocalDate::now);
    }

    @SneakyThrows
    public static Path getSessionFile(Path metadataPath, Supplier<LocalDate> localDateFunc) {
        Path sessionPath = getSessionPath(metadataPath);
        LocalDate lastDate;
        try (final Stream<Path> list = Files.list(sessionPath) ) {
            lastDate = list.map(f->{
                final String name = f.getFileName().toString();
                return LocalDate.parse(name, DATE_FORMATTER);
            }).max(LocalDate::compareTo).orElse(null);
        }
        Path datePath;
        if (lastDate==null) {
            datePath = sessionPath.resolve(DATE_FORMATTER.format(localDateFunc.get()));
            Files.createDirectory(datePath);
        }
        else {
            datePath = sessionPath.resolve(DATE_FORMATTER.format(lastDate));
        }
        int maxFileNumber = getCurrMaxFileNumber(datePath);
        if (maxFileNumber==-1) {
            return null;
        }
        Path actualSessionFile = getActualSessionFile(datePath, maxFileNumber);
        if (!Files.exists(actualSessionFile)) {
            throw new IllegalStateException("File " + actualSessionFile + " doesn't exist");
        }
        return actualSessionFile;
    }
}
