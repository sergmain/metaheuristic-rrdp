package ai.metaheuristic.rrdp;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.With;

import javax.annotation.Nullable;
import java.util.function.Supplier;

/**
 * @author Serge
 * Date: 6/1/2022
 * Time: 4:41 AM
 */
@AllArgsConstructor
@NoArgsConstructor
@With
public class RrdpEntry {

    public RrdpEnums.EntryState state;

    public Supplier<String> uri;
    public Supplier<String> hash;
    @Nullable
    public Supplier<String> content = null;
}
