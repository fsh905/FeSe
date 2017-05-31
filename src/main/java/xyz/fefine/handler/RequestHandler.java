package xyz.fefine.handler;

import bid.fese.entity.SeRequest;
import bid.fese.entity.SeResponse;
import xyz.fefine.entity.Interceptor;
import xyz.fefine.entity.MethodInterceptor;
import xyz.fefine.exception.NoHandlerFoundException;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 	在servlet初始化是进行存储List<RequestHandler> ,
 * 对请求的url进行适配，查找符合的requesHandler，requestHandler
 * 进行执行适配的方法
 * @ClassName: RequestHandler
 * @Description: TODO
 * @author feng_
 * @date 2016年3月31日
 *
 */
public class RequestHandler implements Comparable{

    /**
     * 包含信息：
     */
    /**
     * 原url信息
     */
    private String url;

    /**
     * url正则
     */
    private Pattern urlPattern;

    /**
     * 类名
     */
    private String className;

    /**
     * 参数信息
     */
    private String paramsInfo[];

    /**
     * 传递的参数
     */
    private Object[] objs;
    /**
     * 方法
     */
    private Method method;

    /**
     * 方法所在的位置
     */
    private int methodLocation;

    /**
     * 参数类型
     */
//	private Type[] paramsType;

    /**
     * 返回值类型
     */
//	private Type returnType;

    /**
     * 请求方式
     */
    private String requestMethod;

    /**
     * request和response的存放位置
     */
    private Map<String,Integer> reqAndRespLocaltion;


    public Map<String, Integer> getReqAndRespLocaltion() {
        return reqAndRespLocaltion;
    }

    public void setReqAndRespLocaltion(Map<String, Integer> reqAndRespLocaltion) {
        this.reqAndRespLocaltion = reqAndRespLocaltion;
    }

    public String getUrl() {
        return url;
    }




    public void setUrl(String url) {
        this.url = url;
    }




    public Pattern getUrlPattern() {
        return urlPattern;
    }




    public void setUrlPattern(Pattern urlPattern) {
        this.urlPattern = urlPattern;
    }




    public String getClassName() {
        return className;
    }




    public void setClassName(String className) {
        this.className = className;
    }




    public String[] getParamsInfo() {
        return paramsInfo;
    }




    public void setParamsInfo(String[] paramsInfo) {
        this.paramsInfo = paramsInfo;
    }




    public Object[] getObjs() {
        return objs;
    }




    public void setObjs(Object[] objs) {
        this.objs = objs;
    }




    public Method getMethod() {
        return method;
    }




    public void setMethod(Method method) {
        this.method = method;
    }




    public int getMethodLocation() {
        return methodLocation;
    }




    public void setMethodLocation(int methodLocation) {
        this.methodLocation = methodLocation;
    }



    public String getRequestMethod() {
        return requestMethod;
    }




    public void setRequestMethod(String requestMethod) {
        this.requestMethod = requestMethod;
    }




    /**
     * 是否符合
     * @Title: matcher
     * @Description: TODO
     * @param @param url
     * @param @return
     * @return boolean
     * @throws
     */
    public boolean matcher(String url){
//		return url.matches(urlPattern);

        return urlPattern.matcher(url).find();

    }


    /**
     * 调用方法
     * @param req
     * @param resp
     * @param interceptor 自定义的拦截器
     * @throws NoHandlerFoundException
     */
    public void invokeMethod(SeRequest req, SeResponse resp, Interceptor interceptor) throws NoHandlerFoundException {

        this.setObjs(createObjs(req,resp));

        MethodInterceptor proxy = null;
        try {
            //这里的拦截器默认为空，需要进行修改
            proxy = new MethodInterceptor(this, interceptor, req, resp);
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        try {
            proxy.invokeMethod();
        } catch (Throwable e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


    }

    /**
     * 填充方法参数
     * @param req
     * @param resp
     * @return
     */
    private Object[] createObjs(SeRequest req, SeResponse resp){

        String[] infos = this.getParamsInfo();

        Object[] objs = new Object[this.method.getParameterCount()];

        //参数前两个必须为request,response
        //这里需要修改
        if(!infos[infos.length-2] .equals("-1"))
            objs[Integer.parseInt(infos[infos.length-2])] = req;
        if(!infos[infos.length-1] .equals("-1"))
            objs[Integer.parseInt(infos[infos.length-1])] = resp;
//		objs[1] = resp;

        //进行变量注入
        String reqPath = req.getUrl();
        reqPath = reqPath.substring(0, reqPath.lastIndexOf("."));
//		method.getParameters();
//		proxy.
//		Parameter[] ps = method.getParameters();
        String[] path = reqPath.split("/");
        //减去包含req，reesp的
        for (int i=0;i< infos.length-2;i++) {

            String info = infos[i];
            //获取不同的参数
            String[] t = info.split("}");


            //获取url中的值,t[1] 为在path中的位置
            String value = path[Integer.parseInt(t[1])];

            //将url中的值传递给参数 t[2]为在objs中的位置
            objs[Integer.parseInt(t[2])] = value;

        }

        return objs;
    }


    /**
     * 重写比较方法，便于二叉搜索树进行比较
     * 这里只比较urlpattern
     * @param o
     * @return
     */
    @Override
    public int compareTo(Object o) {
        String u1 = this.getUrl();
        String u2 = ((RequestHandler)o).getUrl().toString();
        int lim = Math.min(u1.length(),u2.length());
        char[] v1 = u1.toCharArray();
        char[] v2 = u2.toCharArray();

        int k = 0;
        while (k < lim){

            char c1 = v1[k];
            char c2 = v2[k];
            if(c1 == '{' || c2 =='{')
                break;
            if (c1 != c2) {

                return c1 - c2;
            }
            k++;
        }
        //比较长度不合理，应比较“/”数目

        int l1 = 0,l2 = 0;

        k = u1.length();
        while ((--k)!= -1){
            if(v1[k] == '/')
                l1 ++;
        }

        k = u2.length();
        while ((--k)!= -1){
            if(v2[k] == '/')
                l2 ++;
        }
        return l1 - l2;
    }
}

