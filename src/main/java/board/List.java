package board;

import helpers.DBQueryHelper;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class List {
    private int id, board_id;
    private String title, tasks, created_at;
    private JSONObject jsonObject;
    private JSONArray tasksJSONArray;

    public List(int board_id, String title, String tasks) {
        this.board_id = board_id;
        this.title = title;
        this.tasks = tasks;
    }

    public List(int id, int board_id, String title, String tasks, String created_at) {
        this.id = id;
        this.board_id = board_id;
        this.title = title;
        this.tasks = tasks;
        this.created_at = created_at;
        this.tasksJSONArray = new JSONArray(tasks);
        this.jsonObject = new JSONObject().put("id", id).put("title", title).put("tasks", this.tasksJSONArray).put("created_at", created_at);
    }

    public static boolean belongsToBoard(int id, int board_id) {
        boolean belongsTo = false;
        try {
            PreparedStatement stmt = DBQueryHelper.getPreparedStatement("SELECT * FROM `pts`.`lists` WHERE board_id = ? AND id = ?");
            stmt.setInt(1, board_id);
            stmt.setInt(2, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                belongsTo = true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return belongsTo;
    }

    public JSONObject getJSONObject() {
        return this.jsonObject;
    }

    public int getId() {
        return id;
    }

    public int getBoard_id() {
        return board_id;
    }

    public String getTitle() {
        return title;
    }

    public String getTasks() {
        return tasks;
    }

    public String getCreated_at() {
        return created_at;
    }

    @Override
    public String toString() {
        return this.jsonObject.toString();
    }
}
