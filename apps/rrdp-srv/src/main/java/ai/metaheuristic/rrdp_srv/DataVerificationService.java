package ai.metaheuristic.rrdp_srv;

import ai.metaheuristic.rrdp_disk_storage.FileChecksumProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

import static ai.metaheuristic.rrdp_disk_storage.FileChecksumProcessor.processPath;

/**
 * @author Sergio Lissner
 * Date: 6/29/2022
 * Time: 12:23 AM
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DataVerificationService {

    private final Globals globals;

    private static final FileChecksumProcessor.ProcessorParams PROCESSOR_PARAMS =
            new FileChecksumProcessor.ProcessorParams("/rest/v1/rrdp/replication/data/", "/rest/v1/rrdp/replication/entry/");

    private final ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);

    private final AtomicInteger countVerifyTasks = new AtomicInteger();

    @SuppressWarnings("unused")
    public synchronized void addVerificationTask() {
        if (countVerifyTasks.get()>0) {
            return;
        }
        countVerifyTasks.incrementAndGet();
    }

    public void processVerificationTask() {
        if (executor.getActiveCount()>0) {
            return;
        }
        executor.submit(() -> {
            while (countVerifyTasks.get()>0) {
                verifyData();
                countVerifyTasks.decrementAndGet();
            }
        });
    }

    public void verifyData() {
        processPath(globals.path.metadata.path, globals.path.source.path, PROCESSOR_PARAMS);
    }

}
