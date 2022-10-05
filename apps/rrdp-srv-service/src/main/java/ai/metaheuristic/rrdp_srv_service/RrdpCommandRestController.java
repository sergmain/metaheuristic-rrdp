package ai.metaheuristic.rrdp_srv_service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

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
    public boolean startRescanning(@PathVariable String code) {
        return commandService.startRescanning(code, null);
    }

    @PostMapping(value= "/rescan-path/{code}")
    public boolean startRescanning(@PathVariable String code, String list) {
        return commandService.startRescanning(code, list);
    }
}
