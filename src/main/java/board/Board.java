package board;

import appexceptions.ObjectAlreadyExistsException;
import appexceptions.ObjectDoesNotExistException;
import helpers.DBQueryHelper;
import org.json.JSONObject;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;

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
    }

    public static boolean create(int team_id, String title, String description) throws SQLIntegrityConstraintViolationException {
        try {
            PreparedStatement stmt = DBQueryHelper.getPreparedStatement("INSERT INTO `pts`.`boards` (`team_id`, `title`, `description`) VALUES ( ? , ? , ? )");
            stmt.setInt(1, team_id);
            stmt.setString(2, title);
            stmt.setString(3, description);
            stmt.execute();
            stmt.close();
            return true;
        } catch (SQLIntegrityConstraintViolationException e) {
            e.printStackTrace();
            throw new SQLIntegrityConstraintViolationException("Team Does not exist !");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public String toString() {
        return this.jsonObject.toString();
    }
}
