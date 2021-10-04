package team;

import appexceptions.ObjectAlreadyExistsException;
import appexceptions.ObjectDoesNotExistException;
import helpers.DBQueryHelper;

import java.sql.*;

public class TeamMember {
    public static boolean add(int team_id, String username) throws ObjectAlreadyExistsException, ObjectDoesNotExistException {
        try {
            PreparedStatement stmt = DBQueryHelper.getPreparedStatement("INSERT INTO `pts`.`team_members` (`team_id`,`user_id`) VALUES (? , (SELECT `id` FROM `pts`.`users` WHERE `username` = ? LIMIT 1) )");
            stmt.setInt(1, team_id);
            stmt.setString(2, username);
            stmt.execute();
            stmt.close();
            return true;
        } catch (SQLIntegrityConstraintViolationException e) {
            int error_code = e.getErrorCode();
            if (error_code == 1062) {
                throw new ObjectAlreadyExistsException("User already present in the team !");
            } else {
                throw new ObjectDoesNotExistException("Invalid team or user Id !");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
