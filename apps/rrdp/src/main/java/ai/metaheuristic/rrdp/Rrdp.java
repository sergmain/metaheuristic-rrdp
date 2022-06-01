package ai.metaheuristic.rrdp;

import lombok.RequiredArgsConstructor;

/**
 * @author Serge
 * Date: 6/1/2022
 * Time: 4:39 AM
 */
@RequiredArgsConstructor
public class Rrdp {

    public final RrdpConfig config;

    public void produce() {
        final RrdpEnums.Produce produce = config.produceFunc.get();
        switch(produce) {
            case SNAPHOST:
                produceSnapshot();
                break;
            case DELTA:
                produceDelta();
                break;
        }
    }

    private void produceSnapshot() {

    }

    private void produceDelta() {

    }

}
