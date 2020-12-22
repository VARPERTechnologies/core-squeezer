package vpt.backbone.backend.app.testing.it;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.sql.Timestamp;

import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import com.mysql.cj.jdbc.exceptions.CommunicationsException;

import classes.only.testing.CarDefinitionEntityValidatorTesting;
import vpt.backbone.backend.app.DBApp;
import vpt.backbone.backend.app.entities.MultipleTasksQueue;
import vpt.backbone.backend.app.exceptions.MissingMandatoryPropertyException;

import static vpt.backbone.backend.app.utils.DBToolsVariation.resetDb;


public class DBAppIT
{
   private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
   private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
   private final PrintStream originalOut = System.out;
   private final PrintStream originalErr = System.err;
   
//   private final InputStream originalIn = System.in;
   
   SessionFactory sf;

   @BeforeAll
   public static void initConfigs() throws ClassNotFoundException, SQLException, CommunicationsException, IOException
   {
      Runtime.getRuntime().exec("sc start MySQL80");
//      Assertions.assertTrue(isDatabaseOnline());
   }

   @BeforeEach
   public void beforeTasks()
   {
      // Assertions.assertNotNull(sf);
      outContent.reset();
      errContent.reset();
      System.setOut(new PrintStream(outContent));
      System.setErr(new PrintStream(errContent));
   }
   
   public void afterTasks()
   {
      System.setOut(originalOut);
      System.setErr(originalErr);
   }

   @DisplayName("Calling tryOpenConnection should return not null Session object when passing correct connection arguments")
   @Test
   @Timeout(10)
   public void tryOpenConnection_OpenConfigFile_OpenDB() throws ParseException, SQLException, IOException, URISyntaxException
   {
      // Arrange
      DBApp app;
      String dbname = "it_tryOpenConnection_OpenConfigFile_OpenDB";
      
      Session s;
      String[] args = new String[] {"--server", "localhost", "--db", dbname, "--user", "user_it", "--pass", "pass_it"};
      
      resetDb(dbname);
      
      // Act
      app = new DBApp();
      app.parseArgs(args);
      app.build();
      s = app.buildSession();

      // Assert
      Assertions.assertNotNull(s);
   }
   
   @DisplayName("Creating a new application with no arguments should connect to a database with default config file")
   @Test
   @Timeout(value = 10)
   public void build_WithNoArgs_OpenDBWithConfigFile() throws ParseException, SQLException, IOException, URISyntaxException, MissingMandatoryPropertyException
   {
      // Arrange
      DBApp app;
      String dbname = "it_" + new Object() {}.getClass().getEnclosingMethod().getName();
      
      resetDb(dbname);
      
      // Act
      app = new DBApp();
      
      // Assert
      Assertions.assertDoesNotThrow(() ->
      {
         app.loadConfigFile(Paths.get(app.getHomeDir().getAbsolutePath(), "config", dbname + ".properties").toString());
         app.build();
      });
   }
   
   @DisplayName("Default application with missing database connection parameters in config file should throw exception")
   @Test
   @Timeout(value = 10)
   public void buildDefault_MissingDatabaseConfig_throwsException() throws ParseException, SQLException, IOException, URISyntaxException
   {
      // Arrange
      DBApp app;
      String dbname = "it_" + new Object() {}.getClass().getEnclosingMethod().getName();
      
      resetDb(dbname);
      
      // Act
      app = new DBApp();
      
      // Assert
      Assertions.assertThrows(MissingMandatoryPropertyException.class, () ->
      {
         app.loadConfigFile(Paths.get(app.getHomeDir().getAbsolutePath(), "config", dbname + ".properties").toString());
         app.build();
      });
   }
   
   @DisplayName("Default application with missing password connection parameters in config file should throw exception")
   @Test
   @Timeout(value = 10)
   public void buildDefault_MissingPasswordConfig_throwsException() throws ParseException, SQLException, IOException, URISyntaxException
   {
      // Arrange
      DBApp app;
      String dbname = "it_" + new Object() {}.getClass().getEnclosingMethod().getName();
      
      resetDb(dbname);
      
      // Act
      app = new DBApp();
      
      // Assert
      Assertions.assertThrows(MissingMandatoryPropertyException.class, () ->
      {
         app.loadConfigFile(Paths.get(app.getHomeDir().getAbsolutePath(), "config", dbname + ".properties").toString());
         app.build();
      });
   }

   @DisplayName("Default application with missing user connection parameters in config file should throw exception")
   @Test
   @Timeout(value = 10)
   public void buildDefault_MissingUserConfig_throwsException() throws ParseException, SQLException, IOException, URISyntaxException
   {
      // Arrange
      DBApp app;
      String dbname = "it_" + new Object() {}.getClass().getEnclosingMethod().getName();
      
      resetDb(dbname);
      
      // Act
      app = new DBApp();
      
      // Assert
      Assertions.assertThrows(MissingMandatoryPropertyException.class, () ->
      {
         app.loadConfigFile(Paths.get(app.getHomeDir().getAbsolutePath(), "config", dbname + ".properties").toString());
         app.build();
      });
   }
   
   @DisplayName("Default application with missing host connection parameters in config file should throw exception")
   @Test
   @Timeout(value = 10)
   public void buildDefault_MissingHostConfig_throwsException() throws ParseException, SQLException, IOException, URISyntaxException
   {
      // Arrange
      DBApp app;
      String dbname = "it_" + new Object() {}.getClass().getEnclosingMethod().getName();
      
      resetDb(dbname);
      
      // Act
      app = new DBApp();
      
      // Assert
      Assertions.assertThrows(MissingMandatoryPropertyException.class, () ->
      {
         app.loadConfigFile(Paths.get(app.getHomeDir().getAbsolutePath(), "config", dbname + ".properties").toString());
         app.build();
      });
   }
      
   @DisplayName("Only for testing purposes, ignore it")
   @Disabled
   @Test
   @Timeout(value = 10)
   public void test2() throws ParseException, IOException, InterruptedException
   {
      // Arrange
      DBApp app;
      Session s;
      String[] args = new String[] { "--server", "localhost", "--db", "vpt_it_database", "--user", "user_it", "--pass",
            "pass_it" };
      String outputP;
//      ByteArrayInputStream inContent = new ByteArrayInputStream(pass.getBytes());

      // Act
      app = new DBApp();
      app.parseArgs(args);
      app.build();
      s = app.buildSession();
      
      Timestamp res1 = (Timestamp) s.createNativeQuery("select now()").getResultList().get(0);
      
//      System.setIn(inContent);
//      Process p = Runtime.getRuntime().exec("runas /user:\"" + user + "\" \"sc.exe stop MySQL80\"");
//      Process p = Runtime.getRuntime().exec("runas /profile /user:\"" + user + "\" \"cmd\"");
//      Process p = Runtime.getRuntime().exec("cmd");
      Process p = Runtime.getRuntime().exec("sc.exe stop MySQL80");
      BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
      p.waitFor();
      String str = null;
      while ((str = stdInput.readLine()) != null) {
         System.out.println(str);
      }
//      p.waitFor();
      
//      try(BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(p.getOutputStream())))
//      {
//      BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()));
//      bw.write(pass);
//      bw.newLine();
//      p.waitFor();
      outputP = outContent.toString();   
//      };
      
      Timestamp res2 = (Timestamp) s.createNativeQuery("select now()").getResultList().get(0);

      // Assert
      Assertions.assertNotNull(s);
   }

   @DisplayName("--checkdb should show missing problems")
   @Test
   @Timeout(value = 10)
   final void parseArgs_withCheckDB_ShouldCreateAlerts() throws SQLException, IOException, URISyntaxException
   {
      int actual;
      int expected;
      String output;
      String dbname = "it_parseArgs_withCheckDB_ShouldCreateAlerts";
      
      String[] args = new String[] {
            "--server", "localhost",
            "--db", dbname,
            "--user", "user_it",
            "--pass", "pass_it",
            "--checkdb"
      };
      DBApp dq = new DBApp();
      
      resetDb(dbname);
      
      dq.parseArgs(args);
      dq.registerEntity(MultipleTasksQueue.class);
      dq.registerEntity(CarDefinitionEntityValidatorTesting.class);
      dq.build();
      
      output = outContent.toString();
      
      expected = 2;
      actual = StringUtils.countMatches(output, "missing table [");
      
      Assertions.assertEquals(expected, actual);
   }
}
