package webappexceptions;

public class TemplateNotFoundException extends Exception{
    public TemplateNotFoundException(String errorMessage){
        super(errorMessage);
    }
}
