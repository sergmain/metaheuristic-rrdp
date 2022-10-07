package ai.metaheuristic.rrdp_srv_service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * @author Sergio Lissner
 * Date: 6/27/2022
 * Time: 12:09 AM
 */
@RestController
@RequestMapping("/rest/v1/rrdp/command")
@Slf4j
@RequiredArgsConstructor
public class RrdpCommandRestController {

    private final CommandService commandService;

    @GetMapping(value= "/rescan/{code}")
    public boolean rescan(@PathVariable String code) {
        return commandService.startRescanning(code, List.of());
    }

    @PostMapping(value= "/rescan-paths/{code}")
    public String rescanPaths(@Nullable final MultipartFile file, @PathVariable String code) {
        if (file==null) {
            return "File is null";
        }
        try (InputStream is = file.getInputStream()) {
            List<String> lines = IOUtils.readLines(is, StandardCharsets.UTF_8);
            return ""+commandService.startRescanning(code, lines);
        }
        catch (IOException e) {
            log.error("error", e);
            return "Error: " + e.getMessage();
        }
    }

    @GetMapping(value= "/status")
    public RrdpData.RrdpServerStatus status() {
        return CommandService.status();
    }

}
