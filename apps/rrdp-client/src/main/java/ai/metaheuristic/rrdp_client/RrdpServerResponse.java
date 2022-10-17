package ai.metaheuristic.rrdp_client;

import lombok.RequiredArgsConstructor;

import java.nio.file.Path;

/**
 * @author Sergio Lissner
 * Date: 10/16/2022
 * Time: 8:59 PM
 */
@RequiredArgsConstructor
public class RrdpServerResponse {
    public final int statusCode;
    public final Path dataPath;
}
