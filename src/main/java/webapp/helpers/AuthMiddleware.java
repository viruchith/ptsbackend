package webapp.helpers;

import helpers.ResponseObject;
import spark.Request;
import spark.Response;
import spark.Filter;

public class AuthMiddleware {
    public static Filter handle = (Request req, Response res)->{
        Boolean isLoggedIn = (Boolean)req.session().attribute("loggedin");
        if(isLoggedIn==null){
            res.redirect("/app/login");
        }
    };
}
