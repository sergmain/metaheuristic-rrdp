package ai.metaheuristic.rrdp_client;

import lombok.SneakyThrows;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ResponseHandler;

import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Sergio Lissner
 * Date: 10/16/2022
 * Time: 8:55 PM
 */
public class RrdpServerResponseHandler implements ResponseHandler<RrdpServerResponse> {

    public final Path tempPath;

    public RrdpServerResponseHandler(Path tempPath) {
        this.tempPath = tempPath;
    }

    @SneakyThrows
    @Override
    public RrdpServerResponse handleResponse(final HttpResponse response) {
        final StatusLine statusLine = response.getStatusLine();
        final HttpEntity entity = response.getEntity();

        final RrdpServerResponse serverResponse = new RrdpServerResponse(statusLine.getStatusCode(), Files.createTempFile(tempPath, "data-", ".bin"));
        try (InputStream is = entity.getContent(); OutputStream os = Files.newOutputStream(serverResponse.dataPath); BufferedOutputStream bos = new BufferedOutputStream(os, 0xFFFF)) {
            is.transferTo(bos);
        }
        return serverResponse;
    }

}
