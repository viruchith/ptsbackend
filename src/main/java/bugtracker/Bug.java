package bugtracker;

import helpers.DBQueryHelper;
import org.json.JSONObject;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;

public class Bug {
    private int id, bugtracker_id, severity_level;
    private String title, description, created_at;
    private boolean is_closed;
    private JSONObject jsonObject;

    public Bug(int id, int bugtracker_id, String title, String description, int severity_level, boolean is_closed, String created_at) {
        this.id = id;
        this.bugtracker_id = bugtracker_id;
        this.title = title;
        this.description = description;
        this.severity_level = severity_level;
        this.is_closed = is_closed;
        this.jsonObject = new JSONObject().put("id", id).put("bugtracker_id", bugtracker_id).put("title", title).put("description", description).put("severity_level", severity_level).put("is_closed", is_closed).put("created_at", created_at);
    }

    public Bug(int bugtracker_id, String title, String description, int severity_level) {
        this.bugtracker_id = bugtracker_id;
        this.title = title;
        this.description = description;
        this.severity_level = severity_level;
        this.is_closed = false;
        this.jsonObject = new JSONObject().put("bugtracker_id", bugtracker_id).put("title", title).put("description", description).put("severity_level", severity_level);
    }

    public static boolean create(int bugtracker_id, String title, String description, int severity_level) {
        try {
            PreparedStatement stmt = DBQueryHelper.getPreparedStatement("INSERT INTO `pts`.`bugs` ( `bugtracker_id`, `title`, `description`, `severity_level`,`is_closed`) VALUES ( ? ,? , ? ,? , ?)");
            stmt.setInt(1, bugtracker_id);
            stmt.setString(2, title);
            stmt.setString(3, description);
            stmt.setInt(4, severity_level);
            stmt.setBoolean(5, false);
            stmt.execute();
            stmt.close();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean update(int id, String title, String description, int severity_level, boolean is_closed) {
        try {
            PreparedStatement stmt = DBQueryHelper.getPreparedStatement("UPDATE `pts`.`bugs` SET `title` = ?, `description` = ?, `severity_level` = ?, `is_closed` = ? WHERE `id` = ?");
            stmt.setString(1, title);
            stmt.setString(2, description);
            stmt.setInt(3, severity_level);
            stmt.setBoolean(4, is_closed);
            stmt.setInt(5, id);
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

    public static boolean belongsToBugTracker(int bugtracker_id, int bug_id) {
        try {
            PreparedStatement stmt = DBQueryHelper.getPreparedStatement("SELECT * FROM `pts`.`bugs` WHERE id = ? AND bugtracker_id = ?");
            stmt.setInt(1, bug_id);
            stmt.setInt(2, bugtracker_id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public JSONObject getJsonObject() {
        return jsonObject;
    }
}
