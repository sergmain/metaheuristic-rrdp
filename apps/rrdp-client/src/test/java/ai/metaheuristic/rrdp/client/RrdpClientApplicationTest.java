package ai.metaheuristic.rrdp.client;

import org.apache.commons.cli.AlreadySelectedException;
import org.apache.commons.cli.MissingArgumentException;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.ParseException;
import org.junit.jupiter.api.Test;

import static ai.metaheuristic.rrdp.client.RrdpClientApplication.parseArgs;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Sergio Lissner
 * Date: 10/7/2022
 * Time: 4:41 PM
 */
public class RrdpClientApplicationTest {

    @Test
    public void test_55() throws ParseException {
        assertEquals("test", parseArgs("--code", "test").getOptionValue("code"));

        assertFalse(parseArgs("--code", "test").hasOption("verify"));
        assertTrue(parseArgs("--code", "test", "--clean").hasOption("clean"));
        assertFalse(parseArgs("--code", "test", "--verify").hasOption("clean"));
        assertTrue(parseArgs("--code", "test", "--verify").hasOption("verify"));

        assertThrows(AlreadySelectedException.class, ()->parseArgs("--code", "test", "--clean", "--verify").hasOption("clean"));
        assertThrows(MissingArgumentException.class, ()->parseArgs("--code").getOptionValue("code"));
        assertThrows(MissingOptionException.class, RrdpClientApplication::parseArgs);
    }

    @Test
    public void test_56() throws ParseException {
        assertEquals("test", parseArgs("-code", "test").getOptionValue("code"));

        assertFalse(parseArgs("-code", "test").hasOption("verify"));
        assertTrue(parseArgs("-code", "test", "-clean").hasOption("clean"));
        assertFalse(parseArgs("-code", "test", "-verify").hasOption("clean"));
        assertTrue(parseArgs("-code", "test", "-verify").hasOption("verify"));

        assertThrows(AlreadySelectedException.class, ()->parseArgs("-code", "test", "-clean", "-verify").hasOption("clean"));
        assertThrows(MissingArgumentException.class, ()->parseArgs("-code").getOptionValue("code"));
        assertThrows(MissingOptionException.class, RrdpClientApplication::parseArgs);
    }
}
