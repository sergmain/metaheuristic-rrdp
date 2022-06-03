package ai.metaheuristic.rrdp_disk_storage;

import lombok.Data;

/**
 * @author Sergio Lissner
 * Date: 6/2/2022
 * Time: 2:36 PM
 */
@Data
public class FileEntry {
    public String path;
    public long size;
    public String checksum;
}
