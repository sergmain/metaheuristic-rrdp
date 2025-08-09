package ai.metaheuristic.rrdp;

import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * @author Sergio Lissner
 * Date: 6/4/2022
 * Time: 2:00 AM
 */
public class ChecksumTest {

    @Test
    public void test() {
        String md51 = DigestUtils.md5Hex("1");
        System.out.println("md51 = " + md51);
        String md52 = DigestUtils.md5Hex("2");
        System.out.println("md52 = " + md52);
        assertNotEquals(md51, md52);

    }
}
