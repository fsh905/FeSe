package bid.fese;

/**
 * Created by feng_ on 2016/11/28.
 * launcher the server
 */
public class Booter {

    public static void main(String[] args) {
        FeServer server = new FeServer();
        server.start(8080);
        while (true);
    }

}
