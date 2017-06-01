package xyz.fefine.handler;

import bid.fese.entity.SeHeader;
import bid.fese.entity.SeRequest;
import bid.fese.entity.SeResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import xyz.fefine.entity.Interceptor;
import xyz.fefine.entity.MethodInterceptor;
import xyz.fefine.entity.TYPE;
import xyz.fefine.exception.NoHandlerFoundException;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 在servlet初始化是进行存储List<RequestHandler> ,
 * 对请求的url进行适配，查找符合的requesHandler，requestHandler
 * 进行执行适配的方法
 *
 * @author feng_
 */
public class RequestHandler implements Comparable {

    private static final Logger logger = LogManager.getLogger(RequestHandler.class);
    private static final ObjectMapper om = new ObjectMapper();
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
    private Object paramsInfo[][];

    /**
     * 传递的参数
     */
    private Object[] objs;
    /**
     * 方法
     */
    private Method method;

    // 是否将返回值转化为json， 并传递给response
    private boolean toJson = false;

    /**
     * 请求方式
     */
    private String requestMethod;


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


    public Object[][] getParamsInfo() {
        return paramsInfo;
    }


    public void setParamsInfo(Object[][] paramsInfo) {
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

    public String getRequestMethod() {
        return requestMethod;
    }

    public void setRequestMethod(String requestMethod) {
        this.requestMethod = requestMethod;
    }

    public boolean isToJson() {
        return toJson;
    }

    public void setToJson(boolean toJson) {
        this.toJson = toJson;
    }

    /**
     * 是否符合
     */
    public boolean matcher(String url) {
        return urlPattern.matcher(url).find();
    }


    /**
     * 调用方法
     *
     * @param interceptor 自定义的拦截器
     * @throws NoHandlerFoundException 未找到handler
     */
    public void invokeMethod(SeRequest req, SeResponse resp, Interceptor interceptor) throws NoHandlerFoundException {
        this.setObjs(createObjs(req, resp));
        MethodInterceptor proxy = null;
        try {
            proxy = new MethodInterceptor(this, interceptor, req, resp);
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        if (proxy != null) {
            try {
                // todo 这里对返回值进行判断
                Object res = proxy.invokeMethod();
                if (this.isToJson()) {
                    // 将json数据进行传输
                    String json = om.writeValueAsString(res);
                    resp.getPrintWriter().write(json);
                    resp.getPrintWriter().flush();
                    resp.getHeader().setContentType("application/json");
                    resp.flush();
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 填充方法参数
     */
    private Object[] createObjs(SeRequest req, SeResponse resp) {
        Object[][] infos = this.getParamsInfo();
        Object[] objs = new Object[this.method.getParameterCount()];
        //参数前两个必须为request,response
        // request
        if (infos[infos.length - 2][1] != null) {
            objs[(int) infos[infos.length - 2][1]] = req;
        }
        // resp
        if (infos[infos.length - 1][1] != null) {
            objs[(int) infos[infos.length - 1][1]] = resp;
        }
        //进行变量注入
        String reqPath = req.getUrl();
        reqPath = reqPath.substring(0, reqPath.lastIndexOf("."));
        String[] path = reqPath.split("/");
        //减去包含req，reesp的
        for (int i = 0; i < infos.length - 2; i++) {
            Object[] info = infos[i];
            int index = (int) info[1];
            //获取url中的值,t[1] 为在path中的位置
            String value = path[index];
            Object arg = null;
            // 参数类型转换，　仅支持基本数据类型
            //将url中的值传递给参数 t[2]为在objs中的位置
            switch ((TYPE) info[3]) {
                case BYTE: objs[(int) info[2]] = Byte.parseByte(value); break;
                case CHAR: objs[(int) info[2]] = (char)Integer.parseInt(value); break;
                case SHORT: objs[(int) info[2]] = Short.parseShort(value); break;
                case INT: objs[(int) info[2]] = Integer.parseInt(value); break;
                case LONG: objs[(int) info[2]] = Long.parseLong(value); break;
                case FLOAT: objs[(int) info[2]] = Float.parseFloat(value); break;
                case DOUBLE: objs[(int) info[2]] = Double.parseDouble(value); break;
                default: objs[(int) info[2]] = value;
            }
        }
        logger.debug(url + " the objs:" + Arrays.toString(objs));
        return objs;
    }


    /**
     * 重写比较方法，便于二叉搜索树进行比较
     * 这里只比较urlpattern
     */
    @Override
    public int compareTo(Object o) {
        String u1 = this.getUrl();
        String u2 = ((RequestHandler) o).getUrl();
        int lim = Math.min(u1.length(), u2.length());
        char[] v1 = u1.toCharArray();
        char[] v2 = u2.toCharArray();
        int k = 0;
        while (k < lim) {
            char c1 = v1[k];
            char c2 = v2[k];
            if (c1 == '{' || c2 == '{')
                break;
            if (c1 != c2) {
                return c1 - c2;
            }
            k++;
        }
        //比较长度不合理，应比较“/”数目
        int l1 = 0, l2 = 0;
        k = u1.length();
        while ((--k) != -1) {
            if (v1[k] == '/')
                l1++;
        }
        k = u2.length();
        while ((--k) != -1) {
            if (v2[k] == '/')
                l2++;
        }
        return l1 - l2;
    }
}

