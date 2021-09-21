package helpers;

public class Constants {
    public static final String DB_CONNECTION_URL = "jdbc:mysql://localhost:3306/pts?user=root&password=toor";
    public static final String APP_TEMPLATES_FOLDER = "src/main/resources/templates/";
    public  class User{
        public static final String USER_NAME_REGEX = "^[a-zA-z][a-zA-Z0-9_]{4,14}$";
    }

    public class Team{
        public static final  String TEAM_TITLE_REGEX = "(([a-zA-Z0-9\\-]+)+\\s*(([a-zA-Z0-9\\-])*\\s*))";
    }
}
