package ai.metaheuristic.rrdp;

import ai.metaheuristic.rrdp.core.paths.SessionPath;
import ai.metaheuristic.rrdp.disk_storage.SerialUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Sergio Lissner
 * Date: 6/3/2022
 * Time: 12:48 AM
 */
public class SerialUtilsTest {

    @Test
    public void test(@TempDir Path tempPath) {

        String session = "123";

        SessionPath sessionPath = new SessionPath(tempPath.resolve(session));

        assertNull(SerialUtils.getSerialFile(sessionPath));
        Integer serial = SerialUtils.getSerial(sessionPath);
        assertNull(serial);

        String serial1 = SerialUtils.persistSerial(sessionPath, 1, LocalDate::now);
        assertNotNull(serial1);
        assertDoesNotThrow(()-> Integer.parseInt(serial1));

        Path serialPath1 = SerialUtils.getSerialFile(sessionPath);
        assertNotNull(serialPath1);


        Integer serial11 = SerialUtils.getSerial(sessionPath);
        assertNotNull(serial11);
        assertEquals(1, serial11);


        String serial2 = SerialUtils.persistSerial(sessionPath, 2, LocalDate::now);
        assertNotNull(serial2);
        assertEquals(2, Integer.parseInt(serial2));

        Path serialPath2 = SerialUtils.getSerialFile(sessionPath);
        assertNotNull(serialPath2);

        String serial3 = SerialUtils.persistSerial(sessionPath, 3, LocalDate::now);
        assertNotNull(serial3);
        assertEquals(3, Integer.parseInt(serial3));

        Path serialPath3 = SerialUtils.getSerialFile(sessionPath);
        assertNotNull(serialPath3);

        assertNotEquals(serialPath1, serialPath2);
        assertNotEquals(serialPath2, serialPath3);
        assertNotEquals(serialPath1, serialPath3);

    }

}
