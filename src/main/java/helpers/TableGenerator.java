package helpers;

import java.sql.*;

public class TableGenerator {
    private static Connection getConnection(){
        try{
            Connection conn = DriverManager.getConnection(Constants.DB_CONNECTION_URL);
            conn.setAutoCommit(true);
            return conn;
        }catch(SQLTimeoutException e){
            e.printStackTrace();
        }catch (SQLException e){
            e.printStackTrace();
        }
        return null;
    }

    public static void run(){
        Connection conn = TableGenerator.getConnection();

        if(conn!=null){
            try {
                Statement stmt = conn.createStatement();
                // USERS TABLE
                stmt.addBatch("CREATE TABLE IF NOT EXISTS `pts`.`users` ( `id` INT NOT NULL AUTO_INCREMENT , `username` VARCHAR(15) NOT NULL , `name` VARCHAR(256) NOT NULL , `email` VARCHAR(100) NOT NULL , `password` VARCHAR(256) NOT NULL , `password_hint` VARCHAR(256) NOT NULL , `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP , PRIMARY KEY (`id`), UNIQUE (`username`))");
                // TEAMS TABLE
                stmt.addBatch("CREATE TABLE IF NOT EXISTS `pts`.`teams` ( `id` INT NOT NULL AUTO_INCREMENT, `owner_id` INT NOT NULL, `title` VARCHAR(256) NOT NULL, `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, PRIMARY KEY (`id`), INDEX `owner_id_idx` (`owner_id` ASC) VISIBLE, CONSTRAINT `teams_owner_id` FOREIGN KEY (`owner_id`) REFERENCES `pts`.`users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE)");
                // TEAM MEMBERS TABLE
                stmt.addBatch("CREATE TABLE IF NOT EXISTS `pts`.`team_members` ( `team_id` INT NOT NULL,  `user_id` INT NOT NULL,  PRIMARY KEY (`team_id`, `user_id`),  INDEX `member_user_id_fkey_idx` (`user_id` ASC) VISIBLE,  CONSTRAINT `member_team_id_fkey` FOREIGN KEY (`team_id`)  REFERENCES `pts`.`teams` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,  CONSTRAINT `member_user_id_fkey` FOREIGN KEY (`user_id`) REFERENCES `pts`.`users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE)");
                // Boards Table
                stmt.addBatch("CREATE TABLE IF NOT EXISTS `pts`.`boards` ( `id` INT NOT NULL AUTO_INCREMENT, `team_id` INT NOT NULL, `title` VARCHAR(50) NOT NULL, `description` VARCHAR(256) NOT NULL, `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP(), PRIMARY KEY (`id`), INDEX `boards_team_fkey_idx` (`team_id` ASC) VISIBLE, CONSTRAINT `boards_team_fkey` FOREIGN KEY (`team_id`) REFERENCES `pts`.`teams` (`id`) ON DELETE CASCADE ON UPDATE CASCADE)");
                // Lists Table
                stmt.addBatch("CREATE TABLE IF NOT EXISTS `lists` ( `id` int(11) NOT NULL AUTO_INCREMENT, `board_id` int(11) NOT NULL, `title` varchar(50) NOT NULL, `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP, PRIMARY KEY (`id`), KEY `board_fkey_idx` (`board_id`), CONSTRAINT `board_fkey` FOREIGN KEY (`board_id`) REFERENCES `boards` (`id`) ON DELETE CASCADE ON UPDATE CASCADE ) ");
                //TASKS Table
                stmt.addBatch("CREATE TABLE IF NOT EXISTS `tasks` ( `id` int(11) NOT NULL AUTO_INCREMENT, `list_id` int(11) NOT NULL, `data` tinytext NOT NULL, `is_archived` tinyint(4) NOT NULL DEFAULT '0', `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP, PRIMARY KEY (`id`), KEY `task_list_fkey_idx` (`list_id`), CONSTRAINT `task_list_fkey` FOREIGN KEY (`list_id`) REFERENCES `lists` (`id`) ON DELETE CASCADE ON UPDATE CASCADE ) ");
                //BUGTRACKERS TABLE
                stmt.addBatch("CREATE TABLE IF NOT EXISTS `pts`.`bugtrackers` ( `id` INT NOT NULL AUTO_INCREMENT, `team_id` INT NOT NULL, `title` VARCHAR(50) NOT NULL, `description` VARCHAR(512) NOT NULL, `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP(), PRIMARY KEY (`id`), INDEX `bugtacker_team_fkey_idx` (`team_id` ASC) VISIBLE, CONSTRAINT `bugtacker_team_fkey` FOREIGN KEY (`team_id`) REFERENCES `pts`.`teams` (`id`) ON DELETE CASCADE ON UPDATE CASCADE)");
                //BUGS TABLE
                stmt.addBatch("CREATE TABLE IF NOT EXISTS `pts`.`bugs` ( `id` INT NOT NULL AUTO_INCREMENT, `bugtracker_id` INT NOT NULL, `title` VARCHAR(50) NOT NULL, `description` VARCHAR(45) NOT NULL, `severity_level` TINYINT(1) NOT NULL, `is_closed` TINYINT NOT NULL, `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP(), PRIMARY KEY (`id`), INDEX `bugs_bugtrackers_fkey_idx` (`bugtracker_id` ASC) VISIBLE, CONSTRAINT `bugs_bugtrackers_fkey` FOREIGN KEY (`bugtracker_id`) REFERENCES `pts`.`bugtrackers` (`id`) ON DELETE CASCADE ON UPDATE CASCADE)");
                stmt.executeBatch();
                stmt.clearBatch();
                stmt.closeOnCompletion();
                conn.close();
            }catch(SQLException e){
                e.printStackTrace();
            }
        }

    }
}
