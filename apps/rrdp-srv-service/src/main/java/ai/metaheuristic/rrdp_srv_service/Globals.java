package ai.metaheuristic.rrdp_srv_service;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesBinding;
import org.springframework.boot.convert.DurationUnit;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.nio.file.Path;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * @author Sergio Lissner
 * Date: 6/27/2022
 * Time: 12:14 AM
 */
@ConfigurationProperties("rrdp.server")
@Getter
@Setter
@Slf4j
@RequiredArgsConstructor
public class Globals {

    public static final Duration SECONDS_60 = Duration.ofSeconds(60);
    public static final Duration SECONDS_120 = Duration.ofSeconds(120);

    @Component
    @ConfigurationPropertiesBinding
    public static class MetadataPathConverter implements Converter<String, PathPath> {
        @Override
        public PathPath convert(String from) {
            return new PathPath(toPath(from));
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PathPath {
        public Path path = null;
    }

    @Setter
    public static class RrdpPath {
        public PathPath metadata = new PathPath();
        public PathPath source = new PathPath();
    }

    @Setter
    public static class Timeout {

        @DurationUnit(ChronoUnit.SECONDS)
        public Duration notificationRefresh = Duration.ofSeconds(120);
        @DurationUnit(ChronoUnit.SECONDS)
        public Duration codesRefresh = Duration.ofSeconds(60);


        public Duration getNotificationRefresh() {
            return notificationRefresh.toSeconds() >= 10 && notificationRefresh.toSeconds() <= 3600 ? notificationRefresh : SECONDS_120;
        }

        public Duration getCodesRefresh() {
            return codesRefresh.toSeconds() >= 10 && codesRefresh.toSeconds() <= 600 ? codesRefresh : SECONDS_60;
        }
    }

    @Setter
    public static class ThreadNumber {
        private int scheduler = 3;

        public int getScheduler() {
            return minMax( scheduler, 3, 6);
        }
    }

    public final RrdpPath path = new RrdpPath();
    public final Timeout timeout = new Timeout();
    public final ThreadNumber threadNumber = new ThreadNumber();

    public boolean sslRequired = false;
    public boolean testing = false;

    @SneakyThrows
    @PostConstruct
    public void postConstruct() {
        if (path.metadata==null || path.metadata.path ==null) {
            throw new IllegalStateException("(metadata.path==null || metadata.path.path ==null)");
        }
        if (path.source==null || path.source.path ==null) {
            throw new IllegalStateException("(path.source==null || path.source.path ==null)");
        }
        logGlobals();
        logSystemEnvs();
        logGarbageCollectors();
    }

    @Nullable
    private static Path toPath(@Nullable String dirAsString) {
        if (dirAsString==null || dirAsString.isBlank()) {
            return null;
        }

        // special case for ./some-dir
        if (dirAsString.charAt(0) == '.' && (dirAsString.charAt(1) == '\\' || dirAsString.charAt(1) == '/')) {
            return Path.of(dirAsString.substring(2));
        }
        return Path.of(dirAsString);
    }

    public static int minMax(int curr, int min, int max) {
        if (curr >=min && curr <=max) {
            return curr;
        }
        else if (curr <min) {
            return min;
        }
        return max;
    }

    private static void logSystemEnvs() {
        log.info("Current system properties:");
        System.getProperties().forEach( (o, o2) -> {
            if (o instanceof String) {
                if (StringUtils.equalsAny((String)o, "java.class.path", "java.library.path", "line.separator")) {
                    return;
                }
            }
            log.info("'\t{}: {}", o, o2);
        });
    }

    private void logGlobals() {
        final Runtime rt = Runtime.getRuntime();
        log.warn("Memory, free: {}, max: {}, total: {}", rt.freeMemory(), rt.maxMemory(), rt.totalMemory());
        log.info("Current globals:");
        log.info("'\ttesting: {}", testing);
        log.info("'\tthreadNumber.scheduler: {}", threadNumber.getScheduler());
        log.info("'\ttimeout.notificationRefresh: {}", timeout.getNotificationRefresh());
        log.info("'\ttimeout.codesRefresh: {}", timeout.getCodesRefresh());
        log.info("'\tpath.metadata: {}", path.metadata.path !=null ? path.metadata.path : "<!!!ERROR!!! path.metadata is null>");
        log.info("'\tpath.source: {}", path.source.path !=null ? path.source.path : "<!!!ERROR!!! path.source is null>");
    }

    private static void logGarbageCollectors() {
        log.info("Garbage collectors:");
        List<GarbageCollectorMXBean> beans = ManagementFactory.getGarbageCollectorMXBeans();
        for (GarbageCollectorMXBean bean : beans) {
            log.info("'\t"+ bean.getName());
        }
    }


}
