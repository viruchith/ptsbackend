package board;

import helpers.Authenticator;
import helpers.ResponseObject;
import helpers.Validator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import spark.Route;
import spark.Request;
import spark.Response;
import user.User;

import java.sql.SQLIntegrityConstraintViolationException;

public class BoardController {
    public static Route create = (Request req, Response res) -> {
        res.type("application/json");
        JSONObject tokenData, boardData, errors;

        String token = req.headers("Auth-Token");

        if (token == null) {
            return new ResponseObject(false, "Missing Auth Token !");
        }

        tokenData = Authenticator.verifyToken(token);

        if (tokenData == null) {
            return new ResponseObject(false, "Invalid Token !");
        }

        try {
            boardData = new JSONObject(req.body());
        } catch (JSONException e) {
            return new ResponseObject(false, "Invalid Data !");
        }

        if (!boardData.has("team_id") || !boardData.has("title") || !boardData.has("description")) {
            return new ResponseObject(false, "Missing Data");
        }

        Validator team_id = new Validator(Integer.toString(boardData.getInt("team_id"))).isPresent("Team ID must not be empty !").isInt("Team ID must be a Number"),
                title = new Validator(boardData.getString("title")).isPresent("Board title must not be empty !").minLength(5).maxLength(50),
                description = new Validator(boardData.getString("description")).isPresent("Description must not be empty !").minLength(5).maxLength(200);

        errors = new JSONObject();

        if (!team_id.isValid()) {
            errors.put("team_id", new JSONArray(team_id.getErrorMessages()));
        }

        if (!title.isValid()) {
            errors.put("title", new JSONArray(title.getErrorMessages()));
        }
        if (!description.isValid()) {
            errors.put("title", new JSONArray(description.getErrorMessages()));
        }

        if (!errors.isEmpty()) {
            return new ResponseObject(false, new JSONObject().put("message", "Invalid Data !").put("errors", errors));
        }

        if (!User.isOwnerOfTeam(Integer.parseInt(team_id.getValue()), tokenData.getInt("id"))) {
            return new ResponseObject(false, "Only team owner can create a board !");
        }
        try {
            Board.create(Integer.parseInt(team_id.getValue()), title.getValue(), description.getValue());
            return new ResponseObject(true, "Board created successfully !");
        } catch (SQLIntegrityConstraintViolationException e) {
            return new ResponseObject(false, "User is not part of the team");
        }
    };
}
