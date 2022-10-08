package ai.metaheuristic.rrdp_client;

import org.apache.commons.cli.AlreadySelectedException;
import org.apache.commons.cli.MissingArgumentException;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.ParseException;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static ai.metaheuristic.rrdp_client.RrdpClientApplication.*;
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
        assertTrue(parseArgs("--code", "test", "--verify").hasOption("verify"));
        assertFalse(parseArgs("--code", "test", "--full-verify").hasOption("verify"));
        assertTrue(parseArgs("--code", "test", "--full-verify").hasOption("full-verify"));

        assertThrows(AlreadySelectedException.class, ()->parseArgs("--code", "test", "--verify", "--full-verify").hasOption("verify"));
        assertThrows(MissingArgumentException.class, ()->parseArgs("--code").getOptionValue("code"));
        assertThrows(MissingOptionException.class, RrdpClientApplication::parseArgs);
    }
}
