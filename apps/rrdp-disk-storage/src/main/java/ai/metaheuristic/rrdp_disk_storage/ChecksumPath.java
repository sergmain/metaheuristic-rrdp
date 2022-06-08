package ai.metaheuristic.rrdp_disk_storage;

import ai.metaheuristic.rrdp.RrdpEnums;

/**
 * @author Sergio Lissner
 * Date: 6/3/2022
 * Time: 7:14 PM
 */

public class ChecksumPath {
    public String path;
    public long size;
    public String md5First2Chars;
    public String sha1;
    public RrdpEnums.EntryState state;

    @Override
    public String toString() {
        return "ChecksumPath{" +
                "path='" + path + '\'' +
                ", size=" + size +
                ", md5First2Chars='" + md5First2Chars + '\'' +
                ", sha1='" + sha1 + '\'' +
                '}';
    }
}
