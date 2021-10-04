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
                stmt.addBatch("CREATE TABLE IF NOT EXISTS `pts`.`lists` ( `id` INT NOT NULL AUTO_INCREMENT, `board_id` INT NOT NULL, `title` VARCHAR(50) NOT NULL, `tasks` MEDIUMTEXT CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_unicode_ci' NOT NULL, PRIMARY KEY (`id`), INDEX `board_fkey_idx` (`board_id` ASC) VISIBLE, CONSTRAINT `board_fkey` FOREIGN KEY (`board_id`) REFERENCES `pts`.`boards` (`id`) ON DELETE CASCADE ON UPDATE CASCADE)");
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
