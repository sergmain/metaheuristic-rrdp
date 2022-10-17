package ai.metaheuristic.rrdp_client;

import ai.metaheuristic.rrdp.*;
import ai.metaheuristic.rrdp.paths.MetadataPath;
import ai.metaheuristic.rrdp.paths.SessionPath;
import ai.metaheuristic.rrdp_disk_storage.*;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.file.PathUtils;
import org.apache.commons.io.filefilter.FileFileFilter;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.apache.http.client.utils.URIBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriUtils;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import static ai.metaheuristic.rrdp.RrdpEnums.EntryState;
import static ai.metaheuristic.rrdp.RrdpEnums.NotificationEntryType;

/**
 * @author Sergio Lissner
 * Date: 6/27/2022
 * Time: 12:14 AM
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ContentService {

    public static final String REST_V_1_RRDP_REPLICATION_DATA = "/rest/v1/rrdp/replication/data/";
    private final Globals globals;

    public enum ContextState { OK, NOT_EXIST }

    public static class ProcessingContext {
        public ContextState state = ContextState.OK;
        public String session;
        public int serial;
        public RrdpNotificationXml n;
        public MetadataPath metadataPath;
        public SessionPath sessionPath;
    }

    public Path tempPath;

    @PostConstruct
    public void init() throws IOException {
        final Path p = DirUtils.createMhRrdpTempPath("rrdp-client-");
        if (p==null) {
            throw new RuntimeException("Can't create temp dir");
        }
        tempPath = p;
    }

    @SneakyThrows
    public void process(String code) {
        System.out.println("code = " + code);
        boolean exist = checkCode(code);
        if (!exist) {
            return;
        }

        ProcessingContext ctx = prepareContext(code);
        if (ctx.state == ContextState.NOT_EXIST) {
            deleteForCode(code);
            return;
        }

        List<Pair<Integer, RrdpEntryXml>> entryPairs = processSerials(ctx.n, ctx.serial);

        reduceEntries(entryPairs);

        processNewEntries(ctx.sessionPath, entryPairs);
    }

    private boolean checkCode(String code) {
        List<String> codes = requestCodes();
        if (!codes.contains(code)) {
            deleteForCode(code);
            return false;
        }
        return true;
    }

    public void verify(String code, boolean onlyClean) throws IOException {
        System.out.println("code = " + code);

        ProcessingContext ctx = prepareContext(code);
        if (ctx.state == ContextState.NOT_EXIST) {
            deleteForCode(code);
            return;
        }

        List<Pair<Integer, RrdpEntryXml>> entryPairs = processSerials(ctx.n, 0);
        Set<String> paths = collectPaths(entryPairs);
        System.out.println("Total paths for processing: " + paths.size());

        PathUtils.walk(globals.path.data.path, FileFileFilter.INSTANCE, Integer.MAX_VALUE, false).forEach(p-> {
            if (Files.isDirectory(p)) {
                return;
            }
            final String absPath = p.toAbsolutePath().toString();
            if (!paths.contains(absPath)) {
                try {
                    Files.delete(p);
                }
                catch (IOException e) {
                    System.out.println("Error while deleting a path: " + absPath+", error: " + e.getMessage());
                }
            }
        });

        if (!onlyClean) {
            entryPairs = processSerials(ctx.n, 0);
            reduceEntries(entryPairs);
            processNewEntries(ctx.sessionPath, entryPairs);
        }
    }

    private Set<String> collectPaths(List<Pair<Integer, RrdpEntryXml>> entryPairs) {
        Set<String> set = new HashSet<>(2_000_000);
        for (Pair<Integer, RrdpEntryXml> pair : entryPairs) {
            for (RrdpEntryXml.Entry en : pair.getRight().entries) {
                Path path = entryXmlUriToPath(en);
                set.add(path.toAbsolutePath().toString());
            }
        }
        return set;
    }

    private void deleteForCode(String code) {
        System.out.println("Code "+ code +" doesn't exist at server side. Local dir will be deleted.");
        final Path path = globals.path.data.path.resolve(code);
        if (Files.notExists(path)) {
            return;
        }
        try {
            PathUtils.delete(path);
        }
        catch (IOException e) {
            System.out.println("can't delete path " + path+", error: " + e.getMessage());
        }
    }

    private ProcessingContext prepareContext(String code) {
        ProcessingContext ctx = new ProcessingContext();
        ctx.metadataPath = new MetadataPath(PersistenceUtils.resolveSubPath(globals.path.metadata.path, code));

        String notification = requestNotification(code);
        if (notification==null) {
            ctx.state = ContextState.NOT_EXIST;
            System.out.println("Notification for code '" + code + "' is null");
            return ctx;
        }

        System.out.println("notification:\n"+ notification);
        ctx.n = RrdpNotificationXmlUtils.parseNotificationXml(notification);
        ctx.sessionPath = new SessionPath(ctx.metadataPath.path.resolve(ctx.n.sessionId));

        int serial = 0;
        if (!ctx.n.sessionId.equals(SessionUtils.getSession(ctx.metadataPath))) {
            SessionUtils.persistSession(ctx.metadataPath, ctx.n.sessionId, LocalDate::now);
        }
        else {
            Integer currSerial = SerialUtils.getSerial(ctx.sessionPath);
            serial = (currSerial==null) ? 0 : currSerial;
        }
        ctx.serial = serial;
        ctx.session = ctx.n.sessionId;

        return ctx;
    }

    private List<Pair<Integer, RrdpEntryXml>> processSerials(RrdpNotificationXml n, int serial) throws IOException {
        List<RrdpNotificationXml.Entry> sorted = RrdpCommonUtils.sortNotificationXmlEntries(n);
        List<Pair<Integer, RrdpEntryXml>> entryPairs = new ArrayList<>();
        for (RrdpNotificationXml.Entry entry : sorted) {
            int actualSerial = 0;
            if (entry.type == NotificationEntryType.DELTA) {
                if (entry.serial == null) {
                    throw new IllegalStateException("(entry.serial==null)");
                }
                actualSerial = entry.serial;
            }
            else if (entry.type == NotificationEntryType.SNAPSHOT) {
                actualSerial = 1;
            }
            if (actualSerial<=serial) {
                System.out.println("serial #"+actualSerial+" was already processed");
                continue;
            }
            entryPairs.add(Pair.of(actualSerial, requestRrdpEntryXml(n.sessionId, actualSerial, entry)));
        }
        return entryPairs;
    }

    private void processNewEntries(SessionPath sessionPath, List<Pair<Integer, RrdpEntryXml>> entryPairs) {
        for (Pair<Integer, RrdpEntryXml> pair : entryPairs) {
            processNotificationEntry(pair.getRight());
            SerialUtils.persistSerial(sessionPath, pair.getLeft(), LocalDate::now);
        }
    }

    private static void reduceEntries(List<Pair<Integer, RrdpEntryXml>> entryPairs) {
        if (entryPairs.size()<2) {
            return;
        }

        // -1 because there is no meaning to verify the last delta
        for (int i = 0; i < entryPairs.size()-1; i++) {
            Pair<Integer, RrdpEntryXml> pair = entryPairs.get(i);

            for (RrdpEntryXml.Entry entry : pair.getRight().entries) {
                if (entry.state==EntryState.WITHDRAWAL) {
                    continue;
                }
                for (int k = i+1; k < entryPairs.size(); k++) {
                    Pair<Integer, RrdpEntryXml> otherPair = entryPairs.get(k);
                    boolean changed = false;
                    for (RrdpEntryXml.Entry toCheck : otherPair.getRight().entries) {
                        if (toCheck.state==EntryState.WITHDRAWAL && toCheck.uri.equals(entry.uri)) {
                            entry.state = EntryState.WITHDRAWAL;
                            changed = true;
                            break;
                        }
                    }
                    if (changed) {
                        break;
                    }
                }
            }
        }
    }

    public RrdpEntryXml requestRrdpEntryXml(String sessionId, int serial, RrdpNotificationXml.Entry entry) throws IOException {
        Path content = requestEntry(entry.uri);
        try (InputStream is = Files.newInputStream(content)) {
            verifyChecksum(entry, is);
        }
        try (InputStream is = Files.newInputStream(content)) {
            RrdpEntryXml entryXml = RrdpEntryXmlUtils.parseRrdpEntryXml(is);
            if (!sessionId.equals(entryXml.sessionId)) {
                throw new IllegalStateException("(!sessionId.equals(entryXml.sessionId))");
            }
            System.out.println("Entry: " + entry.uri + "\nnumber of RrdpEntryXml.Entry: " + entryXml.entries.size());
            if (serial != entryXml.serial) {
                throw new IllegalStateException("(entry.serial!=entryXml.serial)");
            }
            return entryXml;
        }
    }

    @SneakyThrows
    private void processNotificationEntry(RrdpEntryXml entryXml) {
        int curr = 1;
        for (RrdpEntryXml.Entry en : entryXml.entries) {
            Path path = entryXmlUriToPath(en);
            System.out.print(""+entryXml.entries.size()+':'+ (curr++) + " "+ path+", length: " + en.length+ " ");
            if (en.state==EntryState.WITHDRAWAL) {
                if (Files.exists(path)) {
                    String hash = FileChecksumProcessor.calcSha1(path);
                    System.out.println("" + ((en.hash.equals(hash)) ? "" : "!!! HASH IS DIFFERENT, ") + "EXIST, WITHDRAWAL");
                    Files.delete(path);
                }
                else {
                    System.out.println("NOT EXIST, WITHDRAWAL");
                }
            }
            else if (en.state == EntryState.PUBLISH) {
                if (Files.exists(path)) {
                    String hash = FileChecksumProcessor.calcSha1(path);
                    if (!en.hash.equals(hash)) {
                        downloadAndPersistEntry(path, en);
                        System.out.println("!!! HASH IS DIFFERENT, REPLACED");
                    }
                    else {
                        System.out.println("EXIST, SAME");
                    }
                }
                else {
                    Files.createDirectories(path.getParent());
                    downloadAndPersistEntry(path, en);
                    System.out.println("CREATED");
                }
            }
        }
    }

    private void downloadAndPersistEntry(Path path, RrdpEntryXml.Entry en) throws IOException {
        RrdpServerResponse rrdpServerResponse = getData(en.uri, (uri) -> Request.Get(uri).connectTimeout(5000).socketTimeout(20000));
        Files.copy(rrdpServerResponse.dataPath, path);
    }

    private Path entryXmlUriToPath(RrdpEntryXml.Entry en) {
        int idx = en.uri.indexOf(REST_V_1_RRDP_REPLICATION_DATA);
        if (idx==-1) {
            throw new IllegalStateException("(idx==-1)");
        }
        String uri = en.uri.substring(idx+ REST_V_1_RRDP_REPLICATION_DATA.length());
        final Path path = globals.path.data.path.resolve(uri);
        return path;
    }

    private static void verifyChecksum(RrdpNotificationXml.Entry entry, InputStream is) throws IOException {
        String actualHash = DigestUtils.sha256Hex(is);
        if (!entry.hash.equals(actualHash)) {
            throw new RuntimeException("Hashes are different, expected: " + entry.hash+", actual: " + actualHash);
        }
    }

    @Nullable
    @SneakyThrows
    public String requestNotification(String dataCode) {
        RrdpServerResponse rrdpServerResponse = getData(
                "/rest/v1/rrdp/replication/" + dataCode + "/notification.xml",
                (uri) -> Request.Get(uri).connectTimeout(5000).socketTimeout(20000));

        return Files.readString(rrdpServerResponse.dataPath);
    }

    // http://localhost:8080/rest/v1/rrdp/replication/entry/edition/000001.xml
    @SneakyThrows
    public Path requestEntry(String entryUri) {
        RrdpServerResponse rrdpServerResponse = getData(entryUri, (uri) -> Request.Get(uri).connectTimeout(5000).socketTimeout(20000));
        return rrdpServerResponse.dataPath;
    }

    @SneakyThrows
    public List<String> requestCodes() {
        RrdpServerResponse rrdpServerResponse = getData(
                "/rest/v1/rrdp/replication/codes",
                (uri) -> Request.Get(uri).connectTimeout(5000).socketTimeout(20000));

        final String s = Files.readString(rrdpServerResponse.dataPath);
        if (s.isBlank()) {
            return List.of();
        }
        List<String> codes = JsonUtils.getMapper().readValue(s, List.class);
        return codes;
    }

    @SneakyThrows
    public static String asString(InputStream is) {
        return IOUtils.toString(is, StandardCharsets.UTF_8);
    }

    @SneakyThrows
    public RrdpServerResponse getData(String uriPath, Function<URI, Request> requestFunc) {
        final URI uri = getUri(globals.asset.url, uriPath);
        final Request request = requestFunc.apply(uri);

        Response response = Executor.newInstance().execute(request);

        final RrdpServerResponse rrdpServerResponse = response.handleResponse(new RrdpServerResponseHandler(tempPath));

        if (rrdpServerResponse.statusCode != 200) {
            final String s = Files.readString(rrdpServerResponse.dataPath);
            final String es = "Server error: " + rrdpServerResponse.statusCode + ", uri: " + uri.toString() + ", response:\n" +
                              (s.isBlank() ? "<response is blank>" : s);
            log.error(es);
            throw new RuntimeException(es);
        }
        return rrdpServerResponse;
    }

    public static URI getUri(String baseUrl, String uri) throws URISyntaxException {
        final String url = baseUrl + UriUtils.encodePath(uri, StandardCharsets.UTF_8);

        final URIBuilder builder = new URIBuilder(url).setCharset(StandardCharsets.UTF_8);
        final URI build = builder.build();
        return build;
    }


}
