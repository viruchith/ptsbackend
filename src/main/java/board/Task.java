package board;

import helpers.DBQueryHelper;
import org.json.JSONObject;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Task {
    private int id, list_id;
    private String title, description, due_date, created_at;
    private JSONObject jsonObject;

    public Task(int id, int list_id, String title, String description, String due_date, String created_at) {
        this.id = id;
        this.list_id = list_id;
        this.title = title;
        this.description = description;
        this.due_date = due_date;
        this.created_at = created_at;
        this.jsonObject = new JSONObject().put("id", id).put("list_id", list_id).put("title", title).put("description", description).put("due_date", due_date).put("created_at", created_at);
    }

    public Task(int id, int list_id, String title, String description, String due_date) {
        this.id = id;
        this.list_id = list_id;
        this.title = title;
        this.description = description;
        this.due_date = due_date;
        this.jsonObject = new JSONObject().put("id", id).put("list_id", list_id).put("title", title).put("description", description).put("due_date", due_date);
    }

    public Task(int list_id, String title, String description, String due_date) {
        this.list_id = list_id;
        this.title = title;
        this.description = description;
        this.due_date = due_date;
        this.jsonObject = new JSONObject().put("list_id", list_id).put("title", title).put("description", description).put("due_date", due_date);
    }

    public static boolean insert(int list_id, String data) {
        try {
            PreparedStatement stmt = DBQueryHelper.getPreparedStatement("INSERT INTO `pts`.`tasks` ( `list_id`, `data`, `is_archived`) VALUES ( ?, ?, ?)");
            stmt.setInt(1, list_id);
            stmt.setString(2, data);
            stmt.setBoolean(3, false);
            stmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static boolean update(int id, int list_id, String data, boolean is_archived) {
        try {
            PreparedStatement stmt = DBQueryHelper.getPreparedStatement("UPDATE `pts`.`tasks` SET `list_id` = ?, `data` = ? , `is_archived` = ?  WHERE `id` = ?");
            stmt.setInt(1, list_id);
            stmt.setString(2, data);
            stmt.setBoolean(3, is_archived);
            stmt.setInt(4, id);
            stmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
