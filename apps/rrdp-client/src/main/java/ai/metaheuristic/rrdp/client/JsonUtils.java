package ai.metaheuristic.rrdp.client;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * @author Sergio Lissner
 * Date: 6/27/2022
 * Time: 4:08 PM
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
