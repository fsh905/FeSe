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
import xyz.fefine.annotation.Path;
import xyz.fefine.annotation.RequestParam;
import xyz.fefine.entity.DefaultInterceptor;
import xyz.fefine.entity.Interceptor;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 请求协助类
 * 用于请求处理
 */
public class RequestHelper {

    private Logger LOG = LogManager.getLogger(this.getClass());

    private RequestSearchTree<RequestHandler> handlers;
    //拦截器
    private Interceptor interceptor;


//	private RequestHandler handler;


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
            LOG.info("request method is GET");
            return "get";
        }

        String method = req.getParameter("_method");

        if (method == null) {
            LOG.info( "request method is POST");
            return "post";
        }
        LOG.info( "request method is " + method);
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

        LOG.info( "request url is " + url.substring(0, url.lastIndexOf(".")));

        //去除请求的后缀，要不然带后缀的请求找不到合适的handler
        //这里查找比较浪费时间
        url = url.substring(0, url.lastIndexOf("."));

        RequestHandler rh = handlers.find(url);
        if (rh == null)
            throw new NoHandlerFoundException();
        return rh;
    }

    /**
     * 初始化requestHandler
     *
     * @param packagePath 包路径
     * @return requestHandler
     */
    public void initRequestHandler(String packagePath) {
        Document doc = getDoc(packagePath);
        //contentConfigLocation
        String[] packages = getPackageName(doc);
        this.interceptor = initInterceptor(doc);

        if (packages == null) {
            LOG.error("package is null");
//			return null;
        }

        for (String pkName : packages) {

            scanPackage(pkName);
        }

    }

    /**
     * 生成Interceptor，当没有自定义时使用默认
     *
     * @param doc
     * @return
     */
    private Interceptor initInterceptor(Document doc) {

        //获取根
        Element ele = doc.getDocumentElement();

        //要扫描的包名 packages/package
        NodeList nls = ele.getElementsByTagName("interceptor");
        if (nls.getLength() < 1) {
            return new DefaultInterceptor();
        } else {

            org.w3c.dom.Node node = nls.item(0);
            NamedNodeMap nnm = node.getAttributes();

            String claName = nnm.getNamedItem("class").getNodeValue();

            try {
                LOG.info("Scan Inteceptor class is "+claName);
                return (Interceptor) Class.forName(claName).newInstance();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }


        }
        return new DefaultInterceptor();
    }


    /**
     * 获取扫描包的路径
     * @param doc
     * @return
     */
    private String[] getPackageName(Document doc){

        //获取根
        Element ele = doc.getDocumentElement();

        //要扫描的包名 packages/package
        NodeList nls = ele.getElementsByTagName("packages");
        Node pks = null;
        if(nls.getLength() < 1){
            LOG.error("Not found scan packages");
            return null;
        }else{
            pks = nls.item(0);
        }

        NodeList nl = pks.getChildNodes();

        String[] scPkName = new String[nl.getLength()];

        for (int i = 0; i < nl.getLength(); i++) {

            Node no = nl.item(i);

            scPkName[i] = no.getTextContent();

            //scanPackage(sacnPkName);
            LOG.info("scanPackageName:"+no.getTextContent());

        }


        return scPkName;

    }
    //获取解析好的doc
    private Document getDoc(String packagePath){
        // getpath
        try {
            try {
                packagePath = this.getClass().getClassLoader().getResource("").toURI().getPath()+"//" + packagePath;
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }catch (NullPointerException e){

            LOG.error("get the class loader error !");
            e.printStackTrace();
        }


        DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();

        DocumentBuilder build = null;
        try {
            build = fact.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            // TODO Auto-generated catch block
            LOG.error("ParserConfigurationException");
            e.printStackTrace();
        }


        Document doc = null;
        try {
            doc = build.parse(new File(packagePath));
        } catch ( IOException | org.xml.sax.SAXException e) {
            // TODO Auto-generated catch block
            LOG.error("open the package failed");
            e.printStackTrace();
        }

        return doc;
    }

    /**
     * 扫描包下面所有类
     * @param pkName pkName
     */
    private void scanPackage(String pkName){
        String path = pkName.replace(".", "/");
        URL url = Thread.currentThread().getContextClassLoader().getResource(path);
        try {
            if(url!=null && url.toString().startsWith("file")){
                String filePath = URLDecoder.decode(url.getFile(),"utf-8");
                File dir = new File(filePath);
                List<File> fileList = new ArrayList<File>();
                fetchFileList(dir,fileList);
                for(File f:fileList){
                    String className =  f.getAbsolutePath();
                    if(className.endsWith(".class")){
                        String nosuffixFileName = className.substring(8+className.lastIndexOf("classes"),className.indexOf(".class"));
                        className = nosuffixFileName.replaceAll("\\\\", ".");
                        // on unix this is /
                        className = className.replaceAll("/",".");
                    }
//                    System.out.println("scan class name ："+className);
                    LOG.info( "sacnClassName:"+className);
                    //扫描class
                    scanClass(className);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void  fetchFileList(File dir,List<File> fileList){
        if(dir.isDirectory()){
            for(File f:dir.listFiles()){
                fetchFileList(f,fileList);
            }
        }else{
            fileList.add(dir);
        }
    }


    /**
     * 扫描类中所有的方法及其注解
     * @param className 类名
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws ClassNotFoundException
     */
    private void scanClass(String className) throws InstantiationException, IllegalAccessException, ClassNotFoundException{



        Class<?> cla = Class.forName(className);

        String classAnnoName = "";
        //类是否有path注解
        Path path = cla.getAnnotation(Path.class);
        if(path != null){

            classAnnoName += path.value();

        }

        //获取所有public方法,包含了继承Object的方法
        //warring 仅能获取public方法
        Method[] mes = cla.getMethods();

        for(int i=0;i<mes.length;i++){

            Method m = mes[i];


            path = m.getAnnotation(Path.class);

            String methodName = m.getName();

            //继承的方法不带注解
            if(path != null){

                //每个带注解的method就是一个新的handler
                RequestHandler handler = new RequestHandler();

                handler.setClassName(className);

                handler.setMethod(m);
                //请求方式
                handler.setRequestMethod(path.requestMethod());

                handler.setMethodLocation(i);

/*				handler.setParamsType(Type.getArgumentTypes(m));

				handler.setReturnType(Type.getReturnType(m));*/

                //注解值
                String methodAnnoName = path.value();
                //方法名
//				String requestMethod = path.requestMethod();
                //参数暂时不需要在这里读取，放进Map里面的需要有：urlPattern，url，className，MethodName
                //在拦截器中进行方法的详细操作

                //参数名
                Parameter[] ps = m.getParameters();

                Map<String,Integer> map = new HashMap<String,Integer>();

                for(int ij = 0;ij<ps.length;ij++){

                    Parameter p = ps[ij];

                    RequestParam rp = p.getAnnotation(RequestParam.class);

                    if(rp != null){

                        //参数的名称
                        String paramName = rp.value();
//						System.out.println("param:"+paramName);
                        LOG.info( "param:"+paramName);
                        //参数，位置
                        map.put(paramName, ij);

                    }else {

                        if(p.getType() == SeRequest.class)
                            map.put("_request",ij);
                        else if(p.getType() == SeResponse.class)
                            map.put("_response",ij);

                    }
                }
                //判断map中是否含有req,resp
                if(!map.containsKey("_request"))
                    map.put("_request",-1);
                if(!map.containsKey("_response"))
                    map.put("_response",-1);
                //保存到list中
                save(className, methodName, classAnnoName, methodAnnoName,map,handler);

            }

        }

    }

    /**
     * 将url处理并放入搜索树
     * @param className 类名
     * @param methodName 方法名
     * @param classAnnoName 类注解
     * @param methodAnnoName 方法注解
     * @param map 参数
     * @param handler 处理器
     */
    private void save(String className,String methodName,String classAnnoName,String methodAnnoName,Map<String,Integer> map,RequestHandler handler){

        String url = classAnnoName+methodAnnoName;
        //将url改造成正则的形式
        //main/{a}/{b} -> main/[^/]+/[^/]+$
        String urlPattern = url.replaceAll("\\{[a-z0-9]+\\}", "[^/]+")+"$";

        LOG.info( "scanUrl:"+url+"   scanUrlPattern:"+urlPattern);

        handler.setUrl(url);
        handler.setUrlPattern(Pattern.compile(urlPattern));

        //将map放入字符串中
        String[] params = paramsLocation(url,map);

        //参数的部分信息
        handler.setParamsInfo(params);

        handlers.insert(handler);
    }

    /**
     * 将链接分解并拼接成包含信息的字符串,需要改进
     * @param url 访问链接
     * @param map 参数及其位置
     * @return
     */
    private String[] paramsLocation(String url ,Map<String,Integer> map){

        String[] sta = url.split("/");

        //存放参数,为了防止添加req和resp后溢出，因此+2
        String[] res = new String[sta.length+2];
        int j = 0;
        for (int i = 0; i < sta.length; i++) {
            if(sta[i].matches("\\{.+\\}")){
                String t = "";
                String k = sta[i].substring(1,sta[i].length()-1);
                //第一个为参数名称，参数在源字符串中的位置，参数在方法的的参数中的位置
                t += k + "}"+i + "}" + map.get(k);
                res[j++] = t;
                LOG.info( "params:"+t);
            }
        }
        //放入req，resp
        res[j++] = map.get("_request").toString();
        res[j++] = map.get("_response").toString();

        //sta为新字符串
        sta = new String[j];
        for (int i = 0; i < j; i++)
            //将res中多余的去除
            sta[i] = res[i];


        return sta;

    }



}

