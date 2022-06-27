package ai.metaheuristic.rrdp_srv;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;

/**
 * @author Sergio Lissner
 * Date: 6/27/2022
 * Time: 12:19 AM
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
@EnableConfigurationProperties(Globals.class)
@ComponentScan("ai.metaheuristic")
@EnableScheduling
@EnableAsync
public class Config {

    private final Globals globals;

    @Bean
    public ThreadPoolTaskScheduler threadPoolTaskScheduler() {
        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        log.info("Config.threadPoolTaskScheduler() will use {} as a number of threads for an schedulers", globals.threadNumber.getScheduler());
        threadPoolTaskScheduler.setPoolSize(globals.threadNumber.getScheduler());
        return threadPoolTaskScheduler;
    }

    @Configuration
    @Order(1)
    @RequiredArgsConstructor
    public static class RestAuthSecurityConfig extends WebSecurityConfigurerAdapter {

        private final Globals globals;

        @Override
        protected void configure(HttpSecurity http) throws Exception {

            http
                    .antMatcher("/**/**").sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                    .and()
                    .authorizeRequests()
                    .antMatchers("/**/**").permitAll()
                    .and()
                    .antMatcher("/**/**").csrf().disable().headers().cacheControl();

            if (globals.sslRequired) {
                http.requiresChannel().antMatchers("/**").requiresSecure();
            }
        }
    }
}
