package helpers;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.json.JSONObject;

import java.util.Map;

public class Authenticator {
    private static final String secret = "Secret code hehe";
    private static final Algorithm algorithm = Algorithm.HMAC512(Authenticator.secret);

    public static String createToken(Map payload){
        String token = null;
        try {
            token = JWT.create().withPayload(payload).sign(Authenticator.algorithm);

        } catch (JWTCreationException exception){
            exception.printStackTrace();
        }
        return token;
    }

    public static JSONObject verifyToken(String token){
        try {
            JWTVerifier verifier = JWT.require(Authenticator.algorithm).build(); //Reusable verifier instance
            DecodedJWT jwt = verifier.verify(token);
            return new JSONObject().put("id",jwt.getClaim("id").asString()).put("username",jwt.getClaim("username").asString());
        } catch (JWTVerificationException exception){
            //Invalid signature/claims
            return null;
        }
    }
}
