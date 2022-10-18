package ai.metaheuristic.rrdp_client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import java.io.IOException;

/**
 * @author Sergio Lissner
 * Date: 6/27/2022
 * Time: 12:05 AM
 */
@SpringBootApplication
@Slf4j
@RequiredArgsConstructor
public class RrdpClientApplication implements CommandLineRunner {

    private final ApplicationContext appCtx;
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
        CommandLine cmd;
        try {
            cmd = parseArgs(args);
        }
        catch (ParseException e) {
            System.out.println("Error: " + e.getMessage());
            System.exit(SpringApplication.exit(appCtx, () -> -1));
            return;
        }

        String code = cmd.getOptionValue("code");
        if (cmd.hasOption("clean") || cmd.hasOption("verify")) {
            try {
                contentService.cleanAndVerify(code, cmd.hasOption("verify"));
            }
            catch (IOException e) {
                e.printStackTrace();
                System.exit(SpringApplication.exit(appCtx, () -> -1));
                return;
            }
        }
        else {
            contentService.process(code);
        }

        System.exit(SpringApplication.exit(appCtx, () -> 0));
    }

    public static CommandLine parseArgs(String... args) throws ParseException {
        Options options = new Options();
        Option codeOption = new Option("code", "code", true, "Code for sync");
        codeOption.setRequired(true);
        options.addOption(codeOption);
        Option verifyOption = new Option("clean", "clean", false, "Clean");
        Option fullVerifyOption = new Option("verify", "verify", false, "Full verify");
        OptionGroup group = new OptionGroup();
        group.addOption(verifyOption);
        group.addOption(fullVerifyOption);
        options.addOptionGroup(group);

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);
        return cmd;
    }
}
