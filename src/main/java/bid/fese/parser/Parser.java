package bid.fese.parser;

/**
 * Created by Feng on 2/9/2017.
 * 解析request的父类，所有parser必须继承此
 */
public abstract class Parser {
    /**
     * 解析header的位置
     */
    private int position;

    /**
     * 这里使用自动注入（IOC)
     */
    private Parser nextParser;

    /**
     * 解析内容的主方法，必须实现，（返回值未定）
     * todo 确定返回值
     */
    abstract void parse();

    /**
     * 完成解析之后调用下一parse或者restlet <br>
     * <b>最后一个parser必须重写此方法</b>
     */
    void finishParse(){
        // TODO 确定如何执行
        nextParser.parse();
    }


}
