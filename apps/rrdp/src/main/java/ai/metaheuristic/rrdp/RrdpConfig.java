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

    public Supplier<String> getSession;
    public Supplier<Notification> currentNotification;
    public Supplier<Iterator<RrdpEntry>> rrdpEntryIterator;
    public Function<String, Integer> currSerial;
    public Consumer<String> persistSnapshot;
    public Consumer<String> persistDelta;
    public Supplier<RrdpEnums.ProduceType> produceType;
}
