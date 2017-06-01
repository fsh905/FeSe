package bid.fese.prepare;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;

import java.io.IOException;

/**
 * Created by feng_sh on 17-5-31.
 */
public class JsonTest {

    @Test
    public void test() {
        ObjectMapper objectMapper = new ObjectMapper();
        Data data = new Data();
        data.setName("feng");
        data.setAge(10);
        try {
            String json = objectMapper.writeValueAsString(data);
            System.out.println(json);
            data.setName(null);
            json = objectMapper.writeValueAsString(data);
            System.out.println(json);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    class Data{
        private String name;
        private int age;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }
    }

}
