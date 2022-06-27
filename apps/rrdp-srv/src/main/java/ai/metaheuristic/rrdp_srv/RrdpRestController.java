package ai.metaheuristic.rrdp_srv;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * @author Sergio Lissner
 * Date: 6/27/2022
 * Time: 12:09 AM
 */
@RestController
@RequestMapping("/rest/v1/replication")
@Slf4j
@RequiredArgsConstructor
public class RrdpRestController {

    private final NotificationService notificationService;

    // /rest/v1/replication/edition/entry/000001.xml
    @GetMapping(value= "/entry/{dataCode}/{uri}", produces = MediaType.APPLICATION_XML_VALUE)
    public HttpEntity<String> entry(@PathVariable String dataCode) {
        String content = notificationService.getContent(dataCode);
        if (content==null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(content, HttpStatus.OK);
    }

    // /rest/v1/replication/edition/entry/000001.xml
    @GetMapping(value= "/data/{uri}", produces = MediaType.APPLICATION_XML_VALUE)
    public HttpEntity<String> data(@PathVariable String uri) {
        String content = notificationService.getContent(uri);
        if (content==null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(content, HttpStatus.OK);
    }

    @GetMapping(value= "/{dataCode}/notification.xml", produces = MediaType.APPLICATION_XML_VALUE)
    public HttpEntity<String> notification(@PathVariable String dataCode) {
        String content = notificationService.getContent(dataCode);
        if (content==null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(content, HttpStatus.OK);
    }

}
