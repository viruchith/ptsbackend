package helpers;

import java.util.ArrayList;
import java.util.regex.Pattern;

public class Validator{

    private String value;

    private boolean isValid;

    private ArrayList<String> errorMessages;

    private static String EMAIL_REGEX_PATTERN = "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$" ;

    public Validator(String value){
        this.value = value ;
        this.isValid = false;
        this.errorMessages = new ArrayList<String>();
    }

    private boolean minLength(String value,int length){
        return (value.length()>=length);
    }

    public Validator minLength(int length,String message){
        if(!this.minLength(this.value,length)){
            this.errorMessages.add(message);
        }

        return this;
    }

    public Validator minLength(int length){
        return this.minLength(length,"Must contain atleast "+length+" characters !");
    }

    private boolean maxLength(String value,int length){
        return (value.length()<=length);
    }

    public Validator maxLength(int length,String message){
        if(!this.maxLength(this.value,length)){
            this.errorMessages.add(message);
        }

        return this;
    }


    public Validator maxLength(int length){
        return this.maxLength(length,"Must not contain more than "+length+" characters !");
    }

    private boolean exactLength(String value,int length){
        return (value.length()==length);
    }

    public Validator exactLength(int length,String message){
        if(!this.exactLength(this.value,length)){
            this.errorMessages.add(message);
        }

        return this;
    }

    public Validator exactLength(int length){
        return this.exactLength(length,"Must contain exactly "+length+" characters !");
    }

    private static boolean isNotEmpty(String value){
        return (!( value==null || value.equals("")));
    }

    public Validator isPresent(String value,String message){
        if(!this.isNotEmpty(value)){
            this.errorMessages.add(message);
        }
        return this;
    }

    public Validator isPresent(String message){
        return this.isPresent(this.value,message);
    }

    public Validator isPresent(){
        return this.isPresent(this.value,"Must not be empty !");
    }

    public static boolean isEmpty(String value){
        return !(isNotEmpty(value));
    }

    public static boolean isValidEmail(String value){
        return Pattern.compile(EMAIL_REGEX_PATTERN,Pattern.CASE_INSENSITIVE).matcher(value).matches();
    }

    public Validator isEmail(String message){
        if(!this.isValidEmail(this.value)){
            this.errorMessages.add(message);
        }
        return this;
    }

    public Validator isEmail(){
        return this.isEmail("Must be a valid email !");
    }

    public static boolean excludes(String value,String[] exclusions){
        for(String exclusion : exclusions ){
            if(value.equals(exclusion)){
                return false;
            }
        }

        return true;
    }

    public Validator excludes(String[] exclusions,String message){
        if(!this.excludes(this.value,exclusions)){
            this.errorMessages.add(message);
        }

        return this;
    }

    public Validator excludes(String[] exclusions){
        return this.excludes(exclusions,String.format("\"%s\" is not allowed !",this.value));
    }

    public static boolean includes(String value,String[] inclusions){
        for(String inclusion : inclusions){
            if(value.equals(inclusion)){
                return true;
            }
        }
        return false;
    }

    public Validator includes(String[] inclusions,String message){
        if(!this.includes(this.value,inclusions)){
            this.errorMessages.add(message);
        }
        return this;
    }

    public Validator includes(String inclusions[]){
        return this.includes(inclusions,String.format("\"%s\" is not allowed !",this.value));
    }

    public boolean isValid(){
        return (this.errorMessages.isEmpty());
    }

    public static boolean matches(String value,String REGEX_PATTERN,int flag){
        return Pattern.compile(REGEX_PATTERN,flag).matcher(value).matches();
    }

    public static boolean matches(String value,String REGEX_PATTERN){
        return Validator.matches(value,REGEX_PATTERN,Pattern.CASE_INSENSITIVE);
    }

    public Validator matches(String REGEX_PATTERN,int flag,String message){
        if(!(this.matches(this.value,REGEX_PATTERN,flag))){
            this.errorMessages.add(message);
        }
        return this;
    }


    public Validator matches(String REGEX_PATTERN,int flag){
        return this.matches(REGEX_PATTERN,flag,"Must match a defined pattern !");
    }

    public Validator matches(String REGEX_PATTERN){
        return this.matches(REGEX_PATTERN,Pattern.CASE_INSENSITIVE);
    }

    public static boolean compareValues(String value1, String value2){
        return value1.equals(value2);
    }

    public Validator compare(String value,String message){
        if(!Validator.compareValues(this.value,value)){
            this.errorMessages.add(message);
        }
        return this;
    }

    public Validator notEquals(String value,String message){
        if(Validator.compareValues(this.value,value)){
            this.errorMessages.add(message);
        }
        return this;
    }

    public static boolean isInteger(String value){
        try{
            Integer.parseInt(value);
            return true;
        }catch(NumberFormatException e){
            return false;
        }
    }

    public Validator isInt(String message){
        if(!Validator.isInteger(this.value)){
            this.errorMessages.add(message);
        }
        return this;
    }

    public Validator isInt(){
        return this.isInt("Must be an Integer !");
    }

    public Validator compare(String value){
        return (this.compare(value,"Not equal !"));
    }

    public ArrayList<String> getErrorMessages(){
        return this.errorMessages;
    }

    public String getValue() {
        return value;
    }
}

