package user;

import board.Board;
import bugtracker.BugTracker;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.*;
import java.util.ArrayList;

import helpers.PasswordHasher;
import appexceptions.ObjectAlreadyExistsException;
import helpers.DBQueryHelper;
import team.Team;

public class User {
    private int id;
    private String username,name,email,password,password_hint,created_at;
    private JSONObject userObj;

    public User(String username,String name,String email,String password,String password_hint){
        this.username = username;
        this.name = name;
        this.email = email;
        this.password = password;
        this.password_hint = password_hint;
        this.userObj = new JSONObject().put("username",username).put("name",name).put("email",email).put("password",password).put("password_hint",password_hint);
    }

    public User(int id,String username,String name,String email,String password,String password_hint,String created_at){
        this.id=id;
        this.username = username;
        this.name = name;
        this.email = email;
        this.password = password;
        this.password_hint = password_hint;
        this.created_at = created_at;
        this.userObj = new JSONObject().put("id",id).put("username",username).put("name",name).put("email",email).put("password",password).put("password_hint",password_hint);
    }

    public boolean create() throws ObjectAlreadyExistsException{
        try{
            PreparedStatement statement = DBQueryHelper.getPreparedStatement("INSERT INTO users (username,name,email,password,password_hint) VALUES(?,?,?,?,?)");
            statement.setString(1,this.username);
            statement.setString(2,this.name);
            statement.setString(3,this.email);
            statement.setString(4,PasswordHasher.toSHA512(this.password));
            statement.setString(5,this.password_hint);
            statement.execute();
            statement.close();
            return true;
        }catch (SQLException e){
            if(e.getErrorCode()==1062){
                throw new ObjectAlreadyExistsException("username already exists!");

            }else{
                e.printStackTrace();
            }
            return false;
        }
    }



    public static User getFirstByUsername(String username){
        try {
            PreparedStatement stmt = DBQueryHelper.getPreparedStatement("SELECT * FROM users WHERE username = ? LIMIT 1");
            stmt.setString(1,username);
            ResultSet rs = stmt.executeQuery();
            if(rs.next()){
                int id = rs.getInt("id");
                String name = rs.getString("name"),email = rs.getString("email"),password = rs.getString("password"),password_hint = rs.getString("password_hint"),created_at = rs.getString("created_at");
                stmt.close();
                return new User(id,username,name,email,password,password_hint,created_at);
            }else{
                return null;
            }
        }catch(SQLTimeoutException e){
            e.printStackTrace();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }



    public static User getFirstById(int id){
        try {
            PreparedStatement stmt = DBQueryHelper.getPreparedStatement("SELECT * FROM users WHERE id = ? LIMIT 1");
            stmt.setInt(1,id);
            ResultSet rs = stmt.executeQuery();
            if(rs.next()){
                String username = rs.getString("username") , name = rs.getString("name"),email = rs.getString("email"),password = rs.getString("password"),password_hint = rs.getString("password_hint"),created_at = rs.getString("created_at");
                stmt.close();
                return new User(id,username,name,email,password,password_hint,created_at);
            }else{
                return null;
            }
        }catch(SQLTimeoutException e){
            e.printStackTrace();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }catch(Exception e){
            e.printStackTrace();
        }

        return null;
    }


    public static boolean updateUserPassword(int id,String new_password,String new_password_hint){
        try {
            PreparedStatement stmt = DBQueryHelper.getPreparedStatement("UPDATE users SET password = ? , password_hint = ? WHERE id = ? ");
            stmt.setString(1,PasswordHasher.toSHA512(new_password));
            stmt.setString(2,new_password_hint);
            stmt.setInt(3,id);
            stmt.execute();
            stmt.close();

            return true;
        }catch(SQLTimeoutException e){
            e.printStackTrace();
            return false;
        }
        catch (SQLException e) {
            e.printStackTrace();
            return false;
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }

    }

    public boolean updatePassword(String new_password,String new_password_hint){
        return User.updateUserPassword(this.id,new_password,new_password_hint);
    }

    public static boolean updateAccountDetails(int id ,String username,String name,String email) throws ObjectAlreadyExistsException{
        try {
            PreparedStatement stmt = DBQueryHelper.getPreparedStatement("UPDATE users SET username = ? , name = ? , email = ? WHERE id = ? ");
            stmt.setString(1,username);
            stmt.setString(2,name);
            stmt.setString(3,email);
            stmt.setInt(4,id);
            stmt.execute();
            stmt.close();
            return true;
        }catch(SQLTimeoutException e){
            e.printStackTrace();
            return false;
        }catch(SQLIntegrityConstraintViolationException e){
            //TODO Email constraint violation
            System.out.println(e.getMessage());
            throw new ObjectAlreadyExistsException("Username \""+username+"\" already taken !");
        }
        catch (SQLException e) {
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }

    public boolean updateAccountDetails(String username, String name, String email) throws ObjectAlreadyExistsException {
        return User.updateAccountDetails(this.id, username, name, email);
    }

    public static JSONArray getTeams(int user_id) {
        try {
            PreparedStatement stmt = DBQueryHelper.getPreparedStatement("SELECT `pts`.`teams`.id,`pts`.`teams`.owner_id,`pts`.`teams`.title,`pts`.`teams`.created_at,`pts`.`users`.username AS owner_username, `pts`.`users`.name AS owner_name FROM `pts`.`teams` INNER JOIN `pts`.`users` ON `pts`.`users`.id = `pts`.`teams`.owner_id  WHERE `pts`.`teams`.id IN ( SELECT team_id FROM `pts`.`team_members` WHERE user_id =  ?  ) ORDER BY created_at DESC");
            stmt.setInt(1, user_id);
            ResultSet rs = stmt.executeQuery();
            JSONArray teams = new JSONArray();
            while (rs.next()) {
                teams.put(new JSONObject().put("id", rs.getInt("id")).put("owner", new JSONObject().put("id", rs.getInt("owner_id")).put("username", rs.getString("owner_username")).put("name", rs.getString("owner_name"))).put("title", rs.getString("title")).put("created_at", rs.getString("created_at")));
            }
            stmt.close();
            return teams;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean isMemberOfTeam(int team_id, int user_id) {
        try {
            PreparedStatement stmt = DBQueryHelper.getPreparedStatement("SELECT * FROM pts.team_members WHERE team_id = ? AND user_id = ?");
            stmt.setInt(1, team_id);
            stmt.setInt(2, user_id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }


    public static boolean isOwnerOfTeam(int team_id, int owner_id) {
        try {
            PreparedStatement stmt = DBQueryHelper.getPreparedStatement("SELECT * FROM pts.teams WHERE id = ? AND owner_id = ?");
            stmt.setInt(1, team_id);
            stmt.setInt(2, owner_id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }


    public static JSONArray getAllBoards(int user_id) {
        JSONArray boards = new JSONArray();
        try {
            PreparedStatement stmt = DBQueryHelper.getPreparedStatement("SELECT pts.boards.id,pts.boards.team_id,pts.boards.title,pts.boards.description,pts.boards.created_at,pts.teams.title AS team_title FROM pts.boards INNER JOIN pts.teams ON pts.boards.team_id = pts.teams.id WHERE team_id IN (SELECT team_id FROM pts.team_members WHERE user_id = ?)");
            stmt.setInt(1, user_id);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                boards.put(new Board(rs.getInt("id"), rs.getInt("team_id"), rs.getString("title"), rs.getString("description"), rs.getString("created_at"), rs.getString("team_title")).getJsonObject());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return boards;
    }

    public static JSONArray getAllBugTrackers(int user_id) {
        JSONArray bugtrackers = new JSONArray();
        try {
            PreparedStatement stmt = DBQueryHelper.getPreparedStatement("SELECT `pts`.`bugtrackers`.id , `pts`.`bugtrackers`. team_id,`pts`.`bugtrackers`.title,`pts`.`bugtrackers`.description,`pts`.`bugtrackers`.created_at,`pts`.`teams`.owner_id AS team_owner_id,`pts`.`teams`.title AS team_title FROM pts.bugtrackers INNER JOIN pts.teams ON `pts`.`bugtrackers`.team_id = `pts`.`teams`.id WHERE `pts`.`bugtrackers`.team_id IN ( SELECT team_id FROM `pts`.`team_members` WHERE `pts`.`team_members`.user_id = ? )  ORDER BY `pts`.`bugtrackers`.created_at DESC ");
            stmt.setInt(1, user_id);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                bugtrackers.put(new BugTracker(rs.getInt("id"), rs.getInt("team_id"), rs.getString("title"), rs.getString("description"), rs.getString("created_at"), new JSONObject().put("id", rs.getInt("team_id")).put("owner_id", rs.getInt("team_owner_id")).put("title", rs.getString("team_title"))).getJsonObject());
            }
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return bugtrackers;
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getPassword_hint() {
        return password_hint;
    }

    public String toJSONString(){
        return this.userObj.toString();
    }

    public JSONObject getUserObj() {
        return userObj;
    }

    @Override
    public String toString(){
        return this.toJSONString();
    }

}
