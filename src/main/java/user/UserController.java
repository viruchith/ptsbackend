package user;

import helpers.*;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

import spark.Request;
import spark.Response;
import spark.Route;
import appexceptions.ObjectAlreadyExistsException;

import java.util.HashMap;

public class UserController {
    public static Route createUser = (Request req, Response res)->{
        res.type("application/json");

        JSONObject errors;

        try{
            JSONObject userObj = new JSONObject(req.body());
            Validator username = new Validator(userObj.getString("username")).minLength(3).maxLength(15).matches(Constants.User.USER_NAME_REGEX);
            Validator name = new Validator(userObj.getString("name")).minLength(2).maxLength(256);
            Validator email =new Validator(userObj.getString("email")).isEmail().maxLength(100);
            Validator password = new Validator(userObj.getString("password")).minLength(8).maxLength(100);
            Validator password_hint = new Validator(userObj.getString("password_hint")).minLength(2).maxLength(50);

            errors = new JSONObject();

            if(!username.isValid()){
                errors.put("username",username.getErrorMessages());
            }

            if(!name.isValid()){
                errors.put("name",name.getErrorMessages());
            }

            if(!email.isValid()){
                errors.put("email",email.getErrorMessages());
            }

            if(!password.isValid()){
                errors.put("password",password.getErrorMessages());
            }

            if(!password_hint.isValid()){
                errors.put("password_hint",password_hint.getErrorMessages());
            }

            if(errors.isEmpty()){
                User user = new User(username.getValue(), name.getValue(), email.getValue(), password.getValue(), password_hint.getValue());
                try{
                    if(user.create()){
                        return new ResponseObject(true,"User created successfully!");
                    }
                    else {
                        return new ResponseObject(false,"Unable to create user!");
                    }
                }
                catch (ObjectAlreadyExistsException e){
                    return new ResponseObject(false,e.getMessage());

                }

            }else{
                return new ResponseObject(false,new JSONObject().put("message","Invalid Data !").put("errors",errors));
            }
        }catch(JSONException e){
            return "Invalid data";
        }
    };

    public static Route loginUser = (Request req,Response res)->{
        res.type("application/json");
        JSONObject loginData,resData,errors;

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
        }else{

            if(PasswordHasher.verifyPassword(password.getValue(),user.getPassword())){
                HashMap <String,String> tokenMap = new HashMap<String,String>();
                tokenMap.put("username",user.getUsername());
                tokenMap.put("id",Integer.toString(user.getId()));

                String token = Authenticator.createToken(tokenMap);
                resData.put("message","Authenticated successfully !");
                resData.put("token",token);
                return new ResponseObject(true,resData);
            }else{
                return new ResponseObject(false,"Incorrect Password !");
            }

        }
    };

    public static Route getPublicUserInfo = (Request req, Response res) -> {
        res.type("application/json");
        JSONObject tokenData;

        String token = req.headers("Auth-Token");
        String username = req.params("username");

        if (token == null) {
            return new ResponseObject(false, "Missing Auth Token !");
        }

        tokenData = Authenticator.verifyToken(token);


        if (tokenData == null) {
            return new ResponseObject(false, "Invalid Token !");
        }

        User user = User.getFirstByUsername(username);

        if (user == null) {
            return new ResponseObject(false, "\"" + username + "\" does not exist !");
        } else {
            JSONObject userObj = user.getUserObj();
            userObj.remove("password");
            userObj.remove("password_hint");
            userObj.remove("email");
            userObj.remove("id");

            return new ResponseObject(true, "", new JSONObject().put("user", userObj));
        }


    };

    public static Route verifyUserToken =  (Request req, Response res)->{
        res.type("application/json");

        JSONObject tokenData;

        String token = req.headers("Auth-Token");

        if(token==null){
            return new ResponseObject(false,"Missing Auth Token !");
        }

        tokenData = Authenticator.verifyToken(token);


        if(tokenData==null){
            return new ResponseObject(false,"Invalid Token !");
        }

        User user = User.getFirstById(tokenData.getInt("id"));

        //remove password
        JSONObject userJsonObj = user.getUserObj();
        userJsonObj.remove("password");

        if(user==null){
            return new ResponseObject(false, "User does not exist !");
        }else{
            return new ResponseObject(true,"Token Verified Successfully !",new JSONObject().put("user", userJsonObj));
        }

    };

    public static Route changePassword = (Request req,Response res)->{
        res.type("application/json");

        JSONObject resetData,errors,tokenData;

        String token = req.headers("Auth-Token");

        if(token==null){
            return new ResponseObject(false,"Missing Auth Token !");
        }

        tokenData = Authenticator.verifyToken(token);

        if(tokenData==null){
            return new ResponseObject(false,"Invalid Token !");
        }

        try{
            resetData = new JSONObject(req.body());
        }catch (JSONException e){
            return new ResponseObject(false,"Invalid Data !");
        }

        if(resetData.isEmpty() || !resetData.has("password") || !resetData.has("new_password") || !resetData.has("password_hint")){
            return new ResponseObject(false,"Missing data !");
        }

        errors = new JSONObject();

        Validator password = new Validator(resetData.getString("password")).minLength(8).maxLength(50);
        Validator new_password = new Validator(resetData.getString("new_password")).minLength(8).maxLength(50);
        Validator new_password_hint = new Validator(resetData.getString("password_hint")).minLength(2).maxLength(50);

        if(!password.isValid()){
            errors.put("password",new JSONArray(password.getErrorMessages()));
        }

        if(!new_password.isValid()){
            errors.put("new_password",new JSONArray(new_password.getErrorMessages()));
        }

        if(!new_password_hint.isValid()){
            errors.put("password_hint",new JSONArray(new_password_hint.getErrorMessages()));
        }

        if(!errors.isEmpty()){
            return new ResponseObject(false,new JSONObject().put("message","Invalid Data !").put("errors",errors));
        }


        User user = User.getFirstById(tokenData.getInt("id"));

        if(user==null){
            return new ResponseObject(false,"User does not exist !");
        }else{

            if(PasswordHasher.verifyPassword(resetData.getString("password"),user.getPassword())){
                user.updatePassword(new_password.getValue(),new_password_hint.getValue());
                return new ResponseObject(true,"Password updated successfully !");
            }else{
                return new ResponseObject(false,"Incorrect password !");
            }
        }
    };

    public static Route updateUserAccountDetails = (Request req,Response res) ->{
        res.type("application/json");

        JSONObject errors,tokenData,accountData;

        String token = req.headers("Auth-Token");

        if(token==null){
            return new ResponseObject(false,"Missing Auth Token !");
        }

        try{
            accountData = new JSONObject(req.body());
        }catch (JSONException e){
            return new ResponseObject(false,"Invalid Data !");
        }
        if(accountData.isEmpty() || !accountData.has("username") || !accountData.has("name") || !accountData.has("email")){
            return new ResponseObject(false,"Missing data !");
        }

        tokenData = Authenticator.verifyToken(token);

        if(tokenData==null){
            return new ResponseObject(false,"Invalid Token !");
        }

        Validator username = new Validator(accountData.getString("username")).minLength(3).maxLength(15).matches(Constants.User.USER_NAME_REGEX);
        Validator name = new Validator(accountData.getString("name")).minLength(2).maxLength(256);
        Validator email =new Validator(accountData.getString("email")).isEmail().maxLength(100);

        errors = new JSONObject();

        if(!username.isValid()){
            errors.put("username",username.getErrorMessages());
        }

        if(!name.isValid()){
            errors.put("name",name.getErrorMessages());
        }

        if(!email.isValid()){
            errors.put("email",email.getErrorMessages());
        }

        if(!errors.isEmpty()){
            return new ResponseObject(false,new JSONObject().put("message","Invalid Data !").put("errors",errors));
        }

        User user = User.getFirstById(tokenData.getInt("id"));

        if(user == null){
            return new ResponseObject(false,"User does not exist !");
        }else{
            try {
                if(user.updateAccountDetails(username.getValue(),name.getValue(),email.getValue())){
                    return new ResponseObject(true,"Details updated successfully !");
                }else{
                    return new ResponseObject(false,"Error updating data !");
                }
            }catch (ObjectAlreadyExistsException e){
                return new ResponseObject(false,e.getMessage());
            }
        }

    };

    public static Route getUserPasswordHint = (Request req,Response res)->{
        res.type("application/json");

        String username = req.params("username");

        User user = User.getFirstByUsername(username);

        if(user == null){
            return new ResponseObject(false,"User does not exist !");
        }else{
            return new ResponseObject(true,new JSONObject().put("password_hint",user.getPassword_hint()));
        }

    };


}
