package helpers;
import org.json.JSONObject;
import org.json.JSONException;


public class ResponseObject {
    private JSONObject response;

    public ResponseObject(boolean success, String message) {
        this.response = new JSONObject().put("success", success).put("message", message);

    }

    public ResponseObject(boolean success,JSONObject obj){
        this.response = obj.put("success",success);
    }

    @Override
    public String toString(){
       return this.response.toString();

    }




}
