package ai.metaheuristic.rrdp_disk_storage;

import ai.metaheuristic.rrdp.RrdpEnums;
import lombok.NoArgsConstructor;

/**
 * @author Sergio Lissner
 * Date: 6/3/2022
 * Time: 7:14 PM
 */

@NoArgsConstructor
public class ChecksumPath {
    public String path;
    public long size;
    public String md5First2Chars;
    public String sha1;
    public RrdpEnums.EntryState state;

    public ChecksumPath(ChecksumPath cp, RrdpEnums.EntryState newState) {
        this.path = cp.path;
        this.size = cp.size;
        this.md5First2Chars = cp.md5First2Chars;
        this.sha1 = cp.sha1;
        this.state = newState;
    }

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
