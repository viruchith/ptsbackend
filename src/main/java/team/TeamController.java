package team;

import appexceptions.ObjectAlreadyExistsException;
import appexceptions.ObjectDoesNotExistException;
import helpers.*;
import org.json.JSONArray;
import spark.Request;
import spark.Response;
import spark.Route;

import org.json.JSONObject;
import org.json.JSONException;
import user.User;

import java.sql.PreparedStatement;
import java.sql.SQLException;


public class TeamController {

    public static Route createTeam = (Request req, Response res) -> {
        res.type("application/json");

        JSONObject teamData, tokenData, errors;

        String token = req.headers("Auth-Token");

        if (token == null) {
            return new ResponseObject(false, "Missing Auth Token !");
        }


        tokenData = Authenticator.verifyToken(token);


        if (tokenData == null) {
            return new ResponseObject(false, "Invalid Token !");
        }

        try {
            teamData = new JSONObject(req.body());
        } catch (JSONException e) {
            return new ResponseObject(false, "Invalid Data !");
        }

        if (teamData.isEmpty() || !teamData.has("title")) {
            return new ResponseObject(false, "Missing data !");
        }


        Validator title = new Validator(teamData.getString("title")).isPresent().matches(Constants.Team.TEAM_TITLE_REGEX).minLength(2).maxLength(25);

        if (!title.isValid()) {
            errors = new JSONObject().put("title", new JSONArray(title.getErrorMessages()));
            return new ResponseObject(false, new JSONObject().put("message", "Invalid Title !").put("errors", errors));
        }

        if (Team.create(tokenData.getInt("id"), title.getValue())) {
            Team team = Team.getLastCreatedTeamOfUser(tokenData.getInt("id"));
            //ADD OWNER as team member after creation of team
            TeamMember.add(team.getId(), tokenData.getString("username"));
            if (team == null) {
                return new ResponseObject(true, "Created Successfully !");
            } else {
                return new ResponseObject(true, new JSONObject().put("message", "Created Successfully !").put("team", team.getJsonObject()));
            }
        } else {
            return new ResponseObject(false, "Unable to create team !");
        }

    };


    public static Route addTeamMember = (Request req, Response res) -> {
        res.type("application/json");
        JSONObject memberData, tokenData, errors;

        String token = req.headers("Auth-Token");

        if (token == null) {
            return new ResponseObject(false, "Missing Auth Token !");
        }


        tokenData = Authenticator.verifyToken(token);


        if (tokenData == null) {
            return new ResponseObject(false, "Invalid Token !");
        }

        try {
            memberData = new JSONObject(req.body());
        } catch (JSONException e) {
            return new ResponseObject(false, "Invalid Data !");
        }

        if (memberData.isEmpty() || !memberData.has("team_id") || !memberData.has("username")) {
            return new ResponseObject(false, "Missing Data !");
        }

        Validator team_id = new Validator(Integer.toString(memberData.getInt("team_id"))).isPresent().isInt().maxLength(11);
        Validator username = new Validator(memberData.getString("username")).isPresent().minLength(5).maxLength(15).matches(Constants.User.USER_NAME_REGEX);

        errors = new JSONObject();

        if (!team_id.isValid()) {
            errors.put("team_id", new JSONArray(team_id.getErrorMessages()));
        }

        if (!username.isValid()) {
            errors.put("username", new JSONArray(username.getErrorMessages()));
        }

        if (!errors.isEmpty()) {
            return new ResponseObject(false, new JSONObject().put("errors", errors));
        }

        Team team = Team.getTeamById(Integer.parseInt(team_id.getValue()));

        if (team == null) {
            return new ResponseObject(false, "Invalid Team Id !");
        }

        // Only owner can add the member
        if (team.getOwner_id() != tokenData.getInt("id")) {
            return new ResponseObject(false, "Unauthorized Action !");
        }

        try {
            if (TeamMember.add(Integer.parseInt(team_id.getValue()), username.getValue())) {
                return new ResponseObject(true, "Added successfully !");
            } else {
                return new ResponseObject(false, "Unable to add to team");
            }
        } catch (ObjectDoesNotExistException e) {
            return new ResponseObject(false, e.getMessage());
        } catch (ObjectAlreadyExistsException e) {
            return new ResponseObject(false, e.getMessage());
        }

    };

    public static Route removeTeamMember = (Request req, Response res) -> {
        res.type("application/json");

        JSONObject tokenData, errors;

        String token = req.headers("Auth-Token");

        if (token == null) {
            return new ResponseObject(false, "Missing Auth Token !");
        }


        tokenData = Authenticator.verifyToken(token);


        if (tokenData == null) {
            return new ResponseObject(false, "Invalid Token !");
        }

        errors = new JSONObject();

        Validator team_id = new Validator(req.params("team_id")).isInt(), username = new Validator(req.params("username")).isPresent().minLength(5).maxLength(15).matches(Constants.User.USER_NAME_REGEX);
        if (!team_id.isValid()) {
            errors.put("team_id", new JSONArray(team_id.getErrorMessages()));
        }
        if (!username.isValid()) {
            errors.put("username", new JSONArray(username.getErrorMessages()));
        }

        if (!errors.isEmpty()) {
            return new ResponseObject(false, new JSONObject().put("message", "Invalid data !").put("errors", errors));
        }

        Team team = Team.getTeamById(Integer.parseInt(team_id.getValue()));

        if (team == null) {
            return new ResponseObject(false, "Invalid Team Id !");
        }
        // Only owner can delete the member
        if (team.getOwner_id() != tokenData.getInt("id")) {
            return new ResponseObject(false, "Unauthorized Action !");
        }

        if (username.equals(tokenData.getString("username"))) {
            return new ResponseObject(false, "Team Owner Cannot be deleted !");
        }


        try {
            PreparedStatement stmt = DBQueryHelper.getPreparedStatement("DELETE FROM `pts`.`team_members` WHERE team_id = ? AND user_id = ( SELECT `id` FROM `pts`.`users` WHERE `username` = ? LIMIT 1 ) ");
            stmt.setInt(1, Integer.parseInt(team_id.getValue()));
            stmt.setString(2, username.getValue());
            stmt.execute();
            return new ResponseObject(true, "Deleted Successfully !");
        } catch (SQLException e) {
            e.printStackTrace();
            return new ResponseObject(false, "Error Deleting user !");
        }
    };

    public static Route getTeamMembers = (Request req, Response res) -> {
        res.type("application/json");
        JSONObject tokenData, errors;

        String token = req.headers("Auth-Token");

        if (token == null) {
            return new ResponseObject(false, "Missing Auth Token !");
        }


        tokenData = Authenticator.verifyToken(token);


        if (tokenData == null) {
            return new ResponseObject(false, "Invalid Token !");
        }
        int team_id;

        try {
            team_id = Integer.parseInt(req.params("team_id"));
        } catch (NumberFormatException e) {
            return new ResponseObject(false, "Invalid Team Id !");
        }

        if (User.isMemberOfTeam(team_id, tokenData.getInt("id"))) {// only if user belongs to the Team
            JSONArray members = Team.getMembers(team_id);
            return new ResponseObject(true, "Request successful !", new JSONObject().put("members", members));
        } else {
            return new ResponseObject(false, "User is not a member of the team !");
        }

    };

    public static Route getTeamInfo = (Request req, Response res) -> {
        res.type("application/json");
        JSONObject tokenData, errors;

        String token = req.headers("Auth-Token");

        if (token == null) {
            return new ResponseObject(false, "Missing Auth Token !");
        }

        int team_id;

        try {
            team_id = Integer.parseInt(req.params("team_id"));
        } catch (NumberFormatException e) {
            return new ResponseObject(false, "Invalid Team Id !");
        }

        tokenData = Authenticator.verifyToken(token);

        if (tokenData == null) {
            return new ResponseObject(false, "Invalid Token !");
        }
        if (User.isMemberOfTeam(team_id, tokenData.getInt("id"))) {
            JSONObject team_info = Team.getInfo(team_id);
            return new ResponseObject(true, "Request successful", new JSONObject().put("team_info", team_info));
        } else {
            return new ResponseObject(false, "User is not a member of the team !");
        }
    };

}
