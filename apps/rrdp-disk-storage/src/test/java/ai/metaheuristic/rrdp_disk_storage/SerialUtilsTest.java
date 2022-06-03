package ai.metaheuristic.rrdp_disk_storage;

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

        assertNull(SerialUtils.getSerialFile(tempPath));
        String serial = SerialUtils.getSerial(tempPath);
        assertNull(serial);

        String serial1 = SerialUtils.persistSerial(tempPath, 1, LocalDate::now);
        assertNotNull(serial1);
        assertDoesNotThrow(()-> Integer.parseInt(serial1));

        Path serialPath1 = SerialUtils.getSerialFile(tempPath);
        assertNotNull(serialPath1);


        String serial11 = SerialUtils.getSerial(tempPath);
        assertNotNull(serial11);
        assertEquals(1, Integer.parseInt(serial11));


        String serial2 = SerialUtils.persistSerial(tempPath, 2, LocalDate::now);
        assertNotNull(serial2);
        assertEquals(2, Integer.parseInt(serial2));

        Path serialPath2 = SerialUtils.getSerialFile(tempPath);
        assertNotNull(serialPath2);

        String serial3 = SerialUtils.persistSerial(tempPath, 3, LocalDate::now);
        assertNotNull(serial3);
        assertEquals(3, Integer.parseInt(serial3));

        Path serialPath3 = SerialUtils.getSerialFile(tempPath);
        assertNotNull(serialPath3);

        assertNotEquals(serialPath1, serialPath2);
        assertNotEquals(serialPath2, serialPath3);
        assertNotEquals(serialPath1, serialPath3);

    }

}
