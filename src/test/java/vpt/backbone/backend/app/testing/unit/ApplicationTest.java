package vpt.backbone.backend.app.testing.unit;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.ParseException;
import java.util.logging.Logger;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

//import backend.app.extensions.ExpectSystemExitWithStatus;
import vpt.backbone.backend.app.Application;
import vpt.backbone.backend.app.ExitCodes;
import vpt.backbone.backend.app.exceptions.MissingMandatoryPropertyException;
import vpt.backbone.backend.app.testing.extensions.ExpectSystemExit;
import vpt.backbone.backend.app.testing.extensions.SystemExitValidator;
import java.util.Properties;
import java.util.logging.FileHandler;

//@RunWith(Theories.class)
@TestInstance(Lifecycle.PER_CLASS)
public class ApplicationTest extends SystemExitValidator
{
   private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
   private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
   private final PrintStream originalOut = System.out;
   private final PrintStream originalErr = System.err;

   SecurityManager oldSM = System.getSecurityManager();

   private Path tmpConfigDir;
   private Path configFile;

   @BeforeAll
   public void initializeAppEnviroment() throws IOException, ParseException
   {
      Application app = new Application();
      
      tmpConfigDir = Paths.get(app.getHomeDir().toString(), "config");
      tmpConfigDir.toFile().mkdirs();

      String configName = "config.properties";
      configFile = new File(getClass().getClassLoader().getResource(configName).getFile()).toPath();
      
      try{Files.copy(configFile, Paths.get(tmpConfigDir.toString(), configName), StandardCopyOption.REPLACE_EXISTING);}catch(Exception e){}
   }

   @AfterAll
   public void removeDirs() throws IOException
   {
      // deleteDirectoryRecursion(tmpConfigDir);
   }

   @BeforeEach
   public void setUpStreams()
   {
      preventSystemExit(true);
      outContent.reset();
      errContent.reset();
      System.setOut(new PrintStream(outContent));
      System.setErr(new PrintStream(errContent));
   }

   @AfterEach
   public void restoreStreams()
   {
      preventSystemExit(false);
      System.setOut(originalOut);
      System.setErr(originalErr);
   }

   void deleteDirectoryRecursion(Path path) throws IOException
   {
      if (Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS))
      {
         try (DirectoryStream<Path> entries = Files.newDirectoryStream(path))
         {
            for (Path entry : entries)
            {
               deleteDirectoryRecursion(entry);
            }
         }
      }
      Files.delete(path);
   }

   @DisplayName("Constructor with no arguments should create default log file within log dir")
   @Test
   public void Constructor_EmptyParams_GenerateDefaultLog() throws IOException, ParseException
   {
      // Arrange
      Application app = new Application();

      // Act
      FileHandler obtained = app.getLogFile();

      // Assert
      Assertions.assertNotNull(obtained);
   }

   @DisplayName("Constructor with no arguments set logname same as classname")
   @Test
   public void Constructor_default_SetsLogNameSameAsClassName() throws IOException, ParseException
   {
      // Arrange
      Application app = new Application();

      // Act
      String expected = app.getApplicationName();
      String obtained = app.getLogName();

      // Assert
      Assertions.assertEquals(expected, obtained);
   }

   @DisplayName("Constructor with undeclared arguments should exit with code 10022")
   @ParameterizedTest()
   @CsvSource({ 
      "-CualquierArg, Parametro", 
      "--CualquierArg, Parametro", 
      "--CualquierArg, \"\"", 
      "-CualquierArg, \"\"",
      "-CualquierArg, \"á\"" })
   @ExpectSystemExit(ExitCodes.INVALID_ARGS)
   public void Constructor_UndeclaredArgs_ExitWithInvalidArgsCode(String first, String second)
         throws IOException, ParseException
   {
      // Arrange
      Application app = new Application(null, null);
      String[] args;
      
      // Act
      args = new String[] { first, second };
      app.parseArgs(args);
      app.build();
      
      //Assert
   }

   @DisplayName("Possible lognames")
   @ParameterizedTest(name = "\"{0}\" should be {1}")
   @CsvSource({ "CualquierValor, CualquierValor", "AcentosáéíóóÁÉÍÓÚñÑ, AcentosáéíóóÁÉÍÓÚñÑ",
         "Numeros2424243, Numeros2424243", "'Espacios  ddd asdfasd d    dd', Espacios__ddd_asdfasd_d____dd",
         "'otros c@r@ctres', otros_c-r-ctres", "'otros c@r@ct#r#s',otros_c-r-ct-r-s" })
   public void setLogName_NameOK_SetPossibleName(String first, String second/* final String[] inputs */)
         throws IOException, ParseException
   {
      // Arrange
      Application app = new Application();

      // Act
      app.setLogName(first);
      String expected = second;
      String obtained = app.getLogName();

      // Assert
      Assertions.assertEquals(obtained, expected);
   }

   @DisplayName("loadConfigFile default constructor return a found config file")
   @Test
   public void loadConfigFile_ConstructorNoArgs_DefaultConfig() throws IOException, ParseException
   {
      // Arrange
      Application app = new Application();

      // Act
      Properties obtained = app.getConfigFile();

      // Assert
      Assertions.assertNotNull(obtained);
   }

   @DisplayName("loadConfigFile default constructor return null file when config file does not exist")
   @Test
   public void loadConfigFile_FileNotFound_ReturnsNull() throws ParseException, InvalidPathException, MissingMandatoryPropertyException
   {
      // Arrange
      Application app = new Application();

      // Act
      app.loadConfigFile("prueba_archivo_inexistente.properties");

      // Assert
      Assertions.assertNull(app.getConfigFile());
   }

   @DisplayName("loadConfigFile invalid paths should throws InvalidPathException")
   @ParameterizedTest()
   @ValueSource(strings = { ""
         // TODO:Please add more invalid PATHS here
   })
   public void loadConfigFile_InvalidPaths_ThrowsInvalidPathException(String invalidPaths) throws ParseException
   {
      // Arrange
      Application app = new Application();

      // Act

      // Assert
      Assertions.assertThrows(InvalidPathException.class, () ->
      {
         app.loadConfigFile(invalidPaths);
      });
   }

   @DisplayName("loadConfigFile default constructor return not null Properties file when config is found")
   @Test
   public void loadConfigFile_FileFound_ReturnsProperties() throws ParseException, IOException, InvalidPathException, MissingMandatoryPropertyException
   {
      // Arrange
      Application app = new Application();
      String tmpConfigPath = Paths.get(app.getHomeDir().getAbsolutePath(), "config", "config.properties").toString();

      // Act
      app.loadConfigFile(tmpConfigPath);

      // Assert
      Assertions.assertNotNull(app.getConfigFile());
   }

   @DisplayName("Parameter --version should exit app with code 0")
   @Test
   @ExpectSystemExit(0)
   public void getVersion_Always_ReturnsExitCodeOne() throws ParseException
   {
      // Arrange
      Application app;
      String version = "";
      String[] args;

      // Act
      args = new String[] { "--version" };
      app = new Application();
      app.parseArgs(args);
      app.build();
      version = app.getVersion();

      // Assert
      Assertions.assertNotEquals("version: " + version, outContent.toString());
   }

   @DisplayName("Parameter --help should exit application with code 0")
   @Test
   @ExpectSystemExit(ExitCodes.OK)
   public void Constructor_HelpArg_ShouldExit() throws ParseException
   {
      // Arrange
      Application app;
      String[] args;
      
      // Act
      args = new String[] { "--help" };
      app = new Application();
      app.parseArgs(args);
      app.build();

      // Assert
   }
   
   @DisplayName("printHelpInfo function should print correct text information to Standard output")
   @Test
   public void printHelpInfo_Default_ShouldPrintCorrectMessage() throws ParseException
   {
      // Arrange
      Application app;
      String expected;
      String obtained;
      
      // Act
      app = new Application();
      app.printHelpInfo();
      expected = "usage: classes\r\n" + 
            "    --help        Prints help information about this process\r\n" + 
            "    --log <log>   A log name for a customized log file\r\n" + 
            "    --version     Version of this binary\r\n";
      obtained = outContent.toString();
      
      // Assert
      Assertions.assertEquals(expected, obtained);
   }

   @DisplayName("Version should print current version 1.0")
   @Test
   public void getVersion_PrintProjectVersion() throws ParseException
   {
      // Arrange
      Application app = new Application();
      String expected;
      String obtained;

      // Act
      expected = "1.0";
      obtained = app.getVersion();

      // Assert
      Assertions.assertEquals(expected, obtained);
   }

   @DisplayName("Ensure application have the correct name")
   @Test
   public void getApplicationName_ReturnCurrenAppName() throws ParseException
   {
      // Arrange
      Application app = new Application();
      String expected;
      String obtained;

      // Act
      expected = "classes";
      obtained = app.getApplicationName();

      // Assert
      Assertions.assertEquals(expected, obtained);
   }

   @DisplayName("Ensure the same path is returned for config file loaded")
   @Test
   public void getConfigFilePath_ReturnLoadedConfigPath() throws ParseException
   {
      // Arrange
      Application app = new Application();
      String expected;
      String obtained;

      // Act
      expected = Paths.get(app.getHomeDir().toString(), "config", "config.properties").toString();
      obtained = app.getConfigFilePath().toString();

      // Assert
      Assertions.assertEquals(expected, obtained);
   }

   @DisplayName("Ensure returns a correct logger with the same name assigned")
   @Test
   public void getLogger_ReturnSameLoggerWhenStartedApp() throws ParseException
   {
      //TODO: Should include names with invalid characters
      // Arrange
      Application app = new Application();
      Logger expected;
      Logger obtained;

      // Act
      app.setLogName("Esta_es_una_prueba");
      expected = Logger.getLogger("Esta_es_una_prueba");
      obtained = app.getLogger();

      // Assert
      Assertions.assertEquals(expected, obtained);
   }

   @DisplayName("Ensure returns a correct home dir for a default application")
   @Test
   public void getHomeDir_ReturnCurrentHomeDir() throws ParseException
   {
      // Arrange
      Application app = new Application();
      File expected;
      File obtained;

      // Act
      expected = Paths.get(System.getProperty("user.dir"), "target", "home").toFile();
      obtained = app.getHomeDir();

      // Assert
      Assertions.assertEquals(expected, obtained);
   }

   @DisplayName("Must return compiled target dir plus project_name/execuatable_name path")
   @Test
   public void getExePath_ReturnCurrentExePath() throws ParseException
   {
      // Arrange
      Application app = new Application();
      Path expected;
      Path obtained;

      // Act
      expected = Paths.get(app.getExePath().getParent().toString(), app.getApplicationName());
      obtained = app.getExePath();

      // Assert
      Assertions.assertEquals(expected, obtained);
   }

   @DisplayName("Must return compiled target dir plus project_name/execuatable_name file")
   @Test
   @Disabled //Disabled because at this moment I don't know a reliable way to set the application name the same as the project output executable name
   public void getExePath_ReturnCurrentExeFile() throws ParseException, IOException
   {
      // Arrange
      Application app = new Application();
      File expected;
      File obtained;

      InputStream input = new FileInputStream("src/test/resources/project.definition.properties");
      Properties props = new Properties();
      props.load(input);

      // Act
      expected = Paths.get(app.getExePath().getParent().toString(), app.getApplicationName()).toFile();
      obtained = app.getExeFile();

      // Assert
      Assertions.assertEquals(expected.toString(), obtained.toString());
   }

   @DisplayName("Ensure return the same log name which was assigned manually")
   @Test
   public void getLogName_ReturnAssignedLogName() throws ParseException
   {
      // Arrange
      Application app = new Application();
      String expected;
      String obtained;

      // Act
      expected = "classes";
      obtained = app.getLogName();

      // Assert
      Assertions.assertEquals(expected, obtained);
   }

   @DisplayName("Ensure return the same config file which was assigned manually")
   @Test
   public void getConfigFile_ReturnSameConfigFileWhichWasAssigned() throws ParseException, IOException
   {
      // Arrange
      Application app = new Application();
      Properties expected;
      Properties obtained;
      InputStream input;

      // Act
      input = new FileInputStream(Paths.get(app.getHomeDir().toString(), "config", "config.properties").toString());

      expected = new Properties();
      expected.load(input);

      obtained = app.getConfigFile();

      // Assert
      Assertions.assertEquals(expected.keySet(), obtained.keySet());
   }

   @DisplayName("Ensure return the same config file path which was assigned manually")
   @Test
   public void getConfigFilePath_ReturnSameConfigFilePathWhichWasAssigned() throws ParseException, IOException, InvalidPathException, MissingMandatoryPropertyException
   {
      // Arrange
      Application app = new Application();
      Path expected;
      Path obtained;

      // Act
      expected = Paths.get(app.getHomeDir().toString(), "config", "config.properties");
      app.loadConfigFile(expected.toString());

      obtained = app.getConfigFilePath();

      // Assert
      Assertions.assertEquals(expected, obtained);
   }

   @DisplayName("Ensure return the same separator as java's current OS path separator")
   @Test
   public void getSystemPathSeparator_ReturnCurrentJavaSeparator() throws ParseException, IOException
   {
      //TODO: Improve this test, should validate OS cases
      // Arrange
      Application app = new Application();
      String expected;
      String obtained;

      // Act
      expected = System.getProperty("file.separator");
      obtained = app.getSystemSeparator();

      // Assert
      Assertions.assertEquals(expected, obtained);
   }

   @DisplayName("When default constructor application should return DefaultParser instance")
   @Test
   public void getCliParser_AppDefaultConstructor_ReturnDefaultParser() throws ParseException, IOException
   {
      // Arrange
      Application app = new Application();
      CommandLineParser obtained;

      // Act
      obtained = app.getCliParser();

      // Assert
      Assertions.assertNotNull(obtained);
   }
   
   @DisplayName("Args help and version simultaneously should break application")
   @Test
   @ExpectSystemExit(ExitCodes.INVALID_ARGS)
   public void processArgs_SimultaneousHelpAndVersionArgs_BreakApp() throws ParseException
   {
      //Arrange
      Application app = new Application();
      String[] args = new String[] {"--help", "--version"};
      
      //Act
      app.parseArgs(args);
      app.build();
      
      //Assert
      
   }
   
}
