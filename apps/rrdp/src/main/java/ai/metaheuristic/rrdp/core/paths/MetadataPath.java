package ai.metaheuristic.rrdp.core.paths;

import lombok.NoArgsConstructor;

import java.nio.file.Path;

/**
 * @author Sergio Lissner
 * Date: 10/7/2022
 * Time: 6:44 PM
 */
@NoArgsConstructor
public class MetadataPath {
    public Path path;

    public MetadataPath(Path path) {
        this.path = path;
    }
}
