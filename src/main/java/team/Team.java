package team;

import java.sql.*;

import helpers.DBQueryHelper;
import org.json.JSONObject;

public class Team {
    private int id , owner_id;
    private String title,created_at;
    private JSONObject jsonObject;

    public Team(int owner_id,String title){
        this.owner_id = owner_id;
        this.title = title;
        this.jsonObject = new JSONObject().put("owner_id",owner_id).put("title",title);
    }

    public Team(int id,int owner_id,String title,String created_at){
        this.id = id;
        this.owner_id = owner_id;
        this.title = title;
        this.created_at = created_at;
        this.jsonObject = new JSONObject().put("id",id).put("owner_id",owner_id).put("title",title).put("created_at",created_at);
    }

    public int getId() {
        return id;
    }

    public int getOwner_id() {
        return owner_id;
    }

    public String getTitle() {
        return title;
    }

    public String getCreated_at() {
        return created_at;
    }

    public JSONObject getJsonObject() {
        return jsonObject;
    }

    @Override
    public String toString(){
        return this.jsonObject.toString();
    }

    public static boolean create(int owner_id, String title){
        try{
            PreparedStatement statement = DBQueryHelper.getPreparedStatement("INSERT INTO teams (owner_id,title) VALUES(?,?)");
            statement.setInt(1,owner_id);
            statement.setString(2,title);
            statement.execute();
            statement.getConnection().close();
            statement.close();
            return true;
        }catch(SQLException e){
            e.printStackTrace();
            return false;
        }
    }

    public boolean create(){
        return Team.create(this.owner_id,this.title);
    }

    public static Team getLastCreatedTeamOfUser(int owner_id){
        try{
            PreparedStatement statement = DBQueryHelper.getPreparedStatement("SELECT * FROM `pts`.`teams` WHERE `owner_id` = ? ORDER BY `id` DESC LIMIT 1 ");
            statement.setInt(1,owner_id);

            ResultSet rs = statement.executeQuery();

            if(rs.next()){
                int id = rs.getInt("id");
                String title = rs.getString("title") , created_at = rs.getString("created_at");
                statement.close();
                return new Team(id,owner_id,title,created_at);
            }

        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public static Team getTeamById(int team_id){
        try{
            PreparedStatement statement = DBQueryHelper.getPreparedStatement("SELECT * FROM `pts`.`teams` WHERE `id` =  ?");
            statement.setInt(1,team_id);
            ResultSet rs = statement.executeQuery();
            if(rs.next()){
                int id = rs.getInt("id") , owner_id = rs.getInt("owner_id");
                String title = rs.getString("title") , created_at = rs.getString("created_at");
                return new Team(id,owner_id,title,created_at);
            }else{
                return null;
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
