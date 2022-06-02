package ai.metaheuristic.rrdp;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.With;

import java.util.Iterator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author Serge
 * Date: 6/1/2022
 * Time: 4:38 AM
 */
@With
@AllArgsConstructor
@NoArgsConstructor
public class RrdpConfig {

    public boolean rfc8182 = true;
    public boolean isFileContent = true;
    public boolean isErrorOnFileChange = true;
    public RrdpEnums.ChecksumAlgo checksumAlgo = RrdpEnums.ChecksumAlgo.SHA1;

    public Supplier<String> getSession;
    public Supplier<String> currentNotification;
    public Supplier<Iterator<RrdpEntry>> rrdpEntryIteator;
    public Function<String, Integer> nextSerial;
    public Function<String, Integer> currSerial;
    public Consumer<String> persistSnapshot;
    public Consumer<String> persistDelta;
    public Consumer<String> persistNotification;
    public Supplier<RrdpEnums.ProduceType> produceType;
}
