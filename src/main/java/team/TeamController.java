package team;

import appexceptions.ObjectAlreadyExistsException;
import appexceptions.ObjectDoesNotExistException;
import helpers.*;
import org.json.JSONArray;
import spark.Request;
import spark.Response;
import spark.Route;

import org.json.JSONObject;
import org.json.JSONException;

import java.sql.PreparedStatement;
import java.sql.SQLException;


public class TeamController {

    public static Route createTeam = (Request req , Response res)->{
        res.type("application/json");

        JSONObject teamData,tokenData,errors;

        String token = req.headers("Auth-Token");

        if(token==null){
            return new ResponseObject(false,"Missing Auth Token !");
        }


        tokenData = Authenticator.verifyToken(token);


        if(tokenData==null){
            return new ResponseObject(false,"Invalid Token !");
        }

        try{
            teamData = new JSONObject(req.body());
        }catch (JSONException e){
            return new ResponseObject(false,"Invalid Data !");
        }

        if(teamData.isEmpty() || !teamData.has("title")){
            return new ResponseObject(false,"Missing data !");
        }


        Validator title = new Validator(teamData.getString("title")).isPresent().matches(Constants.Team.TEAM_TITLE_REGEX).minLength(2).maxLength(25);

        if (!title.isValid()){
            errors = new JSONObject().put("title",new JSONArray(title.getErrorMessages()));
            return new ResponseObject(false,new JSONObject().put("message","Invalid Title !").put("errors",errors));
        }

        if(Team.create(tokenData.getInt("id"),title.getValue())){
            Team team = Team.getLastCreatedTeamOfUser(tokenData.getInt("id"));
            //ADD OWNER as team member after creation of team
            TeamMember.add(team.getId(), team.getOwner_id());
            if (team == null) {
                return new ResponseObject(true, "Created Successfully !");
            } else {
                return new ResponseObject(true, new JSONObject().put("message", "Created Successfully !").put("team", team.getJsonObject()));
            }
        }else{
            return new ResponseObject(false,"Unable to create team !");
        }

    };


    public static Route addTeamMember = (Request req, Response res)->{
        res.type("application/json");
        JSONObject memberData,tokenData,errors;

        String token = req.headers("Auth-Token");

        if(token==null){
            return new ResponseObject(false,"Missing Auth Token !");
        }


        tokenData = Authenticator.verifyToken(token);


        if(tokenData==null){
            return new ResponseObject(false,"Invalid Token !");
        }

        try{
            memberData = new JSONObject(req.body());
        }catch(JSONException e){
            return new ResponseObject(false,"Invalid Data !");
        }

        if( memberData.isEmpty() || !memberData.has("team_id") || !memberData.has("member_id") ){
            return new ResponseObject(false,"Missing Data !");
        }

        Validator team_id = new Validator(memberData.getString("team_id")).isPresent().isInt().maxLength(11);
        Validator member_id = new Validator(memberData.getString("member_id")).isPresent().isInt().maxLength(11);

        errors = new JSONObject();

        if(!team_id.isValid()){
            errors.put("team_id",new JSONArray(team_id.getErrorMessages()));
        }

        if(!member_id.isValid()){
            errors.put("member_id",new JSONArray(member_id.getErrorMessages()));
        }

        if(!errors.isEmpty()){
            return new ResponseObject(false,new JSONObject().put("errors",errors));
        }

        Team team = Team.getTeamById(Integer.parseInt(team_id.getValue()));

        if(team==null){
            return new ResponseObject(false,"Invalid Team Id !");
        }

        // Only owner can add the member
        if(team.getOwner_id() != tokenData.getInt("id")){
            return new ResponseObject(false,"Unauthorized Action !");
        }

        try{
            if(TeamMember.add(Integer.parseInt(team_id.getValue()),Integer.parseInt(member_id.getValue()))){
                return new ResponseObject(true,"Added successfully !");
            }else{
                return new ResponseObject(false,"Unable to add to team");
            }
        }catch(ObjectDoesNotExistException e){
            return new ResponseObject(false,e.getMessage());
        }catch(ObjectAlreadyExistsException e){
            return new ResponseObject(false,e.getMessage());
        }

    };

    public static Route removeTeamMember = (Request req, Response res)->{
        res.type("application/json");

        JSONObject tokenData,errors;

        String token = req.headers("Auth-Token");

        if(token==null){
            return new ResponseObject(false,"Missing Auth Token !");
        }


        tokenData = Authenticator.verifyToken(token);


        if(tokenData==null){
            return new ResponseObject(false,"Invalid Token !");
        }

        errors = new JSONObject();

        Validator team_id = new Validator(req.params("team_id")).isInt(), member_id = new Validator(req.params("member_id")).isInt();
        if(!team_id.isValid()){
            errors.put("team_id",new JSONArray(team_id.getErrorMessages()));
        }
        if(!member_id.isValid()){
            errors.put("member_id",new JSONArray(member_id.getErrorMessages()));
        }

        if(!errors.isEmpty()){
            return new ResponseObject(false,new JSONObject().put("message","Invalid data !").put("errors",errors));
        }

        Team team = Team.getTeamById(Integer.parseInt(team_id.getValue()));

        if(team==null){
            return new ResponseObject(false,"Invalid Team Id !");
        }
        // Only owner can delete the member
        if(team.getOwner_id() != tokenData.getInt("id")){
            return new ResponseObject(false,"Unauthorized Action !");
        }

        if (team.getOwner_id() == Integer.parseInt(member_id.getValue())){
            return new ResponseObject(false,"Team Owner Cannot be deleted !");
        }



        try {
            PreparedStatement stmt = DBQueryHelper.getPreparedStatement("DELETE FROM `pts`.`team_members` WHERE team_id = ? AND user_id = ? ");
            stmt.setInt(1,Integer.parseInt(team_id.getValue()));
            stmt.setInt(2,Integer.parseInt(member_id.getValue()));
            stmt.execute();
            return new ResponseObject(true,"Deleted Successfully !");
        }catch(SQLException e){
            e.printStackTrace();
            return new ResponseObject(false,"Error Deleting user !");
        }
    };

}
