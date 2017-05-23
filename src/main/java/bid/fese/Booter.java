package bid.fese;

import bid.fese.handler.RequestHandler;
import bid.fese.handler.RequestHandlers;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.logging.LogManager;

/**
 * Created by feng_ on 2016/11/28.
 * launcher the server
 */
public class Booter {

    public static void main(String[] args) {


        FeServer server = new FeServer(8080);
        int cpu = Runtime.getRuntime().availableProcessors();
        for (int i = 0; i < cpu; i++) {
            RequestHandler handler = new RequestHandler();
            RequestHandlers.addRequestHandler(handler);
            new Thread(handler, "handler-" + i).start();
        }

        new Thread(server, "server").start();

    }

}
