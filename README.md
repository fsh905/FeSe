# FeSe
### A simple web server 
### Use Java AsynchronousSocketChannel
refer：[nio-httpserver](https://github.com/shenedu/nio-httpserver)
 , [easy-httpserver](https://github.com/NotBadPad/easy-httpserver) 
 
 #### demo:[fefine](http://fefine.xyz)
 #### Feature：
 * Asynchronous
 * Request method (GET, POST)
 * Customize dynamic request handler
 #### Configure:
 All config properties is in /src/main/resources/server.properties
 <pre>
 #static resource path
 static_resource_path=/home/wkzq/html
 
 # server port, default is 8080
 server_port=8889
 
 #dynamic request postfix (ps: index.do)
 request_postfix=.do
 
 #index page
 index=/index.html
 
 #404 page
 page_404=/404.html
 
 #500 page
 page_500=/500.html
 
 # dynamic request handler, must implement DisaptcherHandler, 
 # if request is dynamic, while invoke this class,
 # default is DynamicDispatcherHandler, it will be response 404 NotFound
 dynamic_request_handler=bid.fese.handler.DynamicDispatcherHandler
 </pre>
 #### Sample:
 ~~~~
 // your request handler
 public class MyRequestHandler implements DispatcherHandler {
     @Override
     public void handlerRequest(SeRequest request, SeResponse response) {
         // set response status
         response.getHeader().setStatus(SeHeader.NOT_FOUND_404);
         // set cookie
         response.getCookies().set("name", "fese");
         // to response the request, you must invoke this method
         response.flush();
     }
 }
 ~~~~
 In you **server.properties**
 > request_postfix=.do <br>
 > dynamic_request_handler=com.package.MyRequestHandler <br>
 
 then all url that postfix is .do will send to ***MyRequestHandler***
 #### Install
 * Maven
 
 #### Problems
 * If can't find a img, will response a 404 page
 * Post data not parse