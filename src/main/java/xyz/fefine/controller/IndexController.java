package xyz.fefine.controller;

import bid.fese.entity.SeRequest;
import bid.fese.entity.SeResponse;
import xyz.fefine.annotation.Path;
import xyz.fefine.annotation.RequestParam;

import java.io.IOException;

/**
 * Created by feng_sh on 17-5-31.
 *
 */
public class IndexController {

    @Path("/index")
    public void index(SeResponse response) {
        try {
            response.getOutStream().write("<h2>this is index</h2>".getBytes());
        } catch (IOException e) {
            System.out.println("index show error");
            e.printStackTrace();
        }
    }

    @Path("/page/{num}")
    public void pageShow(@RequestParam("num") int num, SeResponse response) {
        try {
            response.getOutStream().write(("<h2>this is page "+ num + "</h2>").getBytes());
        } catch (IOException e) {
            System.out.println("page show error");
            e.printStackTrace();
        }
    }

}
