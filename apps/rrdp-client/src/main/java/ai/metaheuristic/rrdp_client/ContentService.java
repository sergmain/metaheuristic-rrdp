package ai.metaheuristic.rrdp_client;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
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
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Function;

/**
 * @author Sergio Lissner
 * Date: 6/27/2022
 * Time: 12:14 AM
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ContentService {

    private final Globals globals;

    public void process() {
        List<String> codes = requestCodes();
        for (String code : codes) {
            System.out.println("code = " + code);
            String notification = requestNotification(code);
            System.out.println("notification:\n"+ notification);
        }
    }

    @Nullable
    @SneakyThrows
    public String requestNotification(String dataCode) {
        String content = getData(
                "/rest/v1/replication/" + dataCode + "/notification.xml",
                (uri) -> Request.Get(uri).connectTimeout(5000).socketTimeout(20000));
        return content;
    }

    @SneakyThrows
    public List<String> requestCodes() {
        String content = getData(
                "/rest/v1/replication/codes", (uri) -> Request.Get(uri).connectTimeout(5000).socketTimeout(20000));
        if (content == null) {
            return List.of();
        }
        List<String> codes = JsonUtils.getMapper().readValue(content, List.class);
        return codes;
    }

    @SneakyThrows
    @Nullable
    public String getData(String uri, Function<URI, Request> requestFunc) {
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
                return IOUtils.toString(inputStream, StandardCharsets.UTF_8);
            }
        }
        else {
            return null;
        }
    }


}
