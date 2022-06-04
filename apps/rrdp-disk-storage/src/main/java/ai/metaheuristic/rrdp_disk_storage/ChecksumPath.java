package ai.metaheuristic.rrdp_disk_storage;

import javax.annotation.Nullable;
import java.nio.file.Path;

/**
 * @author Sergio Lissner
 * Date: 6/3/2022
 * Time: 7:14 PM
 */
public class ChecksumPath {
    public String path;
    public long size;
    public String md5First2Chars;
    public String md5ShortData;
    public String sha1;

}
