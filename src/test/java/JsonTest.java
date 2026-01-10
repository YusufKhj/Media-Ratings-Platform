import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Map;

public class JsonTest {


    @Test
    void generateJson() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        var json = Map.of("message",  "Added to favorites", "mediaIdResponse", "test");


        String value = objectMapper.writeValueAsString(json);

        System.out.println(value);


    }
}
