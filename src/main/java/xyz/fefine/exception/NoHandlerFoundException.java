package xyz.fefine.exception;

/**
 * Created by feng_sh on 17-5-31.
 * 找不到合适的handler
 */
public class NoHandlerFoundException extends Exception{
    @Override
    public void printStackTrace() {
        System.err.println("not found handler");
        super.printStackTrace();
    }
}
