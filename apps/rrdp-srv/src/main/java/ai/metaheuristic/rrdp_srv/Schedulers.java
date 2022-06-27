package ai.metaheuristic.rrdp_srv;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.Executors;

/**
 * @author Sergio Lissner
 * Date: 6/27/2022
 * Time: 12:27 AM
 */
public class Schedulers {

    @Configuration
    @EnableScheduling
    @RequiredArgsConstructor
    @Slf4j
    public static class NotificationContentRefresherSchedulingConfig implements SchedulingConfigurer {
        private final Globals globals;
        private final ContentService notificationService;

        @Override
        public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
            taskRegistrar.setScheduler(Executors.newSingleThreadScheduledExecutor());
            taskRegistrar.addTriggerTask( this::notificationContentRefresher,
                    context -> {
                        Optional<Date> lastCompletionTime = Optional.ofNullable(context.lastCompletionTime());
                        Instant nextExecutionTime = lastCompletionTime.orElseGet(Date::new).toInstant().plusSeconds(globals.timeout.getNotificationRefresh().toSeconds());
                        return Date.from(nextExecutionTime);
                    }
            );
        }

        public void notificationContentRefresher() {
            if (globals.testing) {
                return;
            }
            log.info("Invoking artifactCleanerAtDispatcher.fixedDelay()");
            notificationService.refreshNotificationContents();
        }
    }


}
