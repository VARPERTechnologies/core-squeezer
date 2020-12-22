package vpt.backbone.backend.app.testing.unit;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.apache.commons.cli.ParseException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.mockito.Mockito;

import vpt.backbone.backend.app.Application;
import vpt.backbone.backend.app.DBApp;
import vpt.backbone.backend.app.ExitCodes;
import vpt.backbone.backend.app.testing.extensions.ExpectSystemExit;
import vpt.backbone.backend.app.testing.extensions.SystemExitValidator;

@TestInstance(Lifecycle.PER_CLASS)
public class DBAppTest extends SystemExitValidator
{
   private final static ByteArrayOutputStream outContent = new ByteArrayOutputStream();
   private final PrintStream originalOut = System.out;
   private final InputStream originalIn = System.in;

   private Path tmpConfigDir;
   private Path configFile;
   private SessionFactory mockedSessionFactory;
   private Session mockedSession;
   private Transaction mockedTransaction;
   
   @BeforeAll
   public void initBefore() throws ParseException 
   {
      Application app = new Application();
      
      tmpConfigDir = Paths.get(app.getHomeDir().toString(), "config");
      tmpConfigDir.toFile().mkdirs();

      String configName = "config.properties";
      configFile = new File(getClass().getClassLoader().getResource(configName).getFile()).toPath();
      
      try{Files.copy(configFile, Paths.get(tmpConfigDir.toString(), configName), StandardCopyOption.REPLACE_EXISTING);}catch(Exception e){}
            
      mockedSessionFactory =  Mockito.mock(SessionFactory.class);
      mockedSession = Mockito.mock(Session.class);
      mockedTransaction = Mockito.mock(Transaction.class);
      Mockito.when(mockedSessionFactory.openSession()).thenReturn(mockedSession);
      Mockito.when(mockedSession.beginTransaction()).thenReturn(mockedTransaction);
      
   }
   
   @BeforeEach
   public void init() 
   {
      preventSystemExit(true);
      
      outContent.reset();
      System.setOut(new PrintStream(outContent));
   }

   @AfterEach
   public void end()
   {
      preventSystemExit(false);
      
      System.setOut(originalOut);
   }
   
   @DisplayName("Print help info whould print all options in alphabetical order")
   @Test
   public void printHelpInfo_Always_PrintCorrectText() throws ParseException
   {
      //Arrange
      DBApp app;
      String expected;
      String obtained;
      
      //Act
      app = new DBApp();
      app.printHelpInfo();
      expected = "usage: classes\r\n" + 
            "    --checkdb           Will perform tasks to validate if database model\r\n" + 
            "                        is coherent with application entities\r\n" + 
            "    --db <database>     The name of the database to be conected\r\n" + 
            "    --help              Prints help information about this process\r\n" + 
            "    --log <log>         A log name for a customized log file\r\n" + 
            "    --pass <pass>       The password to be authenticated\r\n" + 
            "    --port <port>       Server port\r\n" + 
            "    --server <server>   Server where database is hosted\r\n" + 
            "    --user <user>       The user to be connected\r\n" + 
            "    --version           Version of this binary\r\n";
      obtained = outContent.toString();

      //Assert
      Assertions.assertEquals(expected, obtained);
   }
   
   @DisplayName("Missing --port argument should assume 3306 for mysql")
   @Test
   public void parseArgs_MissingPortArgument_Assume3306ForMysql() throws ParseException
   {
      //Arrange
      DBApp app;
      String expected;
      String obtained;
      String[] args;
      
      //Act
      args = new String[] {"--server", "localhost", "--db", "prueba_unit", "--user", "unit_user", "--pass", "junit_pass"};
      app = new DBApp();
      app.parseArgs(args);
      app.setSessionFactory(mockedSessionFactory);
      app.build();
      
      expected = "Argument --port was not specified, assuming 3306\r\n";
      obtained = outContent.toString();

      //Assert
      Assertions.assertEquals(expected, obtained);
   }

   @DisplayName("Missing --pass argument should prompt password")
   @Test
   public void parseArgs_MissingPassArgument_ShouldPromptPassword() throws ParseException
   {
      //Arrange
      DBApp app;
      String expected;
      String obtained;
      String[] args;
      String strPassword = "password_it\n";
      ByteArrayInputStream inContent = new ByteArrayInputStream(strPassword.getBytes());
      System.setIn(inContent);
      
      //Act
      args = new String[] {"--server", "localhost", "--db", "prueba_unit", "--user", "unit_user", "--port", "3306"};
      app = new DBApp();
      app.parseArgs(args);
      app.setSessionFactory(mockedSessionFactory);
      app.build();

      System.setIn(originalIn);
      
      expected = "Password:\r\n";
      obtained = outContent.toString();

      //Assert
      Assertions.assertEquals(expected, obtained);
   }

   @DisplayName("Missing --server argument prints invalid argument line message")
   @Test
   @ExpectSystemExit(ExitCodes.INVALID_ARGS)
   public void test3() throws ParseException, InterruptedException
   {
      // Arrange
      DBApp app;
      String[] args;
//      Runtime rt = Runtime.getRuntime();
//      Thread t = new Thread() {
//         @Override
//         public void run()
//         {
//            //FIXME: Improve this to be handled in one thread. Actually if no assert it works, 
//            //but when assertion triggered JUnit crash
//            String expectedOutputBeforeExit = "usage: classes\r\n" + 
//                  "    --db <database>     The name of the database to be conected\r\n" + 
//                  "    --help              Prints help information about this process\r\n" + 
//                  "    --log <log>         A log name for a customized log file\r\n" + 
//                  "    --pass <pass>       The password to be authenticated\r\n" + 
//                  "    --port <port>       Server port\r\n" + 
//                  "    --server <server>   Server where database is hosted\r\n" + 
//                  "    --user <user>       The user to be connected\r\n" + 
//                  "    --version           Version of this binary\r\n";
//            String obtainedOutputBeforeExit = outContent.toString();
//            Assertions.assertEquals(expectedOutputBeforeExit, obtainedOutputBeforeExit);
//         }
//      };
//      rt.addShutdownHook(t);

      // Act
      args = new String[] { 
            "--db", 
            "prueba_unit", 
            "--user", 
            "unit_user", 
            "--pass", 
            "unit_pass" 
            };
      app = new DBApp();
      app.parseArgs(args);
      app.setSessionFactory(mockedSessionFactory);
      app.build();
      
      // Assert
   }
   
   @DisplayName("Missing --db argument prints invalid argument line message")
   @Test
   @ExpectSystemExit(ExitCodes.INVALID_ARGS)
   public void test4() throws ParseException, InterruptedException
   {
      // Arrange
      DBApp app;
      String[] args;
            
      // Act
      args = new String[] {
            "--server",
            "prueba", 
            "--user", 
            "unit_user", 
            "--pass", 
            "unit_pass" 
            };
      app = new DBApp();
      app.parseArgs(args);
      app.setSessionFactory(mockedSessionFactory);
      app.build();
      
      // Assert
      
   }
   
   @DisplayName("Missing --user argument exits with invalid argument code")
   @Test
   @ExpectSystemExit(ExitCodes.INVALID_ARGS)
   public void test5() throws ParseException, InterruptedException
   {
      // Arrange
      DBApp app;
      String[] args;

      // Act
      args = new String[] {
            "--server",
            "prueba", 
            "--db",
            "prueba_unit",
            "--pass", 
            "unit_pass" 
            };
      app = new DBApp();
      app.parseArgs(args);
      app.setSessionFactory(mockedSessionFactory);
      app.build();

      // Assert
   }
   
   
   @DisplayName("Missing all db connection arguments should process args correctly")
   @Test
   public void test6() throws ParseException, InterruptedException
   {
      // Arrange
      DBApp app;
      SessionFactory sf;      
      // Act
      app = new DBApp();
      app.setSessionFactory(mockedSessionFactory);
      app.build();
      sf = app.getSessionFactory();
      
      // Assert
      Assertions.assertNotNull(sf);
   }
   
}
