package appexceptions;

public class ObjectDoesNotExistException extends Exception{
    public ObjectDoesNotExistException(String errorMessage){
        super(errorMessage);
    }
}
