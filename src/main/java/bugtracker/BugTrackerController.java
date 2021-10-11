package bugtracker;

import helpers.Authenticator;
import helpers.ResponseObject;
import helpers.Validator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import spark.Route;
import spark.Response;
import spark.Request;
import user.User;

public class BugTrackerController {


    public static Route createBugtracker = (Request req, Response res) -> {
        res.type("application/json");
        JSONObject tokenData, bugtrackData, errors;
        Validator team_id, title, description;

        String token = req.headers("Auth-Token");

        if (token == null) {
            return new ResponseObject(false, "Missing Auth Token !");
        }

        tokenData = Authenticator.verifyToken(token);

        if (tokenData == null) {
            return new ResponseObject(false, "Invalid Token !");
        }

        try {
            bugtrackData = new JSONObject(req.body());
        } catch (JSONException e) {
            return new ResponseObject(false, "Invalid Data format !");
        }

        if (!Validator.jsonObjectHasKeys(bugtrackData, new String[]{"team_id", "title", "description"})) {
            return new ResponseObject(false, "Missing data !");
        }

        try {
            team_id = new Validator(Integer.toString(bugtrackData.getInt("team_id"))).isPresent().isInt();
            title = new Validator(bugtrackData.getString("title")).isPresent().minLength(2).maxLength(50);
            description = new Validator(bugtrackData.getString("description")).isPresent().minLength(2).maxLength(512);
        } catch (Exception e) {
            return new ResponseObject(false, "Incorrect data !");
        }

        errors = new JSONObject();

        if (!team_id.isValid()) {
            errors.put("team_id", team_id.getErrorMessages());
        }
        if (!title.isValid()) {
            errors.put("title", title.getErrorMessages());
        }
        if (!description.isValid()) {
            errors.put("description", description.getErrorMessages());
        }

        if (!errors.isEmpty()) {
            return new ResponseObject(false, "Invalid data !", new JSONObject().put("errors", errors));
        }

        if (User.isMemberOfTeam(team_id.getIntValue(), tokenData.getInt("id"))) {
            BugTracker.create(team_id.getIntValue(), title.getValue(), description.getValue());
            return new ResponseObject(true, "Created successfully !");
        } else {
            return new ResponseObject(false, "User is not part of the team !");
        }

    };

    public static Route createBug = (Request req, Response res) -> {
        res.type("application/json");
        JSONObject tokenData, bugData, errors;
        Validator title, description;
        int severity_level = 0, team_id, bugtracker_id;

        String token = req.headers("Auth-Token");

        if (token == null) {
            return new ResponseObject(false, "Missing Auth Token !");
        }

        tokenData = Authenticator.verifyToken(token);

        if (tokenData == null) {
            return new ResponseObject(false, "Invalid Token !");
        }

        try {
            team_id = Integer.parseInt(req.params("team_id"));
            bugtracker_id = Integer.parseInt(req.params("bugtracker_id"));
        } catch (NumberFormatException e) {
            return new ResponseObject(false, "team_id and bugtracker_id must be an Integer !");
        }

        try {
            bugData = new JSONObject(req.body());
        } catch (JSONException e) {
            return new ResponseObject(false, "Invalid Data format !");
        }

        if (!Validator.jsonObjectHasKeys(bugData, new String[]{"title", "description", "severity_level"})) {
            return new ResponseObject(false, "Missing data !");
        }

        try {

            title = new Validator(bugData.getString("title")).isPresent().minLength(2).maxLength(50);
            description = new Validator(bugData.getString("description")).isPresent().minLength(2).maxLength(512);
            severity_level = bugData.getInt("severity_level");
        } catch (Exception e) {
            return new ResponseObject(false, "Incorrect data !");
        }

        errors = new JSONObject();


        if (!title.isValid()) {
            errors.put("title", title.getErrorMessages());
        }
        if (!description.isValid()) {
            errors.put("description", description.getErrorMessages());
        }

        if (!BugTracker.validateSeverityLevel(severity_level)) {
            errors.put("severity_level", new String[]{"Invalid level !"});
        }

        if (!errors.isEmpty()) {
            return new ResponseObject(false, "Invalid data !", new JSONObject().put("errors", errors));
        }

        if (User.isMemberOfTeam(team_id, tokenData.getInt("id"))) {
            if (BugTracker.belongsToTeam(bugtracker_id, team_id)) {
                Bug.create(bugtracker_id, title.getValue(), description.getValue(), severity_level);
                return new ResponseObject(true, "Bug added successfully !");
            } else {
                return new ResponseObject(false, "Unauthorized actions !");
            }
        } else {
            return new ResponseObject(false, "User is not part of the team !");
        }

    };

    public static Route updateBug = (Request req, Response res) -> {
        res.type("application/json");
        JSONObject tokenData, bugData, errors;
        Validator title, description;
        int id, severity_level = 0, team_id, bugtracker_id;
        boolean is_closed = false;

        String token = req.headers("Auth-Token");

        if (token == null) {
            return new ResponseObject(false, "Missing Auth Token !");
        }

        tokenData = Authenticator.verifyToken(token);

        if (tokenData == null) {
            return new ResponseObject(false, "Invalid Token !");
        }

        try {
            team_id = Integer.parseInt(req.params("team_id"));
            bugtracker_id = Integer.parseInt(req.params("bugtracker_id"));
        } catch (NumberFormatException e) {
            return new ResponseObject(false, "team_id and bugtracker_id must be an Integer !");
        }

        try {
            bugData = new JSONObject(req.body());
        } catch (JSONException e) {
            return new ResponseObject(false, "Invalid Data format !");
        }

        if (!Validator.jsonObjectHasKeys(bugData, new String[]{"id", "title", "description", "severity_level", "is_closed"})) {
            return new ResponseObject(false, "Missing data !");
        }

        try {
            id = bugData.getInt("id");
            title = new Validator(bugData.getString("title")).isPresent().minLength(2).maxLength(50);
            description = new Validator(bugData.getString("description")).isPresent().minLength(2).maxLength(512);
            severity_level = bugData.getInt("severity_level");
            is_closed = (bugData.getBoolean("is_closed"));
        } catch (Exception e) {
            return new ResponseObject(false, "Incorrect data !");
        }

        errors = new JSONObject();


        if (!title.isValid()) {
            errors.put("title", title.getErrorMessages());
        }
        if (!description.isValid()) {
            errors.put("description", description.getErrorMessages());
        }

        if (!BugTracker.validateSeverityLevel(severity_level)) {
            errors.put("severity_level", new String[]{"Invalid level !"});
        }

        if (!errors.isEmpty()) {
            return new ResponseObject(false, "Invalid data !", new JSONObject().put("errors", errors));
        }

        if (User.isMemberOfTeam(team_id, tokenData.getInt("id"))) {
            if (BugTracker.belongsToTeam(bugtracker_id, team_id)) {
                if (Bug.belongsToBugTracker(bugtracker_id, id)) {
                    Bug.update(id, title.getValue(), description.getValue(), severity_level, is_closed);
                    return new ResponseObject(true, "updated successfully !");
                } else {
                    return new ResponseObject(false, "Unauthorized actions !");
                }
            } else {
                return new ResponseObject(false, "Unauthorized actions !");
            }
        } else {
            return new ResponseObject(false, "User is not part of the team !");
        }

    };

    public static Route getAllBugs = (Request req, Response res) -> {
        res.type("application/json");
        JSONObject tokenData;
        int team_id, bugtracker_id;

        String token = req.headers("Auth-Token");

        if (token == null) {
            return new ResponseObject(false, "Missing Auth Token !");
        }

        tokenData = Authenticator.verifyToken(token);

        if (tokenData == null) {
            return new ResponseObject(false, "Invalid Token !");
        }

        try {
            team_id = Integer.parseInt(req.params("team_id"));
            bugtracker_id = Integer.parseInt(req.params("bugtracker_id"));
        } catch (NumberFormatException e) {
            return new ResponseObject(false, "team_id and bugtracker_id must be an Integer !");
        }
        if (User.isMemberOfTeam(team_id, tokenData.getInt("id"))) {
            if (BugTracker.belongsToTeam(bugtracker_id, team_id)) {
                JSONArray bugs = BugTracker.getAllBugs(bugtracker_id);
                return new ResponseObject(true, "Request successful", new JSONObject().put("bugs", bugs));
            } else {
                return new ResponseObject(false, "Unauthorized actions !");
            }
        } else {
            return new ResponseObject(false, "User is not part of the team !");
        }

    };


}
