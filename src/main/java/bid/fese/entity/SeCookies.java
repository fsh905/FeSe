package bid.fese.entity;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by feng_ on 2016/12/8.
 * cookies
 */
public class SeCookies {
    private Map<String, String> cookies;
    private String str;

    public SeCookies(String str) {
        this.str = str;
        cookies = new HashMap<>();
        parse();
    }

    /**
     * 添加的时候进行解析
     */
    private void parse() {
        if (str == null) {
            return;
        }
        String[] kvs = str.split(";");
        for (String kv : kvs) {
            String[] ks = kv.split(":");
            cookies.put(ks[0], ks[1]);
        }
    }

    public String get(String name) {
        return cookies.get(name);
    }

    public void set(String name, String value) {
        cookies.put(name, value);
    }

}
