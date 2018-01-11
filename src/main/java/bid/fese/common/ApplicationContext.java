package bid.fese.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

/**
 * Created by feng_sh on 5/23/2017.
 * 存放全局的一些参数, 单例模式
 */
public class ApplicationContext {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationContext.class);
    private static final Properties properties = new Properties();

    static {
        String configPath = System.getProperty(Constants.CONFIGURE_PATH, getClasspath() + "server.properties");
        properties.put(Constants.CONFIGURE_PATH, configPath);
        logger.debug("config path: " + configPath);
        try {
            InputStream in = new FileInputStream(configPath);
            properties.load(in);
        } catch (IOException e) {
            logger.error("load properties file error", e);
        }
        for (Object key : properties.keySet()) {
            Object value = properties.get(key);
            logger.info(String.format("[config.properties]-> %s : %s", key, value));
        }
    }

    private ApplicationContext() {
    }

    public static String getClasspath() {
        String classPath = properties.getProperty(Constants.CLASS_PATH);
        if (classPath == null) {
            URL url = ClassLoader.class.getResource("/");
            if (url != null) {
                classPath = url.getPath();
                properties.put(Constants.CLASS_PATH, classPath);
            }
            return classPath;
        }
        return classPath;
    }

    public static ApplicationContext getInstance() {
        return Inner.application;
    }

    public String getString(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    public String getString(String key) {
        return properties.getProperty(key);
    }

    public Object getObject(String key) {
        return properties.get(key);
    }

    public Object getObject(String key, String defaultObject) {
        return properties.getOrDefault(key, defaultObject);
    }

    public void set(String key, String value) {
        properties.setProperty(key, value);
    }

    private static class Inner {
        private final static ApplicationContext application = new ApplicationContext();
    }

}
