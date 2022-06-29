package ai.metaheuristic.rrdp;

import lombok.SneakyThrows;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * @author Sergio Lissner
 * Date: 6/28/2022
 * Time: 3:29 PM
 */
public class RrdpCommonUtils {

    public static boolean isAny(String s, String ... any) {
        for (String one : any) {
            if (s.equals(one)) {
                return true;
            }
        }
        return false;
    }

    @SneakyThrows
    public static String resourceAsString(String resource) {
        try (InputStream is = RrdpCommonUtils.class.getResourceAsStream(resource)) {
            if (is==null) {
                throw new IllegalArgumentException("Resource not fount: " + resource);
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] bytes = new byte[256];
            int count;
            while ((count=is.read(bytes))!=-1) {
                baos.write(bytes, 0, count);
            }
            return baos.toString(StandardCharsets.UTF_8);
        }
    }


}
