package ai.metaheuristic.rrdp.disk_storage;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * @author Sergio Lissner
 * Date: 6/3/2022
 * Time: 8:45 PM
 */
public class JsonUtils {

    private static final ObjectMapper mapper;
    static {
        ObjectMapper m = new ObjectMapper();
        m.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        m.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper = m;
    }

    public static ObjectMapper getMapper() {
        return mapper;
    }

}
