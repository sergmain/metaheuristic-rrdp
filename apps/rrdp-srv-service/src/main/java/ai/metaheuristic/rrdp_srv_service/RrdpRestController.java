package ai.metaheuristic.rrdp_srv_service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.AbstractResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.nio.file.Path;
import java.util.List;

/**
 * @author Sergio Lissner
 * Date: 6/27/2022
 * Time: 12:09 AM
 */
@RestController
@RequestMapping("/rest/v1/rrdp/replication")
@Slf4j
@RequiredArgsConstructor
public class RrdpRestController {

    public static final String REST_V_1_REPLICATION_DATA = "/rest/v1/rrdp/replication/data/";
    private final ContentService contentService;

    @GetMapping(value= "/codes", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<String> getDataCodes() {
        return contentService.getDataCodes();
    }

    // http://localhost:8080/rest/v1/rrdp/replication/entry/edition/000001.xml
    @GetMapping(value= "/entry/{dataCode}/{uri}", produces = MediaType.APPLICATION_XML_VALUE)
    public HttpEntity<String> entry(@PathVariable String dataCode, @PathVariable String uri) {
        String content = contentService.getEntryContent(dataCode, uri);
        if (content==null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(content, HttpStatus.OK);
    }

    // localhost:8080/rest/v1/rrdp/replication/data/edition/statistics-unpacked/2020-10/2020-10-15/EXP/681450/677262_EXP.XML
    @GetMapping(value= "/data/**", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<AbstractResource> data() {
        String uri = ServletUriComponentsBuilder.fromCurrentRequest().build().getPath();
        if (uri==null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        if (!uri.startsWith(REST_V_1_REPLICATION_DATA)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        String dataPathStr = uri.substring(REST_V_1_REPLICATION_DATA.length());
        if (dataPathStr.length()==0) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        Path normalizedDataPath = contentService.getDataContentPath(dataPathStr);
        if (normalizedDataPath==null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        ResponseEntity<AbstractResource> responseEntity = new ResponseEntity<>(new FileSystemResource(normalizedDataPath), HttpStatus.OK);
        return responseEntity;
    }

    // localhost:8080/rest/v1/rrdp/replication/edition/notification.xml
    @GetMapping(value= "/{dataCode}/notification.xml", produces = MediaType.APPLICATION_XML_VALUE)
    public HttpEntity<String> notification(@PathVariable String dataCode) {
        String content = contentService.getNotificationContent(dataCode);
        if (content==null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(content, HttpStatus.OK);
    }

}
