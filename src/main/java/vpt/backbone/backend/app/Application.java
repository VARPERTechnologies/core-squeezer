package vpt.backbone.backend.app;

import java.nio.file.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.apache.commons.cli.*;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;

import vpt.backbone.backend.app.exceptions.MissingMandatoryPropertyException;


/**
 * @author edixon
 * This is the base class for applications you should use, implements functions to load
 * config files, initializes logger objects and parse basic arguments like help and version
 * 
 * This application will virtually handle a directory structure like this:
 *  - root                             Root directory where app should be installed. Could have any name
 *   \- config                         Where configuration files should be found (application will not create this directory)
 *   \- log                            Default location where logs will be placed
 *   
 * 
 * By default this application creates some default directories like {@code log} directory
 * where all log files are placed, logs can be customized with your own name as you need,
 * this is important when you run multiple instances of this application in the same root
 * directory.
 * 
 * 
 */

//TODO: Add support for --debug parameter
public class Application
{
   private String logName;
   private LogManager logManager;
   private Logger logger;
   private FileHandler fileHandler;
   private ConsoleHandler consoleHandler;
   private SimpleFormatter logFormater;

   private String systemPathSeparator;
   private File exePath;
   private File homeDir;
   private File binDir;

   private CommandLineParser cliParser;
   private CommandLine cmd;
   private Options options;

   private Properties props;
   private Path configFilePath;
   private boolean mandatoryConfigFile;
   private File logDir;

   
   /**
    * Basic constructor with no       
    */
   public Application()
   {
      try{init(null, null);}catch(ParseException e) {};
   }

   /**
    * 
    * @param logname
    * @throws UnexpectedExitException
    */
   public Application(String logname)
   {
      try{init(null, logname);} catch (ParseException e){}
   }

   public Application(Options options) throws ParseException
   {
      init(options, null);
   }

   public Application(Options options, String logName) throws ParseException
   {
      init(options, logName);
   }


   private void initMessages()
   {
      throw new NotImplementedException("");
   }

   /*
    * 
    */
   private void init(Options newOptions, String logName) throws ParseException
   {
      systemPathSeparator = System.getProperty("file.separator");

      exePath = new File(this.getClass().getProtectionDomain().getCodeSource().getLocation().getFile());

      binDir = exePath.getParentFile();

      homeDir = binDir.getParentFile();

      logManager = LogManager.getLogManager();

      logDir = Paths.get(homeDir.getAbsolutePath(), "log").toFile();

      logDir.mkdirs();

      mandatoryConfigFile = false;

      cliParser = new DefaultParser();

      OptionGroup infoOG = new OptionGroup();

      if (newOptions == null)
      {
         this.options = new Options().addOption(Option.builder().longOpt("log").argName("log").hasArg()
               .desc("A log name for a customized log file").optionalArg(true).build());
      } else
      {
         this.options = newOptions;
      }

      initHelpArg(infoOG);
      initVersionArg(infoOG);

      this.options.addOptionGroup(infoOG);

      setLogName(logName);
      
      try{loadConfigFile(getHomeDir() + "/config/config.properties");} catch (MissingMandatoryPropertyException e) {}
   }

   /**
    * 
    * @param args
    */
   public void parseArgs(String[] args) 
   {
      try
      {
         cmd = cliParser.parse(this.options, args);  
      }
      catch(AlreadySelectedException e)
      {
         System.out.println(e.getMessage());
         System.exit(10022);
      }
      catch(ParseException e)
      {
         System.out.println(e.getMessage());
         System.exit(10022);
      }
   }

   public void build()
   {
      if (cmd != null)
      {
         if (cmd.hasOption("help"))
         {
            printHelpInfo();
            System.exit(ExitCodes.OK);
         }
         if (cmd.hasOption("version"))
         {
            printVersionInfo();
            System.exit(ExitCodes.OK);
         }
      }
   }
   
   private void printVersionInfo()
   {
      System.out.println("version: " + getVersion());
   }

   public String getVersion()
   {
      return "1.0";
   }

   public String getApplicationName()
   {
      // File f = new
      // File(this.getClass().getProtectionDomain().getCodeSource().getLocation().getFile());
      return exePath.getName();
   }

   public void printSystemInformation()
   {
      throw new NotImplementedException("");
   }

   public void printConfigInfo()
   {
      throw new NotImplementedException("");
   }

   public void printHelpInfo()
   {
      HelpFormatter formatter = new HelpFormatter();

      // TODO: If necessary you could implement your own comparator
      // to print options in the order you need
      // formatter.setOptionComparator(new Comparator<Option>()
      // {
      // @Override
      // public int compare(Option o1, Option o2)
      // {
      // if (o1.isRequired() && !o2.isRequired())
      // {
      // return -1;
      // }
      // if (!o1.isRequired() && o2.isRequired())
      // {
      // return 1;
      // }
      // String opt1 = o1.getArgName();
      // String opt2 = o2.getArgName();
      //
      // return opt1.compareTo(opt2);
      // }
      // });

      formatter.printHelp(getApplicationName(), options);
   }
   
   
   public String getHelpInfo() 
   {
      //TODO: Should be implemented
      throw new NotImplementedException("");
   }

   private void initHelpArg(OptionGroup infoOG)
   {
      infoOG.addOption(Option.builder().longOpt("help").argName("help")
            .desc("Prints help information about this process").optionalArg(true).build());
   }

   /**
    * @param infoOG
    * 
    */
   private void initVersionArg(OptionGroup infoOG)
   {
      infoOG.addOption(Option.builder().longOpt("version").argName("version").desc("Version of this binary")
            .optionalArg(true).build());
   }
   
   public Properties loadConfigFile(String path) throws InvalidPathException, MissingMandatoryPropertyException
   {
      if (StringUtils.isEmpty(path))
         throw new InvalidPathException(path, "Path must be a non empty string");

      Level level = mandatoryConfigFile ? Level.SEVERE : Level.WARNING;
      try
      {
         this.configFilePath = Paths.get(path);
      }
      catch (Exception e)
      {
         logger.log(level, "0000", e.getMessage());
         return null;
      }

      try (FileInputStream input = new FileInputStream(configFilePath.toString()))
      {
         props = new Properties();
         props.load(input);
         return props;
      } catch (FileNotFoundException e)
      {
         logger.log(level, "0102", configFilePath.toString());
      } catch (SecurityException e)
      {
         logger.log(level, "0101", configFilePath.toString());
      } catch (Exception e)
      {
         logger.log(level, "0000", e.getMessage());
      }

      return (props = null);
   }
   
   public Path getConfigFilePath()
   {
      return configFilePath;
   }

   public Logger getLogger()
   {
      return logger;
   }

   private void loadLoggerConfig(Logger logger)
   {
      try
      {
         Path cfgFile = getConfigFilePath();
         logManager.readConfiguration(new FileInputStream(cfgFile.toString()));
      } catch (SecurityException e)
      {
         logger.warning("1000");
      } catch (FileNotFoundException e)
      {
         logger.warning("1001");
      } catch (IOException e)
      {
         logger.warning("1002");
      } finally
      {

      }
   }

   private void loadLoggerMessages(Logger logger)
   {
      final String LOG_MSG_PATH = "logmessages";
      try
      {
         ResourceBundle rb = ResourceBundle.getBundle(LOG_MSG_PATH);
         logger.setResourceBundle(rb);
         logger.info("Loading all message codes...");
         logger.info("All messages loaded...");
      } catch (MissingResourceException e)
      {
         logger.severe(
               "Error when trying to load log message catalog \"" + LOG_MSG_PATH + "\". The resource was not found.");
      } catch (NullPointerException e)
      {
         logger.severe("Error when trying to load log message catalog \"" + LOG_MSG_PATH + "\". The resource is null.");
      } catch (SecurityException e)
      {
         logger.severe("Error when trying to load log message catalog \"" + LOG_MSG_PATH
               + "\". You need permissions to load resource");
      } catch (IllegalArgumentException e)
      {
         logger.severe("Error when trying to load log message catalog \"" + LOG_MSG_PATH
               + "\". The resource has not a base name or is invalid");
      } catch (Exception e)
      {
         logger.severe("Error when trying to load log message catalog \"" + LOG_MSG_PATH
               + "\". Unexpected error, this is the stack trace: ");
         e.printStackTrace();
      }
   }

   protected void setLogger(Logger logger)
   {
      if (logger == null)
         return;

      logger.setLevel(Level.FINEST);

      loadLoggerMessages(logger);

      //FIXME: This calling has been commented until a better solution
      //be found, troubles due to cyclic dependency, logger depends on config file and viceversa
//      loadLoggerConfig(logger);

      this.logger = logger;

      try
      {
         // TODO: Include log config properties
         // TODO: Include log date pattern
         // FileHandler fileHandler = new FileHandler(getLogDir() + systemPathSeparator +
         // logger.getName() + ".log", true);
         fileHandler = new FileHandler(getLogDir() + systemPathSeparator + logger.getName() + ".log", 10485760, 1000,
               true);
         consoleHandler = new ConsoleHandler();

         logFormater = new SimpleFormatter();

         fileHandler.setFormatter(logFormater);
         this.logger.addHandler(fileHandler);
         logger.addHandler(consoleHandler);
      } catch (SecurityException e)
      {
         logger.warning("0101");
      } catch (NoSuchFileException e)
      {
         logger.warning("1001");
      } catch (IOException e)
      {
         logger.warning("0100");
      }
   }

   public File getHomeDir()
   {
      return homeDir;
   }

   /**
    * @param dir
    */
   // protected void setHomeDir(String dir) {
   // // TODO implement here
   // }
   //
   // public void setHomeDir(File homeDir)
   // {
   // this.homeDir = homeDir;
   // }

   public File getExeFile()
   {
      return exePath;
   }

   public Path getExePath()
   {
      return exePath.toPath();
   }

   /**
    * @return the logName
    */
   public String getLogName()
   {
      return logName;
   }

   /**
    * @param logName
    *           the logName to set
    */
   public void setLogName(String logName)
   {
      // SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
      // Date date = new Date(System.currentTimeMillis());
      // String fmtDate = formatter.format(date.getTime());
      // StackTraceElement[] stackTraceElements =
      // Thread.currentThread().getStackTrace();

      String possibleLogName = getApplicationName();
      // if (stackTraceElements.length > 0) {
      // possibleLogName = stackTraceElements[stackTraceElements.length -
      // 1].getClassName();
      // }
      // else
      // {
      // possibleLogName = this.getClass().getName();
      // }

      if (StringUtils.isEmpty(logName))
      {
         // setLogger(Logger.getLogger(possibleLogName + "_" + fmtDate));
         setLogger(Logger.getLogger(possibleLogName));
         this.logName = possibleLogName;
      } else
      {
         // setLogger(Logger.getLogger(logName + "_" + fmtDate));
         setLogger(Logger.getLogger(logName));
         logName = logName.replaceAll("[:\\\\/*?|\\@<>#]", "-");
         logName = logName.replaceAll("[\\s]", "_");
         this.logName = logName;
      }
   }

   /**
    * @param file
    */
   protected void setConfigFile(ConfigFile file)
   {
      // TODO implement here
   }

   /**
    * @param path
    */
   protected void setConfigFile(String path)
   {
      // TODO implement here
   }

   /**
    * 
    */
   public Properties getConfigFile()
   {
      return props;
   }

   /**
    * 
    */
   protected void beginLog()
   {
      // TODO implement here
   }

   /**
    * 
    */
   protected void closeLog()
   {
      // TODO implement here
   }

   /**
    * @return the systemSeparator
    */
   public String getSystemSeparator()
   {
      return systemPathSeparator;
   }

   /**
    * @return the cliParser
    */
   public CommandLineParser getCliParser()
   {
      return cliParser;
   }

   /**
    * @return the options
    */
   public Options getArgOptions()
   {
      return options;
   }

   /**
    * @param options
    *           the options to set
    */
   // public void setArgOptions(Options options)
   // {
   // this.options = options;
   // }

   /**
    * @return the cmd
    */
   public CommandLine getCmd()
   {
      return cmd;
   }

   /**
    * @param cmd
    *           the cmd to set
    */
   // public void setCmdArgs(CommandLine cmd)
   // {
   // this.cmd = cmd;
   // }

   public static void execute(String path, String args)
   {
      // TODO: implement
   }

   public static void execute(String path, String args[])
   {
      // TODO: implement
   }

   public static void executeAsync(String path, String args)
   {
      // TODO: implement
   }

   public static void executeAsync(String path, String args[])
   {
      // TODO: implement
   }

   public void sleep(int i)
   {
      try
      {
         Thread.sleep(1000);
      } catch (InterruptedException e)
      {
         // TODO: consider to take care about multithreaded applications
      }
   }

   public File getBinDir()
   {
      return binDir;
   }

   public void forceLoadConfigFile(boolean mandatory)
   {
      mandatoryConfigFile = mandatory;
   }

   public boolean isConfigFileMandatory()
   {
      return mandatoryConfigFile;
   }

   /**
    * @return the logDir
    */
   public File getLogDir()
   {
      return logDir;
   }

   public FileHandler getLogFile()
   {
      return fileHandler;
   }

   /**
    * This resource is only intended to be used in the Main method where an
    * unexpected error should be handled and we don't have an instance of the class
    * 
    * @param msg
    * @param e
    */
   public static void logUnexpectedError(String msg, Throwable e)
   {
      Logger log = Logger.getLogger("UnexpectedExceptions");
      FileHandler fh = null;
      try
      {
         fh = new FileHandler(
               System.getProperty("user.dir") + System.getProperty("file.separator") + "UnhandledExceptions", true);
      } catch (SecurityException e1)
      {
         System.out.println("A security error ocurred when trying to set file handler for logging");
         e.printStackTrace();
      } catch (IOException e1)
      {
         System.out.println("An IO error ocurred when trying to set file handler for loggging");
         e1.printStackTrace();
      }

      if (fh != null)
         log.addHandler(fh);

      log.log(Level.SEVERE, msg, e);

   }

   /**
    * @return the options
    */
   protected Options getOptions()
   {
      return options;
   }

   /**
    * @param options
    *           the options to set
    */
   protected void setOptions(Options options)
   {
      this.options = options;
   }

   /**
    * @param logDir
    *           the logDir to set
    */
   // public void setLogDir(Path logDir)
   // {
   // this.logDir = logDir;
   // }

   /**
    * Register a new message code with its description
    * 
    * @param code
    *           A code can be alphanumeric but can't have whitespace
    * @param message
    *           A descriptive message, consider this message won't be the final
    *           message this should be only the explanation to the message code but
    *           when it is stored in the message list catalog, it is concatenated
    *           with {@code code}
    */
   // public void registerMessageCode(String code, String message, Level severity)
   // {
   // messageList.add(new MessageCatalog(code, message, severity));
   // }
   //
   // public void logFromCatalog(String code)
   // {
   // for(MessageCatalog am : messageList)
   // {
   // if(am.getCode() == code)
   // {
   // logger.log(am.getSeverity(), am.getPrintableMessage());
   // break;
   // }
   // }
   // logger.log(Level.SEVERE, "The message code " + code + "doesn't exists when
   // trying to print from message catalog. See Application class documentation");
   // }
   //
}
