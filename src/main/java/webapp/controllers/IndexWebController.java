package webapp.controllers;

import spark.Route;
import spark.Request;
import spark.Response;
import webapp.helpers.HtmlResponse;

public class IndexWebController {
    public static Route indexPageHandler = (Request req,Response res)-> {
        return HtmlResponse.getTemplate("index.html");
    };
}
