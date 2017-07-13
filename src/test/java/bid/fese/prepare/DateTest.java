package bid.fese.prepare;

import org.junit.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Created by feng_sh on 17-6-3.
 */
public class DateTest {

    @Test
    public void test() {
        LocalDateTime dateTime = LocalDateTime.now();
        ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneId.of(ZoneId.SHORT_IDS.get("CTT")));
        System.out.println(dateTime);
        System.out.println(zonedDateTime);
//        System.out.println(dateTime.format(DateTimeFormatter.RFC_1123_DATE_TIME));
        System.out.println(zonedDateTime.format(DateTimeFormatter.RFC_1123_DATE_TIME));
//        System.out.println(dateTime.format(DateTimeFormatter.BASIC_ISO_DATE));
    }

}
