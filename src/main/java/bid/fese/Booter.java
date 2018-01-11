package bid.fese;

import bid.fese.common.ApplicationContext;
import bid.fese.common.Constants;
import bid.fese.handler.RequestHandler;
import bid.fese.handler.RequestHandlers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by feng_ on 2016/11/28.
 * launcher the server
 */
public class Booter {

    private static final Logger logger = LoggerFactory.getLogger(Booter.class);
    private static ApplicationContext context = ApplicationContext.getInstance();
    private final static int DEFAULT_PORT = 8088;

    public static void main(String[] args) {
        boot();
    }

    public static void boot() {
        Booter booter = new Booter();
        RequestHandlers requestHandlers = new RequestHandlers();

        // 初始化一些配置
        requestHandlers.initHandlers();
        FeServer server = new FeServer(
                Integer.parseInt(context.getString(Constants.SERVER_PORT, String.valueOf(DEFAULT_PORT))),
                requestHandlers);
        int cpu = Runtime.getRuntime().availableProcessors();
        for (int i = 0; i < cpu; i++) {
            RequestHandler handler = new RequestHandler(requestHandlers);
            requestHandlers.addRequestHandler(handler);
            new Thread(handler, "handler-" + i).start();
        }
        new Thread(server, "server").start();
    }

}
