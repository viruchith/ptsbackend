package board;

import appexceptions.ObjectAlreadyExistsException;
import appexceptions.ObjectDoesNotExistException;
import helpers.Constants;
import helpers.DBQueryHelper;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.*;

public class Board {
    private int id, team_id;
    private String title, description, created_at;
    private JSONObject jsonObject;

    public Board(int team_id, String title, String description) {
        this.team_id = team_id;
        this.title = title;
        this.description = description;
        this.jsonObject = new JSONObject().put("team_id", team_id).put("title", title).put("description", description);
    }

    public Board(int id, int team_id, String title, String description, String created_at) {
        this.id = id;
        this.team_id = team_id;
        this.title = title;
        this.description = description;
        this.created_at = created_at;
        this.jsonObject = new JSONObject().put("team_id", team_id).put("title", title).put("description", description).put("created_at", created_at);
    }


    public Board(int id, int team_id, String title, String description, String created_at, String team_title) {
        this.id = id;
        this.team_id = team_id;
        this.title = title;
        this.description = description;
        this.created_at = created_at;
        this.jsonObject = new JSONObject().put("id", id).put("team_id", team_id).put("title", title).put("description", description).put("created_at", created_at).put("team_title", team_title);
    }

    public static boolean create(int team_id, String title, String description) throws SQLIntegrityConstraintViolationException {
        try {
            PreparedStatement stmt = DBQueryHelper.getConnection().prepareStatement("INSERT INTO `pts`.`boards` (`team_id`, `title`, `description`) VALUES ( ? , ? , ? )", Statement.RETURN_GENERATED_KEYS);
            stmt.setInt(1, team_id);
            stmt.setString(2, title);
            stmt.setString(3, description);
            int keys = stmt.executeUpdate();
            if (keys == 0) {
                return false;
            } else {
                int createdBoardId;
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        createdBoardId = generatedKeys.getInt(1);
                    } else {
                        return false;
                    }
                }

                PreparedStatement listStmt = DBQueryHelper.getConnection().prepareStatement("INSERT INTO `pts`.`lists` (`board_id`, `title`) VALUES (?, ?);");
                for (String listTitle : Constants.List.LIST_TITLES) {
                    listStmt.setInt(1, createdBoardId);
                    listStmt.setString(2, listTitle);
                    listStmt.addBatch();
                    listStmt.clearParameters();
                }
                listStmt.executeBatch();
            }
            return true;
        } catch (SQLIntegrityConstraintViolationException e) {
            e.printStackTrace();
            throw new SQLIntegrityConstraintViolationException("Team Does not exist !");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static JSONObject getData(int board_id) {
        JSONObject board = new JSONObject();
        try {
            PreparedStatement stmt = DBQueryHelper.getPreparedStatement("SELECT * FROM `pts`.`boards` WHERE id = ?");
            stmt.setInt(1, board_id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                board.put("id", rs.getInt("id")).put("team_id", rs.getString("team_id")).put("title", rs.getString("title")).put("description", rs.getString("description")).put("created_at", rs.getString("created_at"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return board;
    }

    public static JSONArray getAllListsData(int board_id) {
        JSONArray lists = new JSONArray();
        try {
            PreparedStatement stmt = DBQueryHelper.getPreparedStatement("SELECT * FROM `pts`.`lists` WHERE board_id = ?");
            stmt.setInt(1, board_id);
            ResultSet rs = stmt.executeQuery();
            PreparedStatement listStmt = DBQueryHelper.getPreparedStatement("SELECT * FROM pts.tasks WHERE is_archived = 0 AND list_id = ?");
            while (rs.next()) {
                JSONObject list = new JSONObject().put("id", rs.getInt("id")).put("board_id", rs.getInt("board_id")).put("title", rs.getString("title")).put("created_at", rs.getString("created_at"));
                JSONArray tasks = new JSONArray();
                listStmt.setInt(1, rs.getInt("id"));
                ResultSet rs1 = listStmt.executeQuery();
                while (rs1.next()) {
                    tasks.put(new JSONObject().put("id", rs1.getInt("id")).put("data", new JSONObject(rs1.getString("data"))).put("created_at", rs1.getString("created_at")).put("is_archived", rs1.getBoolean("is_archived")));
                }
                list.put("tasks", tasks);
                lists.put(list);
                listStmt.clearParameters();
            }
            listStmt.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lists;
    }

    public static boolean belongsToTeam(int board_id, int team_id) {
        boolean belongsTo = false;
        try {
            PreparedStatement stmt = DBQueryHelper.getPreparedStatement("SELECT * FROM `pts`.`boards` WHERE id = ? AND team_id = ? ");
            stmt.setInt(1, board_id);
            stmt.setInt(2, team_id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                belongsTo = true;
            }
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return belongsTo;
    }

    public JSONObject getJsonObject() {
        return jsonObject;
    }

    @Override
    public String toString() {
        return this.jsonObject.toString();
    }
}
