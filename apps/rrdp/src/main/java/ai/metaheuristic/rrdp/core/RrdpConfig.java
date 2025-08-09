package ai.metaheuristic.rrdp.core;

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
    public boolean lengthOfContent = false;

    public Supplier<String> getSession;
    public Supplier<RrdpNotificationXml> currentNotification;
    public Supplier<Iterator<RrdpEntryProvider>> rrdpEntryIterator;
    public Function<String, Integer> currSerial;
    public Consumer<String> persistNotificationEntry;
    public Supplier<RrdpEnums.NotificationEntryType> produceType;
}
