package vpt.backbone.backend.app.utils;

import java.sql.SQLException;
//
//import static tools.DBTools.executeUpdate;
//
//import tools.DBTools;
//import vpt.backbone.backend.app.tasks.BasicQueueTask;
//import vpt.backbone.backend.app.tasks.TaskExecutingLongMathOperationAndExecutesOK;
//import vpt.backbone.backend.app.tasks.TaskReturningError;
//import vpt.backbone.backend.app.tasks.TaskThrowingUnhandledException;

import tools.DBTools;
import vpt.backbone.backend.app.tasks.BasicQueueTask;
import vpt.backbone.backend.app.tasks.TaskExecutingLongMathOperationAndExecutesOK;
import vpt.backbone.backend.app.tasks.TaskReturningError;
import vpt.backbone.backend.app.tasks.TaskThrowingUnhandledException;

public class DBToolsVariation extends DBTools
{
   final static String url = "jdbc:mysql://localhost:3306";
   final static String rootUser = "it_admin";
   final static String rootPass = "generic_password_it";
   
   final static String testUser = "user_it";
   final static String testPass = "pass_it";
   
   final static String databaseName = "vpt_it_database";
   
   public static void createPayloadTables(String dbname) throws SQLException {
      executeUpdate("CREATE TABLE if not exists vpt_multiple_tasks_queue (id BIGINT(20) NOT NULL AUTO_INCREMENT,task_name VARCHAR(255) DEFAULT NULL, owner_id BIGINT(20) DEFAULT NULL,status VARCHAR(255) DEFAULT NULL,result_detail LONGTEXT DEFAULT NULL,attempts INT(11) DEFAULT NULL,creation_time DATETIME DEFAULT NULL,last_update DATETIME DEFAULT NULL,PRIMARY KEY (id)) ENGINE = INNODB, CHARACTER SET utf8mb4, COLLATE utf8mb4_unicode_520_ci;", dbname);
      executeUpdate("create table " + BasicQueueTask.class.getSimpleName() + "Payload" + "(id bigint primary key auto_increment not null, task_id bigint, payload_data longtext, foreign key (task_id) references vpt_multiple_tasks_queue(id));", dbname);
      executeUpdate("create table " + TaskExecutingLongMathOperationAndExecutesOK.class.getSimpleName() + "Payload" + "(id bigint primary key auto_increment not null, task_id bigint, payload_data longtext, foreign key (task_id) references vpt_multiple_tasks_queue(id));", dbname);
      executeUpdate("create table " + TaskReturningError.class.getSimpleName() + "Payload" + "(id bigint primary key auto_increment not null, task_id bigint, payload_data longtext, foreign key (task_id) references vpt_multiple_tasks_queue(id));", dbname);
      executeUpdate("create table " + TaskThrowingUnhandledException.class.getSimpleName() + "Payload" + "(id bigint primary key auto_increment not null, task_id bigint, payload_data longtext, foreign key (task_id) references vpt_multiple_tasks_queue(id));", dbname);
   }
}
