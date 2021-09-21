import static spark.Spark.*;

import team.TeamController;
import user.UserController;
import helpers.TableGenerator;
import webapp.controllers.IndexWebController;
import webapp.controllers.UserWebController;
import webapp.helpers.AuthMiddleware;

public class Main{
    public static void main(String[] args) {
        // LOG4J
        //BasicConfigurator.configure();

        //STATIC FILES DIR
        staticFiles.location("/public"); // Static files

        // GENERATE TABLES
        TableGenerator.run();


        // USER ROUTES
        post("/user/create","application/json",UserController.createUser);
        post("/user/login","application/json",UserController.loginUser);
        get("/user/token/verify","application/json",UserController.verifyUserToken);
        post("/user/changepassword","application/json",UserController.changePassword);
        post("/user/update","application/json",UserController.updateUserAccountDetails);
        get("/user/:username/passwordhint","application/json",UserController.getUserPasswordHint);
        // TEAM ROUTES
        post("/team/create","application/json",TeamController.createTeam);
        post("/team/member/add","application/json",TeamController.addTeamMember);
        delete("/team/:team_id/member/:member_id/delete","application/json",TeamController.removeTeamMember);

        //WEB APP ROUTES
        get("/", IndexWebController.indexPageHandler);//WEB INDEX
        get("/app/login", UserWebController.getLoginPage);
        post("/app/login","application/json",UserWebController.loginUser);

        before("/app/auth/*", AuthMiddleware.handle);//AUTH MIDDLEWARE ( PROTECTED ROUTES )

        get("/app/auth/dashboard", UserWebController.getDashboardPage);

    }
}