package xyz.fefine.handler;


import bid.fese.entity.SeRequest;
import bid.fese.entity.SeResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import xyz.fefine.annotation.JsonData;
import xyz.fefine.annotation.Path;
import xyz.fefine.annotation.RequestParam;
import xyz.fefine.entity.DefaultInterceptor;
import xyz.fefine.entity.Interceptor;
import xyz.fefine.entity.TYPE;
import xyz.fefine.exception.NoHandlerFoundException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;
import java.util.regex.Pattern;

/**
 * 请求协助类
 * 用于请求处理
 */
public class RequestHelper {

    private static final Logger logger = LogManager.getLogger(RequestHelper.class);
    private RequestSearchTree<RequestHandler> handlers;
    //拦截器
    private Interceptor interceptor;

    public Interceptor getInterceptor() {
        return interceptor;
    }

    //ArrayList 不是线程安全，但是这里只在初始化时进行添加，其后只有读取
    //为了加快读取速度，因此采用需要根据正则 自定义一种可以进行快速查找的数据结构，加快响应速度
    //目前采用二叉搜索树
    public RequestHelper() {
        handlers = new RequestSearchTree<>();
    }


    /**
     * 获取真实的请求方式get,post,put,delete,返回的均为小写
     *
     * @param req 请求
     * @return 请求方式
     */
    public String getRealRequestMathod(SeRequest req) {

        if (req.getMethod() == SeRequest.METHOD.GET) {
            logger.info("request method is GET");
            return "get";
        }

        String method = req.getParameter("_method");

        if (method == null) {
            logger.info("request method is POST");
            return "post";
        }
        logger.info("request method is " + method);
        //返回小写
        return method.toLowerCase();
    }

    /**
     * 获取到适合的请求处理器，找不到返回null
     *
     * @param url 通过url获取handler
     * @return handler
     */
    public RequestHandler findRequestHandler(String url) throws NoHandlerFoundException {

        logger.info("request url is " + url.substring(0, url.lastIndexOf(".")));

        //去除请求的后缀，要不然带后缀的请求找不到合适的handler
        //这里查找比较浪费时间
        url = url.substring(0, url.lastIndexOf("."));
        RequestHandler rh = handlers.find(url);
        if (rh == null)
            throw new NoHandlerFoundException();
        logger.debug("get request handler:" + rh.getClassName());
        return rh;
    }

    /**
     * 初始化requestHandler
     * @param packagePath 包路径
     */
    public void initRequestHandler(String packagePath) {
        Document doc = getDoc(packagePath);
        String[] packages = getPackageName(doc);
        this.interceptor = initInterceptor(doc);

        if (packages == null) {
            logger.error("package is null");
            return;
        }
        for (String pkName : packages) {
            scanPackage(pkName);
        }
    }

    /**
     * 生成Interceptor，当没有自定义时使用默认
     */
    private Interceptor initInterceptor(Document doc) {

        //获取根
        Element ele = doc.getDocumentElement();
        //要扫描的包名 packages/package
        NodeList nls = ele.getElementsByTagName("interceptor");
        if (nls.getLength() < 1) {
            return new DefaultInterceptor();
        } else {
            Node node = nls.item(0);
            NamedNodeMap nnm = node.getAttributes();
            String claName = nnm.getNamedItem("class").getNodeValue();
            try {
                logger.info("Scan Inteceptor class is " + claName);
                return (Interceptor) Class.forName(claName).newInstance();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        logger.info("use default interceptor");
        return new DefaultInterceptor();
    }


    /**
     * 获取扫描包的路径
     * @return 包名
     */
    private String[] getPackageName(Document doc) {

        //获取根
        Element ele = doc.getDocumentElement();

        //要扫描的包名 packages/package
        NodeList nls = ele.getElementsByTagName("packages");
        Node pks = null;
        if (nls.getLength() < 1) {
            logger.error("Not found scan packages");
            return null;
        } else {
            pks = nls.item(0);
        }

        NodeList nl = pks.getChildNodes();

        String[] scPkName = new String[nl.getLength()];

        for (int i = 0; i < nl.getLength(); i++) {

            Node no = nl.item(i);

            scPkName[i] = no.getTextContent();

            //scanPackage(sacnPkName);
            logger.info("scanPackageName:" + no.getTextContent());

        }


        return scPkName;

    }

    //获取解析好的doc
    private Document getDoc(String packagePath) {
        DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();
        DocumentBuilder build = null;
        try {
            build = fact.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            logger.error("ParserConfigurationException");
            e.printStackTrace();
        }
        Document doc = null;
        File file = null;
        try {
            file = new File(packagePath);
            doc = build.parse(file);
        } catch (IOException | org.xml.sax.SAXException e) {
            logger.error("open the package failed" + file.getAbsolutePath());
            e.printStackTrace();
        }
        return doc;
    }

    /**
     * 扫描包下面所有类
     *
     * @param pkName pkName
     */
    private void scanPackage(String pkName) {
        String path = pkName.replace(".", "/");
        URL url = Thread.currentThread().getContextClassLoader().getResource(path);
        try {
            if (url != null && url.toString().startsWith("file")) {
                String filePath = URLDecoder.decode(url.getFile(), "utf-8");
                File dir = new File(filePath);
                List<File> fileList = new ArrayList<>();
                fetchFileList(dir, fileList);
                for (File f : fileList) {
                    String className = f.getAbsolutePath();
                    if (className.endsWith(".class")) {
                        String nosuffixFileName = className.substring(8 + className.lastIndexOf("classes"), className.indexOf(".class"));
                        className = nosuffixFileName.replaceAll("\\\\", ".");
                        // on unix this is /
                        className = className.replaceAll("/", ".");
                    }
                    logger.info("scanClassName:" + className);
                    //扫描class
                    scanClass(className);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void fetchFileList(File dir, List<File> fileList) {
        if (dir.isDirectory()) {
            File[] fs = dir.listFiles();
            if (fs != null) {
                for (File f : fs) {
                    fetchFileList(f, fileList);
                }
            }
        } else {
            fileList.add(dir);
        }
    }


    /**
     * 扫描类中所有的方法及其注解
     * @param className 类名
     */
    private void scanClass(String className) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        Class<?> cla = Class.forName(className);
        String classAnnoName = "";
        //类是否有path注解
        Path path = cla.getAnnotation(Path.class);
        if (path != null) {
            classAnnoName += path.value();
        }
        //获取所有public方法
        Method[] mes = cla.getDeclaredMethods();
        for (Method m : mes) {
            path = m.getAnnotation(Path.class);
            String methodName = m.getName();
            //继承的方法不带注解
            if (path != null) {
                //每个带注解的method就是一个新的handler
                RequestHandler handler = new RequestHandler();
                handler.setClassName(className);
                handler.setMethod(m);
                JsonData jd = m.getAnnotation(JsonData.class);
                if (jd != null) {
                    // 返回值需要进行json转化
                    if (m.getReturnType() == Void.TYPE) {
                        throw new RuntimeException("the method return object can't be cast to json");
                    }
                    handler.setToJson(true);
                }
                //请求方式
                handler.setRequestMethod(path.requestMethod());
                //注解值
                String methodAnnoName = path.value();
                //参数
                Parameter[] ps = m.getParameters();
                //参数，位置
                Map<String, Integer> indexMap = new HashMap<>();
                //参数，类型
                Map<String, TYPE> typeMap = new Hashtable<>();
                for (int ij = 0; ij < ps.length; ij++) {
                    Parameter p = ps[ij];
                    RequestParam rp = p.getAnnotation(RequestParam.class);
                    TYPE type = null;
                    String paramName = null;
                    if (rp != null) {
                        //参数的名称
                        paramName = rp.value();
                        logger.info("param:" + paramName);
                    }

                    if (p.getType() == SeRequest.class) {
                        paramName = "_request";
                        type = TYPE.STRING;
                    } else if (p.getType() == SeResponse.class) {
                        paramName = "_response";
                        type = TYPE.STRING;
                    } else if (p.getType() == Byte.TYPE) {
                        type = TYPE.BYTE;
                    } else if (p.getType() == Character.TYPE) {
                        type = TYPE.CHAR;
                    } else if (p.getType() == Short.TYPE) {
                        type = TYPE.SHORT;
                    } else if (p.getType() == Integer.TYPE) {
                        type = TYPE.INT;
                    } else if (p.getType() == Long.TYPE) {
                        type = TYPE.LONG;
                    } else if (p.getType() == Float.TYPE) {
                        type = TYPE.FLOAT;
                    } else if (p.getType() == Double.TYPE) {
                        type = TYPE.DOUBLE;
                    } else {
                        type = TYPE.STRING;
                    }
                    indexMap.put(paramName, ij);
                    typeMap.put(paramName, type);
                }

                //保存到list中
                save(className, methodName, classAnnoName, methodAnnoName, indexMap, typeMap, handler);
            }
        }
    }

    /**
     * 将url处理并放入搜索树
     * @param className      类名
     * @param methodName     方法名
     * @param classAnnoName  类注解
     * @param methodAnnoName 方法注解
     * @param map            参数
     * @param handler        处理器
     */
    private void save(String className, String methodName, String classAnnoName, String methodAnnoName, Map<String, Integer> map, Map<String, TYPE> typeMap, RequestHandler handler) {
        String url = classAnnoName + methodAnnoName;
        //将url改造成正则的形式
        //main/{a}/{b} -> main/[^/]+/[^/]+$
        String urlPattern = url.replaceAll("\\{[a-z0-9]+\\}", "[^/]+") + "$";
        logger.info("scanUrl:" + url + "   scanUrlPattern:" + urlPattern);
        handler.setUrl(url);
        handler.setUrlPattern(Pattern.compile(urlPattern));
        //将map放入字符串中
        Object[][] params = paramsLocation(url, map, typeMap);
        //参数的部分信息
        handler.setParamsInfo(params);
        handlers.insert(handler);
    }

    /**
     * 将链接分解并拼接成包含信息的字符串,需要改进
     *
     * @param url 访问链接
     * @param map 参数及其位置
     */
    private Object[][] paramsLocation(String url, Map<String, Integer> map, Map<String, TYPE> typeMap) {
        String[] sta = url.split("/");
        //存放参数,为了防止添加req和resp后溢出，因此+2
        Object[][] res = new Object[sta.length + 2][3];
        int j = 0;
        for (int i = 0; i < sta.length; i++) {
            if (sta[i].matches("\\{.+\\}")) {
                String k = sta[i].substring(1, sta[i].length() - 1);
                //分别为　参数名称，参数在源字符串中的位置，参数在方法的的参数中的位置,参数的类型
                res[j++] = new Object[]{k, i, map.get(k), typeMap.get(k)};
                logger.debug("params:" + Arrays.toString(res[j - 1]));
            }
        }
        //放入req，resp
        // 名称，　位置
        res[j++] = new Object[]{"_request", map.get("_request")};
        res[j++] = new Object[]{"_response", map.get("_response")};


        Object[][] ns = new Object[j][];
        System.arraycopy(res, 0, ns, 0, j);
        return ns;
    }
}

