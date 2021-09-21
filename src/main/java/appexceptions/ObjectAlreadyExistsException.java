package appexceptions;
import java.lang.Exception;
public class ObjectAlreadyExistsException extends Exception{
    public ObjectAlreadyExistsException(String errorMessage){
        super(errorMessage);

}}
