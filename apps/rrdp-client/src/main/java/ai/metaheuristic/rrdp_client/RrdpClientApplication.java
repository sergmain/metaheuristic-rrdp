package ai.metaheuristic.rrdp_client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author Sergio Lissner
 * Date: 6/27/2022
 * Time: 12:05 AM
 */
@SpringBootApplication
@Slf4j
@RequiredArgsConstructor
public class RrdpClientApplication implements CommandLineRunner {

    public final ContentService contentService;

    public static void main(String[] args) {
        final String encoding = System.getProperty("file.encoding");
        if (!StringUtils.equalsAnyIgnoreCase(encoding, "utf8", "utf-8")) {
            System.out.println("Must be run with -Dfile.encoding=UTF-8, actual file.encoding: " + encoding);
            System.exit(-1);
        }
        SpringApplication.run(RrdpClientApplication.class, args);
    }

    @Override
    public void run(String... args) {
        contentService.process();
    }
}
