package bugtracker;

import helpers.DBQueryHelper;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;

public class BugTracker {
    private int id, team_id;
    private String title, description, created_at;
    private JSONObject jsonObject;

    public BugTracker(int id, int team_id, String title, String description, String created_at) {
        this.id = id;
        this.team_id = team_id;
        this.title = title;
        this.description = description;
        this.created_at = created_at;
        this.jsonObject = new JSONObject().put("id", id).put("team_id", team_id).put("title", title).put("description", description).put("created_at", created_at);
    }

    public BugTracker(int id, int team_id, String title, String description, String created_at, JSONObject team) {
        this.id = id;
        this.team_id = team_id;
        this.title = title;
        this.description = description;
        this.created_at = created_at;
        this.jsonObject = new JSONObject().put("id", id).put("team_id", team_id).put("title", title).put("description", description).put("created_at", created_at).put("team", team);
    }


    public BugTracker(int team_id, String title, String description) {
        this.team_id = team_id;
        this.title = title;
        this.description = description;
        this.jsonObject = new JSONObject().put("team_id", team_id).put("title", title).put("description", description);
    }

    public static boolean create(int team_id, String title, String description) {
        try {
            PreparedStatement stmt = DBQueryHelper.getPreparedStatement("INSERT INTO `pts`.`bugtrackers` (`team_id`, `title`, `description`) VALUES (?,?,?)");
            stmt.setInt(1, team_id);
            stmt.setString(2, title);
            stmt.setString(3, description);
            stmt.execute();
            stmt.close();
            return true;
        } catch (SQLIntegrityConstraintViolationException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean belongsToTeam(int bugtracker_id, int team_id) {
        try {
            PreparedStatement stmt = DBQueryHelper.getPreparedStatement("SELECT * FROM `pts`.`bugtrackers` WHERE id = ? AND team_id = ?");
            stmt.setInt(1, bugtracker_id);
            stmt.setInt(2, team_id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static JSONArray getAllBugs(int bugtracker_id) {
        JSONArray bugs = new JSONArray();
        try {
            PreparedStatement stmt = DBQueryHelper.getPreparedStatement("SELECT `bugs`.`id`, `bugs`.`bugtracker_id`, `bugs`.`title`, `bugs`.`description`, `bugs`.`severity_level`, `bugs`.`is_closed`, `bugs`.`created_at` FROM `pts`.`bugs` WHERE `bugs`.`bugtracker_id` = ? ORDER BY `bugs`.`severity_level` ASC ");
            stmt.setInt(1, bugtracker_id);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                bugs.put(new Bug(rs.getInt("id"), rs.getInt("bugtracker_id"), rs.getString("title"), rs.getString("description"), rs.getInt("severity_level"), rs.getBoolean("is_closed"), rs.getString("created_at")).getJsonObject());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return bugs;
    }

    public JSONObject getJsonObject() {
        return jsonObject;
    }

    public static boolean validateSeverityLevel(int SEVERITY_LEVEL) {
        if (SEVERITY_LEVEL >= 0 && SEVERITY_LEVEL <= 3)
            return true;
        else
            return false;
    }
}
