package ai.metaheuristic.rrdp_srv;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ConcurrentTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author Sergio Lissner
 * Date: 6/27/2022
 * Time: 12:19 AM
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
@EnableConfigurationProperties(Globals.class)
@EnableScheduling
public class Config {

    @Configuration
    @ComponentScan("ai.metaheuristic")
    @EnableAsync
    @RequiredArgsConstructor
    @Slf4j
    public static class SpringAsyncConfig implements AsyncConfigurer {

        private final Globals globals;

        @Bean
        public ThreadPoolTaskScheduler threadPoolTaskScheduler() {
            ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
            log.info("Config.threadPoolTaskScheduler() will use {} as a number of threads for an schedulers", globals.threadNumber.getScheduler());
            threadPoolTaskScheduler.setPoolSize(globals.threadNumber.getScheduler());
            return threadPoolTaskScheduler;
        }
    }
}
