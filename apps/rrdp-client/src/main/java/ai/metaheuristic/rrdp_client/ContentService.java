package ai.metaheuristic.rrdp_client;

import ai.metaheuristic.rrdp.*;
import ai.metaheuristic.rrdp_disk_storage.FileChecksumProcessor;
import ai.metaheuristic.rrdp_disk_storage.PersistenceUtils;
import ai.metaheuristic.rrdp_disk_storage.SerialUtils;
import ai.metaheuristic.rrdp_disk_storage.SessionUtils;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.apache.http.client.utils.URIBuilder;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static ai.metaheuristic.rrdp.RrdpEnums.*;
import static java.nio.file.StandardOpenOption.*;

/**
 * @author Sergio Lissner
 * Date: 6/27/2022
 * Time: 12:14 AM
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ContentService {

    public static final String REST_V_1_REPLICATION_DATA = "/rest/v1/replication/data/";
    private final Globals globals;

    public void process() {
        List<String> codes = requestCodes();
        for (String code : codes) {
            System.out.println("code = " + code);
            String notification = requestNotification(code);
            if (notification==null) {
                System.out.println("Notification for code '" + code+"' is null");
                continue;
            }
            System.out.println("notification:\n"+ notification);
            RrdpNotificationXml n = RrdpNotificationXmlUtils.parseNotificationXml(notification);

            Path metadataPath = PersistenceUtils.resolveSubPath(globals.path.metadata.path, code);

            String session = SessionUtils.getSession(metadataPath);
//            if (session==null) {
//                session = UUID.randomUUID().toString();
//                SessionUtils.persistSession(metadataPath, session, LocalDate::now);
//            }
//            SerialUtils.persistSerial(metadataPath, serial, LocalDate::now);
            Integer serial = SerialUtils.getSerial(metadataPath);
            if (serial==null) {
                serial = 0;
            }
            else if (!n.sessionId.equals(session)) {
                serial = 0;
            }

            processSerials(code, serial, n);

        }
    }

    private void processSerials(String code, int serial, RrdpNotificationXml n) {
        List<RrdpNotificationXml.Entry> sorted = RrdpCommonUtils.sortNotificationXmlEntries(n);
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
                continue;
            }
            processNotificationEntry(actualSerial, n.sessionId, entry);
        }
    }

    @SneakyThrows
    private void processNotificationEntry(int actualSerial, String sessionId, RrdpNotificationXml.Entry entry) {
        String content = requestEntry(entry.uri);
        if (content==null) {
            throw new IllegalStateException("Notification is broken, entry wasn't found on server, entry: " + entry.uri);
        }
        verifyChecksum(entry, content);
        RrdpEntryXml entryXml = RrdpEntryXmlUtils.parseRrdpEntryXml(content);
        if (!sessionId.equals(entryXml.sessionId)) {
            throw new IllegalStateException("(!sessionId.equals(entryXml.sessionId))");
        }
        System.out.println("Entry: " + entry.uri+"\nnumber of RrdpEntryXml.Entry: "+entryXml.entries.size());
        if (actualSerial!=entryXml.serial) {
            throw new IllegalStateException("(entry.serial!=entryXml.serial)");
        }
        int curr = 1;
        for (RrdpEntryXml.Entry en : entryXml.entries) {
            Path path = entryXmlUriToPath(en);
            System.out.print(""+entryXml.entries.size()+':'+ (curr++) + " "+ path+", length: " + en.length+ " ");
            if (en.state== EntryState.WITHDRAWAL && Files.exists(path)) {
                String hash = FileChecksumProcessor.calcSha1(path);
                if (!en.hash.equals(hash)) {
                    throw new IllegalStateException("(!en.hash.equals(hash))");
                }
                System.out.println("WITHDRAWAL");
                Files.delete(path);
            }
            else if (en.state == EntryState.PUBLISH) {
                if (Files.exists(path)) {
                    String hash = FileChecksumProcessor.calcSha1(path);
                    if (!en.hash.equals(hash)) {
                        throw new IllegalStateException("(!en.hash.equals(hash))");
                    }
                    System.out.println("EXIST, SKIP");
                }
                else {
                    Files.createDirectories(path.getParent());
                    downloadAndPersistEntry(path, en);
                    System.out.println("CREATE");
                }
            }
        }
    }

    private void downloadAndPersistEntry(Path path, RrdpEntryXml.Entry en) {
        getData(
                en.uri,
                (uri) -> Request.Get(uri).connectTimeout(5000).socketTimeout(20000),
                is -> {
                    try (OutputStream os = Files.newOutputStream(path, CREATE, TRUNCATE_EXISTING, WRITE)) {
                        IOUtils.copy(is, os, 8196);
                    }
                    catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } );
    }

    private Path entryXmlUriToPath(RrdpEntryXml.Entry en) {
        int idx = en.uri.indexOf(REST_V_1_REPLICATION_DATA);
        if (idx==-1) {
            throw new IllegalStateException("(idx==-1)");
        }
        String uri = en.uri.substring(idx+REST_V_1_REPLICATION_DATA.length());
        final Path path = globals.path.data.path.resolve(uri);
        return path;
    }

    private void verifyChecksum(RrdpNotificationXml.Entry entry, String content) {
        String actualHash = DigestUtils.sha256Hex(content);
        if (!entry.hash.equals(actualHash)) {
            throw new RuntimeException("Hashes are different, expected: " + entry.hash+", actual: " + actualHash);
        }
    }

    @Nullable
    @SneakyThrows
    public String requestNotification(String dataCode) {
        final StringBuilder content = new StringBuilder();
        getData(
                "/rest/v1/replication/" + dataCode + "/notification.xml",
                (uri) -> Request.Get(uri).connectTimeout(5000).socketTimeout(20000),
                is -> content.append(asString(is)) );

        return content.toString();
    }

    // http://localhost:8080/rest/v1/replication/entry/edition/000001.xml
    @Nullable
    @SneakyThrows
    public String requestEntry(String entryUri) {
        final StringBuilder content = new StringBuilder();
        getData(entryUri, (uri) -> Request.Get(uri).connectTimeout(5000).socketTimeout(20000), is -> content.append(asString(is)));
        return content.toString();
    }

    @SneakyThrows
    public List<String> requestCodes() {
        final StringBuilder content = new StringBuilder();
        getData(
                "/rest/v1/replication/codes",
                (uri) -> Request.Get(uri).connectTimeout(5000).socketTimeout(20000),
                is -> content.append(asString(is)));
        final String s = content.toString();
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
    public void getData(String uri, Function<URI, Request> requestFunc, Consumer<InputStream> inputStreamConsumer) {
        final String url = globals.asset.url + uri;

        final URIBuilder builder = new URIBuilder(url).setCharset(StandardCharsets.UTF_8);
        final URI build = builder.build();
        final Request request = requestFunc.apply(build);

        Response response = Executor.newInstance().execute(request);

        final HttpResponse httpResponse = response.returnResponse();
        if (httpResponse.getStatusLine().getStatusCode() != 200) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final HttpEntity entity = httpResponse.getEntity();
            if (entity != null) {
                entity.writeTo(baos);
            }

            log.error("Server response:\n" + baos.toString());
            throw new RuntimeException("Server response: " + baos);
        }
        final HttpEntity entity = httpResponse.getEntity();
        if (entity != null) {
            try (final InputStream inputStream = entity.getContent()) {
                inputStreamConsumer.accept(inputStream);
            }
        }
    }


}
