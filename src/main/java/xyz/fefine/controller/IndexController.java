package xyz.fefine.controller;

import bid.fese.entity.SeResponse;
import xyz.fefine.annotation.Path;
import xyz.fefine.annotation.RequestParam;

/**
 * Created by feng_sh on 17-5-31.
 *
 */
public class IndexController {

    @Path("/index")
    public void index(SeResponse response) {
        response.getCookies().set("name", "feng");
        response.getCookies().set("sex", "nan");
        response.getPrintWriter().print(("<h2>index</h2>"));
        response.getPrintWriter().flush();
        response.flush();
    }

    @Path("/page/{num}")
    public void pageShow(@RequestParam("num") int num, SeResponse response) {
        String name = response.getCookies().get("name");
        response.getPrintWriter().print(("<h2>this is page "+ num + " and the name is " + name + "</h2>"));
        response.getPrintWriter().flush();
        response.flush();
    }

}
