package bid.fese;

import bid.fese.common.ApplicationContext;
import bid.fese.common.Constants;
import bid.fese.handler.RequestHandler;
import bid.fese.handler.RequestHandlers;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Created by feng_ on 2016/11/28.
 * launcher the server
 */
public class Booter {

    private static final Logger logger = LogManager.getLogger(Booter.class);

    public static void main(String[] args) {
        Booter booter = new Booter();
        ApplicationContext.put(Constants.CONFIG_SERVER_PORT, 8080);
        booter.config();
        FeServer server = new FeServer((int) ApplicationContext.get(Constants.CONFIG_SERVER_PORT));
        int cpu = Runtime.getRuntime().availableProcessors();
        for (int i = 0; i < cpu; i++) {
            RequestHandler handler = new RequestHandler();
            RequestHandlers.addRequestHandler(handler);
            new Thread(handler, "handler-" + i).start();
        }
        new Thread(server, "server").start();
    }

    private void config() {
        String classPath = ApplicationContext.getClassPath();
        Map<String, String> props = null;
        try {
            props = getProp(classPath + Constants.CONFIGURE_PATH);
        } catch (IOException e) {
            logger.error("load properties error", e);
        }
        if (props != null) {
            for(String k : props.keySet()) {
                switch (k) {
                    case Constants.CONFIG_SERVER_PORT :
                        ApplicationContext.put(Constants.CONFIG_SERVER_PORT, Integer.parseInt(props.get(k)));
                        break;
                    case Constants.CONFIG_STATIC_RESOURCE_PATH :
                        // /?/static//?\\S+
                        // /?static/?\\S+
//                        ApplicationContext.put(Constants.CONFIG_STATIC_RESOURCE_PATH, Pattern.compile("/?" + props.get(k) + "/?\\S+"));
                        ApplicationContext.put(Constants.CONFIG_STATIC_RESOURCE_PATH, props.get(k));
                        break;
                    case Constants.CONFIG_CONTROLLER_PATH :
                        ApplicationContext.put(Constants.CONFIG_CONTROLLER_PATH, props.get(k));
                        break;
                    case Constants.CONFIG_REQUEST_POSTFIX :
                        ApplicationContext.put(Constants.CONFIG_REQUEST_POSTFIX, props.get(k).toLowerCase().toCharArray());
                        break;
                    case Constants.CONFIG_INDEX :
                        ApplicationContext.put(Constants.CONFIG_INDEX, props.get(k));
                        break;
                    case Constants.CONFIG_REQUEST_HANDLER :
                        ApplicationContext.put(Constants.CONFIG_REQUEST_HANDLER, props.get(k));
                        break;
                    case Constants.CONFIG_PAGE_404 :
                        ApplicationContext.put(Constants.CONFIG_PAGE_404, props.get(k));
                        break;
                    case Constants.CONFIG_PAGE_500:
                        ApplicationContext.put(Constants.CONFIG_PAGE_500, props.get(k));
                        break;
                    default:
                        logger.error("error config properties key:" + k);
                }
            }
        }
    }

    private Map<String, String> getProp(String configFilePath) throws IOException {
        Map<String, String> props = new HashMap<>();
        BufferedReader reader = new BufferedReader(new FileReader(new File(configFilePath)));
        String line = null;
        while ((line = reader.readLine()) != null) {
            if (line.length() < 3 || line.trim().charAt(0) == '#') {
                continue;
            }
            String[] kv = line.trim().split("=");
            logger.debug("prop: k:"+ kv[0] + "-\t" + kv[1] + "-");
            props.put(kv[0], kv[1]);
        }
        reader.close();
        return props;
    }

}
