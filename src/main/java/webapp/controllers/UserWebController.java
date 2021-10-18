package webapp.controllers;
import helpers.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import spark.Response;
import spark.Request;
import spark.Route;
import user.User;
import webapp.helpers.HtmlResponse;

import java.util.HashMap;

public class UserWebController {
    public static Route getLoginPage = (Request req,Response res)->{

          Boolean isLoggedIn = (Boolean)req.session().attribute("loggedin");
          if(isLoggedIn==null){
              return HtmlResponse.getTemplate("login.html");
          }else{
              res.redirect("/app/auth/documentation");
              return "";
          }

    };

    public static Route loginUser = (Request req,Response res)->{
        res.type("application/json");
        JSONObject loginData,resData,errors;

        Boolean isLoggedIn = (Boolean)req.session().attribute("loggedin");
        if(isLoggedIn!=null){
            return new ResponseObject(false,"User already loggedin !");
        }

        try{
            loginData = new JSONObject(req.body());
        }catch(JSONException e){
            return new ResponseObject(false,"Invalid Data !");
        }

        resData = new JSONObject();
        errors = new JSONObject();
        if(loginData.isEmpty() || !loginData.has("username") || !loginData.has("password")){
            return new ResponseObject(false,"Missing Data !");
        }
        Validator username = new Validator(loginData.getString("username")).minLength(2).maxLength(15).matches(Constants.User.USER_NAME_REGEX);
        Validator password = new Validator(loginData.getString("password")).minLength(8);
        if(!username.isValid()){
            errors.put("username",new JSONArray(username.getErrorMessages()));
        }
        if(!password.isValid()){
            errors.put("password",new JSONArray(password.getErrorMessages()));
        }
        if(!errors.isEmpty()){
            return new ResponseObject(false,new JSONObject().put("message","Invalid Data !").put("errors",errors));
        }
        User user = User.getFirstByUsername(username.getValue());
        if(user == null){
            return new ResponseObject(false,"User does not exist !");
        }else {

            if (PasswordHasher.verifyPassword(password.getValue(), user.getPassword())) {
                HashMap<String, String> tokenMap = new HashMap<String, String>();
                tokenMap.put("username", user.getUsername());
                tokenMap.put("id", Integer.toString(user.getId()));

                String token = Authenticator.createToken(tokenMap);
                resData.put("message", "Authenticated successfully !");
                resData.put("token", token);
                resData.put("username", user.getUsername());

                req.session(true).attribute("loggedin", true);
                req.session().attribute("user_id", user.getId());

                return new ResponseObject(true, resData);
            } else {
                return new ResponseObject(false, "Incorrect Password !");
            }

        }
    };

    public static Route getDocumentationPage = (Request req, Response res) -> {
        return HtmlResponse.getTemplate("documentation.html");
    };

    public static Route logoutUser = (Request req, Response res) -> {
        if ((Boolean) req.session().attribute("loggedin") != null) {
            req.session().removeAttribute("loggedin");
        }

        res.redirect("/app/login");
        return "Redirecting....";
    };
}
