package ai.metaheuristic.rrdp;

import java.util.Iterator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author Serge
 * Date: 6/1/2022
 * Time: 4:38 AM
 */
public class RrdpConfig {

    public boolean isFileContent;
    public RrdpEnums.ChecksumAlgo checksumAlgo = RrdpEnums.ChecksumAlgo.SHA1;

    public Supplier<Iterator<RrdpEntry>> rrdpEntryIteatorFunc;
    public Function<Integer, String> nextSerialFunc;
    public Consumer<String> persistXmlPartFunc;
    public Supplier<RrdpEnums.Produce> produceFunc;
}
