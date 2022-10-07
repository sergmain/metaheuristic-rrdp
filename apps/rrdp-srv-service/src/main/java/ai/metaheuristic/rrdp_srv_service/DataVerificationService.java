package ai.metaheuristic.rrdp_srv_service;

import ai.metaheuristic.rrdp_disk_storage.FileChecksumProcessor;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;

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

    public static class ParamsMap {
        public final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
        public final ReentrantReadWriteLock.ReadLock readLock = lock.readLock();
        public final ReentrantReadWriteLock.WriteLock writeLock = lock.writeLock();

        public final TreeMap<String, RrdpData.TaskParams> taskParams = new TreeMap<>();

        @SneakyThrows
        public List<String> getKeys() {
            try {
                paramsMap.readLock.lock();
                return new ArrayList<>(taskParams.keySet());
            } finally {
                paramsMap.readLock.unlock();
            }
        }

        @SneakyThrows
        public List<RrdpData.TaskParams> getTasksParams() {
            try {
                paramsMap.readLock.lock();
                return new ArrayList<>(taskParams.values());
            } finally {
                paramsMap.readLock.unlock();
            }
        }

        @SuppressWarnings("unused")
        @Nullable
        @SneakyThrows
        public RrdpData.TaskParams geParams(String code) {
            try {
                readLock.lock();
                return taskParams.get(code);
            } finally {
                readLock.unlock();
            }
        }

        @Nullable
        @SneakyThrows
        public RrdpData.TaskParams getAndRemoveParams(String code) {
            try {
                writeLock.lock();
                RrdpData.TaskParams params = taskParams.get(code);
                if (params!=null) {
                    taskParams.remove(code);
                }
                return params;
            } finally {
                writeLock.unlock();
            }
        }

        @SneakyThrows
        public void putParams(String code, RrdpData.TaskParams params) {
            try {
                writeLock.lock();
                if (taskParams.containsKey(code)) {
                    log.warn("Task for rescanning with code '"+ code +"'  already exists");
                    return;
                }
                taskParams.put(code, params);
            } finally {
                writeLock.unlock();
            }
        }
    }

    @Nullable
    public static RrdpData.TaskParams activeTaskParams = null;
    public static final ParamsMap paramsMap = new ParamsMap();

    public static class ParamsIterator implements Iterator<RrdpData.TaskParams> {

        @Nullable
        private Iterator<String> iterator = null;

        @Override
        public boolean hasNext() {
            if (iterator==null) {
                List<String> codes = paramsMap.getKeys();
                iterator = codes.iterator();
            }
            boolean b = iterator.hasNext();
            if (!b) {
                iterator = null;
            }
            return b;
        }

        @Nullable
        @Override
        public RrdpData.TaskParams next() {
            if (iterator==null) {
                throw new IllegalStateException("(iterator==null)");
            }
            String code = iterator.next();
            return paramsMap.getAndRemoveParams(code);
        }
    }

    public synchronized void addVerificationTask(RrdpData.TaskParams params) {
        paramsMap.putParams(params.code, params);

        if (countVerifyTasks.get()>0) {
            return;
        }
        countVerifyTasks.incrementAndGet();
    }

    public void processVerificationTask() {
        if (executor.getActiveCount()>0) {
            return;
        }
        executor.execute(()-> {
                    ParamsIterator it = new ParamsIterator();
                    while (it.hasNext()) {
                        activeTaskParams = it.next();
                        if (activeTaskParams == null) {
                            continue;
                        }
                        processPath(globals.path.metadata.path, globals.path.source.path, activeTaskParams.code, activeTaskParams.paths, PROCESSOR_PARAMS);
                        activeTaskParams = null;
                    }
        });
        countVerifyTasks.decrementAndGet();
    }

    public static RrdpData.RrdpServerStatus status() {
        return new RrdpData.RrdpServerStatus(activeTaskParams, paramsMap.getTasksParams());

    }
}
