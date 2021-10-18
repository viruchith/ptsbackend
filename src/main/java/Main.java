import static spark.Spark.*;

import board.BoardController;
import bugtracker.BugTrackerController;
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

        //CORS ENABLE
        options("/*",
                (request, response) -> {

                    String accessControlRequestHeaders = request
                            .headers("Access-Control-Request-Headers");
                    if (accessControlRequestHeaders != null) {
                        response.header("Access-Control-Allow-Headers",
                                accessControlRequestHeaders);
                    }

                    String accessControlRequestMethod = request
                            .headers("Access-Control-Request-Method");
                    if (accessControlRequestMethod != null) {
                        response.header("Access-Control-Allow-Methods",
                                accessControlRequestMethod);
                    }

                    return "OK";
                });

        before((request, response) -> response.header("Access-Control-Allow-Origin", "*"));

        before("/app/auth/*", AuthMiddleware.handle);//WEB APP AUTH MIDDLEWARE ( PROTECTED ROUTES )


        // USER ROUTES
        post("/user/create", "application/json", UserController.createUser);
        post("/user/login", "application/json", UserController.loginUser);
        get("/user/token/verify", "application/json", UserController.verifyUserToken);
        post("/user/changepassword", "application/json", UserController.changePassword);
        post("/user/update", "application/json", UserController.updateUserAccountDetails);
        get("/user/:username/info", UserController.getPublicUserInfo);
        get("/user/:username/passwordhint", "application/json", UserController.getUserPasswordHint);
        get("/user/search/:username", UserController.searchUsers);
        get("/user/teams", "application/json", UserController.getUserTeamsInfo);
        get("/user/boards", "application/json", UserController.getAllUserBoards);
        get("/user/bugtrackers", "application/json", UserController.getAllUserBugTrackers);

        // TEAM ROUTES
        post("/team/create", "application/json", TeamController.createTeam);
        post("/team/member/add", "application/json", TeamController.addTeamMember);
        delete("/team/:team_id/member/:username/delete", "application/json", TeamController.removeTeamMember);
        get("/team/:team_id/member/all", "application/json", TeamController.getTeamMembers);
        get("/team/:team_id/info", TeamController.getTeamInfo);


        // TEAM BOARD
        post("/team/:team_id/board/:board_id/task/create", BoardController.createTask);
        post("/team/:team_id/board/:board_id/task/update", BoardController.updateTask);
        get("/team/:team_id/board/:board_id", BoardController.getBoardData);
        post("/board/create", "application/json", BoardController.create);

        //TEAM BUGTRACKER
        get("/team/:team_id/bugtracker/:bugtracker_id", BugTrackerController.getAllBugs);
        post("/team/:team_id/bugtracker/:bugtracker_id/bug/create", BugTrackerController.createBug);
        post("/team/:team_id/bugtracker/:bugtracker_id/bug/update", BugTrackerController.updateBug);
        post("/bugtracker/create", BugTrackerController.createBugtracker);


        //WEB APP ROUTES
        get("/", IndexWebController.indexPageHandler);//WEB INDEX
        get("/app/login", UserWebController.getLoginPage);
        post("/app/login", "application/json", UserWebController.loginUser);
        get("/app/logout", UserWebController.logoutUser);

        get("/app/auth/documentation", UserWebController.getDocumentationPage);

    }
}