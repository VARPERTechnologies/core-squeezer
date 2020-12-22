package vpt.backbone.backend.app;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Properties;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.cli.AlreadySelectedException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.MissingArgumentException;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.collections4.list.SetUniqueList;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.text.StringEscapeUtils;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.cfg.Configuration;
import org.hibernate.exception.GenericJDBCException;
import org.hibernate.tool.hbm2ddl.SchemaValidator;
import org.hibernate.tool.schema.spi.SchemaManagementException;

import vpt.backbone.backend.app.exceptions.MissingMandatoryPropertyException;

import org.apache.commons.validator.routines.IntegerValidator;

/*
 * NOTE: This class supports only MySQL so far
 * in the future should support other DBs
 * 
 */

public class DBApp extends Application
{
   private String currentUser;
   private String currentPassword;
   private String currentServer;
   private String currentDB;
   private Integer currentPort;
   
   private SessionFactory sessionFactory;

   private HashSet<Class<?>> entities;
   
   //0 means no retry and less than 0 means infinite retries
   private int retryAttempts;

   public DBApp()
   {
      super();
      try{init();}catch(ParseException e){}
   }

   public DBApp(String logName)
   {
      super(logName);
      try{init();}catch(ParseException e){}
   }

   public DBApp(Options options) throws ParseException
   {
      super(options);
      init();
   }

   public DBApp(Options options, String logName) throws ParseException
   {
      super(options, logName);
      init();
   }

   private void registerMessages()
   {
      // getLogger().severe("Code 0301. Error when trying to open database session. "
      // + e.getMessage());
      // getLogger().severe("Code 0300. Unexpected initial SessionFactory creation
      // failed. " + ex);
      //
      // registerMessageCode("0301", "Error when trying to open database session",
      // Level.SEVERE);
      // registerMessageCode("0300", "Unexpected initial SessionFactory creation
      // failed", Level.SEVERE);
      throw new NotImplementedException("");
   }

   private void init() throws ParseException
   {
      // registerMessages();
      // setRetryOnConnectionLost(false);

      entities = new HashSet<Class<?>>();
      
      // initInstallDBArg();
      initCheckDBArg();
      initDbConnectionArgs();

      retryAttempts = 0;

   }

   @Override
   public void build()
   {
      super.build();
      
      CommandLine cmd = getCmd();
      Options o = getOptions();
      
      Properties props = getConfigFile();
      
      try
      {
         if(props == null && cmd == null) 
            throw new MissingArgumentException(   
               "None of this mandatory properties were found: " +
               o.getOption("db").getArgName() + ", " +
               o.getOption("server").getArgName() + ", " +
               o.getOption("user").getArgName() +
               ". Make sure that config.properties file is in the correct path or you have entered the correct arguments in the command line."
               );
      }
      catch(MissingArgumentException e)
      {
         String msg = e.getMessage() + "\r\n";
         System.out.println(msg);
         printHelpInfo();
         System.exit(ExitCodes.INVALID_ARGS);
      }
      
      if (cmd == null)
      {
         buildSessionFactory();
         return;
      }

      try
      {
         if (cmd.hasOption("db") && cmd.hasOption("server") && cmd.hasOption("user"))
         {
            if (cmd.hasOption("pass"))
            {
               currentPassword = cmd.getOptionValue("pass");
            } else
            {
               try (Scanner s = new Scanner(System.in))
               {
                  System.out.println("Password:");
                  currentPassword = s.nextLine();
               }
            }

            if(cmd.hasOption("port"))
            {
               String strPort = cmd.getOptionValue("port");
               try
               {
                  currentPort = Integer.parseInt(strPort);   
               }catch(NumberFormatException e)
               {
                  System.out.println("Argument --port has an invalid integer value: " + strPort);
                  System.exit(ExitCodes.INVALID_ARGS);
               }
            }
            
            currentServer = cmd.getOptionValue("server");
            currentDB = cmd.getOptionValue("db");
            currentUser = cmd.getOptionValue("user");
            
            if(cmd.hasOption("checkdb")) {
               validateSchema(
                     currentServer, 
                     currentPort, 
                     currentDB, 
                     currentUser,
                     currentPassword);
            }
            else
            {
               buildSessionFactory(
                     currentServer, 
                     currentPort, 
                     currentDB, 
                     currentUser,
                     currentPassword); 
            }
         } else if (!cmd.hasOption("db"))
         {
            throw new MissingArgumentException(o.getOption("db"));
         } else if (!cmd.hasOption("server"))
         {
            throw new MissingArgumentException(o.getOption("server"));
         } else if (!cmd.hasOption("user"))
         {
            throw new MissingArgumentException(o.getOption("user"));
         } else
         {
            if(cmd.hasOption("checkdb")) {
               validateSchema();
            }
            else
            {
               buildSessionFactory();  
            }
         }
      }
      catch(MissingArgumentException e)
      {
         String msg = e.getMessage() + "\r\n";
         System.out.println(msg);
         printHelpInfo();
         System.exit(ExitCodes.INVALID_ARGS);
      }
   }

   private void initDbConnectionArgs() throws AlreadySelectedException
   {
      Options opt = getOptions();

      opt.addOption(Option.builder()
            .longOpt("server")
            .argName("server")
            .hasArg()
            .desc("Server where database is hosted")
            .optionalArg(false)
            .build());

      opt.addOption(Option.builder()
            .longOpt("db")
            .argName("database")
            .hasArg()
            .desc("The name of the database to be conected")
            .optionalArg(false)
            .build());

      opt.addOption(Option.builder()
            .longOpt("user")
            .argName("user")
            .hasArg()
            .desc("The user to be connected")
            .optionalArg(false)
            .build());

      opt.addOption(Option.builder()
            .longOpt("pass")
            .argName("pass")
            .hasArg()
            .desc("The password to be authenticated")
            .optionalArg(false)
            .build());

      opt.addOption(Option.builder()
            .longOpt("port")
            .argName("port")
            .hasArg()
            .desc("Server port")
            .optionalArg(false)
            .build());
   }

   protected void setDBValidator()
   {
      // TODO implement here
      throw new NotImplementedException("");
   }

   private void initInstallDBArg()
   {
      // TODO implement here
      throw new NotImplementedException("");
   }

   private void initCheckDBArg()
   {
      Options opt = getOptions();

      opt.addOption(Option.builder()
            .longOpt("checkdb")
            .desc("Will perform tasks to validate if database model is coherent with application entities")
            .optionalArg(true)
            .build());
   }

   public void setSessionFactory(SessionFactory sf)
   {
      this.sessionFactory = sf;
   }

   public SessionFactory getSessionFactory()
   {
      return sessionFactory;
   }

   private SessionFactory buildSessionFactory()
   {
      Properties props = getConfigFile();
      String port = props.getProperty("port", "");
      Integer iPort = IntegerValidator.getInstance().validate(port);
      
      Configuration cfg = buildCredentials(props.getProperty("host"), iPort, props.getProperty("database"), props.getProperty("user"), props.getProperty("password"));
            
      sessionFactory = buildSessionFactory(cfg);

      return sessionFactory;
   }
   
   public SessionFactory buildNewSessionFactory(HashSet<Class<?>> entities)
   {
      Properties props = getConfigFile();
      
      CommandLine cmd = getCmd();
      
      Configuration cfg;
      if(cmd == null)
      {
         String port = props.getProperty("port", "");
         Integer iPort = IntegerValidator.getInstance().validate(port);
         cfg = buildCredentials(props.getProperty("host"), iPort, props.getProperty("database"), props.getProperty("user"), props.getProperty("password"));  
      }
      else
      {
         cfg = buildCredentials(currentServer, currentPort, currentDB, currentUser, currentPassword);
      }
            
      sessionFactory = buildNewSessionFactory(cfg, entities);

      return sessionFactory;
   }
   
   private SessionFactory buildSessionFactory(Configuration cfg)
   {
      Logger l = getLogger();
      if (sessionFactory != null)
      {
         l.warning("Opened session factory found, in order to build a new one, no session factory should be assigned to this application instance. In this case will return the same session factory assigned");
         return this.sessionFactory;
      }
      
      String strProps = getConfigPrintString(cfg);

      l.info("Trying to build a Session factory with following settings:\r\n" + strProps);
//      StandardServiceRegistry sr = cfg.getStandardServiceRegistryBuilder().applySettings(cfg.getProperties()).build();
//
//      for (Class<?> ent : entities)
//      {
//         cfg.addAnnotatedClass(ent);
//      }
//      setDefaultConfigProperties(cfg);
//      
//      sessionFactory = cfg.buildSessionFactory(sr);
//      
//      l.info("Session factory created sucessfully...");
//      return sessionFactory;
      sessionFactory = buildNewSessionFactory(cfg, this.entities);
      return sessionFactory;
   }
   
   private SessionFactory buildNewSessionFactory(Configuration cfg, HashSet<Class<?>> entities)
   {
//      Logger l = getLogger();
      
//      String strProps = getConfigPrintString(cfg);

//      l.info("Trying to build a new Session factory with following settings:\r\n" + strProps);
      StandardServiceRegistry sr = cfg.getStandardServiceRegistryBuilder().applySettings(cfg.getProperties()).build();

      for (Class<?> ent : entities)
      {
         cfg.addAnnotatedClass(ent);
      }
      setDefaultConfigProperties(cfg);
      
      return cfg.buildSessionFactory(sr);
   }
   
   private void setDefaultConfigProperties(Configuration cfg)
   {
      /* Minimum connection opened */
      cfg.setProperty("hibernate.c3p0.min_size", "1")
      /* Maximum connection opened */
      .setProperty("hibernate.c3p0.max_size", "100")
      /* You specify the timeout period after which an idle connection is removed from the pool */
      .setProperty("hibernate.c3p0.timeout", "60")
      /* Maximum Number of statements that will be cached. Caching of prepared statements 
       is essential for best performance with Hibernate. 0 for no cache */
      .setProperty("hibernate.c3p0.max_statements", "100")
      /* Determines how many connections at a time c3p0 will try to acquire when the pool is exhausted */
      .setProperty("hibernate.c3p0.acquire_increment", "1")
      /* Defines how many times c3p0 will try to acquire a new Connection from 
      the database before giving up. If this value is less than or equal to zero, 
      c3p0 will keep trying to fetch a Connection indefinitely */
      .setProperty("hibernate.c3p0.acquireRetryAttempts", "-1")
      /* Milliseconds, time c3p0 will wait between acquire attempts. */
      .setProperty("hibernate.c3p0.acquireRetryDelay", "1000")
      /*TODO: document this property*/
//      .setProperty("hibernate.c3p0.unreturnedConnectionTimeout", "10")
      /* This is the idle time in seconds before a connection is automatically 
      validated*/
      .setProperty("hibernate.c3p0.idle_test_period", "50")
      .setProperty("hibernate.c3p0.validate", "true")
      .setProperty("hibernate.connection.autoReconnect", "true")
      .setProperty("hibernate.c3p0.preferredTestQuery", "select 1;")
      .setProperty("hibernate.show_sql", "false");
      
   }

   //TODO: validate schema not working because it is not loading credentials from anywhere
   private void validateSchema()
   {
      //TODO: this is new, so should be tested 
      Properties props = getConfigFile();
      String port = props.getProperty("port", "");
      Integer iPort = IntegerValidator.getInstance().validate(port);
      
      Configuration cfg = buildCredentials(props.getProperty("host"), iPort, props.getProperty("database"), props.getProperty("user"), props.getProperty("password"));
            
      validateSchemaHelper(cfg);
   }
   
   private void validateSchema(String server, Integer port, String database, String user, String password)
   {
      Configuration cfg = buildCredentials(server, port, database, user, password);

      validateSchemaHelper(cfg);      
   }
   
   private void validateSchemaHelper(Configuration cfg)
   {
      Logger l = getLogger();
      
      StandardServiceRegistry sr = cfg.getStandardServiceRegistryBuilder().applySettings(cfg.getProperties()).build();
      
      ArrayList<SchemaManagementException> le = new ArrayList<SchemaManagementException>();
      
      SchemaValidator sv = new SchemaValidator();
      
      
      for (Class<?> e : entities)
      {
         Metadata metadata = new MetadataSources(sr).addAnnotatedClass(e).buildMetadata();
         
         try
         {
            sv.validate(metadata, sr);
         }
         catch(SchemaManagementException ex)
         {
            le.add(ex);
         }
      }

      String strReport;
      if(le.size() > 0) 
      {
         strReport = "Performing schema validations due --checkdb arg found. Following alerts were fond:\r\n" + getValidationReport(le);
      }else {
         strReport = "Performing schema validations due --checkdb arg found. Schema is OK";
      }
      
      l.info(strReport);
      System.out.println(strReport);
   }
   
   private String getValidationReport(ArrayList<SchemaManagementException> le)
   {
      StringBuilder sb = new StringBuilder();
      for (SchemaManagementException ex : le)
      {
         String alert = ex.getMessage().replace("Schema-validation: ", "");
         sb.append("\t" + alert + "\r\n");
      }
      return sb.toString();
   }
   
   private String getConfigPrintString(Configuration cfg)
   {
      StringBuilder strBuild = new StringBuilder();
      cfg.getProperties().forEach((k, v) -> {
         String line = "";
         String key = k.toString();
         String value = v.toString();
         if(key.compareTo("hibernate.connection.password") == 0)
         {
            line = "\t" + key + "=**************\r\n";
         }
         else if (key.compareTo("line.separator") == 0) {
            line = "\t" + key + "=" + StringEscapeUtils.escapeJava(value) + "\r\n";
         }
         else
         {
            line = "\t" + k + "=" + v + "\r\n";
         }
         strBuild.append(line);
      });
      
      return strBuild.toString();
   }

   private SessionFactory buildSessionFactory(String server, Integer port, String database, String user, String password)
   {
      // TODO: print connection properties for debug only
      // TODO: add support for different drivers (database servers)
      
      if(port == null) 
      {
         System.out.println("Argument --port was not specified, assuming 3306");
      }
      
      Configuration cfg = buildCredentials(server, port, database, user, password);
            
      sessionFactory = buildSessionFactory(cfg);
      return sessionFactory;
   }

   private Configuration buildCredentials(String server, Integer port, String database, String user, String password)
   {
      return new Configuration()
            .setProperty("hibernate.connection.driver_class", "com.mysql.cj.jdbc.Driver")
            .setProperty("hibernate.dialect", "org.hibernate.dialect.MySQLDialect")
            .setProperty("hibernate.connection.url", "jdbc:mysql://" + server + ":" + (port != null ? port : 3306) + "/" + database + "?autoReconnect=true")
            .setProperty("hibernate.connection.username", user)
            .setProperty("hibernate.connection.password", password)
//            .setProperty("hibernate.connection.provider_class", "org.hibernate.connection.C3P0ConnectionProvider")
//            .setProperty("hibernate.c3p0.acquireRetryAttempts", "0")
            .configure();
   }
   
   @Override
   public Properties loadConfigFile(String path) throws MissingMandatoryPropertyException
   {
      Properties p = super.loadConfigFile(path);
      
      if(p == null) return null;
      
      Logger l = getLogger();
      
      if(!(p.containsKey("host") && p.containsKey("database") && p.containsKey("user") && p.containsKey("password")))
      {
         l.severe("0104");
         throw new MissingMandatoryPropertyException();
      }
      return p;
   }
   
   public Session buildSession()
   {
      try
      {
         Session se = sessionFactory.openSession();
         return se;
      } catch (GenericJDBCException e)
      {
         getLogger().log(Level.SEVERE, "0301", e.getMessage());
         return null;
      } catch (SchemaManagementException e)
      {
         getLogger().log(Level.SEVERE, "0301", e.getMessage());
         return null;
      } catch (HibernateException e)
      {
         getLogger().log(Level.SEVERE, "0301", e.getMessage());
         return null;
      }catch(NullPointerException e)
      {
         getLogger().log(Level.SEVERE, "0306");
         return null;
      } catch (Exception ex)
      {
         getLogger().log(Level.SEVERE, "0300");
         return null;
      } catch (Throwable ex)
      {
         getLogger().log(Level.SEVERE, "0300");
         return null;
      }
   }

   public void closeSession(Session s)
   {
      if (s == null)
         return;

      if (s.isOpen())
      {
         s.clear();
         s.close();
      }
   }
   
   /**
    * @param attempts
    */
//   public void setRetryAttempts(int attempts)
//   {
//      this.retryAttempts = attempts < 0 ? -1 : attempts;
//   }

   /**
    * @return
    */
//   public int getRetryAttempts()
//   {
//      return retryAttempts;
//   }
   
   public void registerEntity(Class<?> entity)
   {
      entities.add(entity);
   }
   
   public HashSet<Class<?>> getEntities()
   {
      return entities;
   }
   
}
