package user;

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
            return teams;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
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
