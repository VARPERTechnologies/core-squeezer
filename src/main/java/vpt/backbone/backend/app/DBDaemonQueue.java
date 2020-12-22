package vpt.backbone.backend.app;

import java.io.IOException;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Observable;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.PersistenceException;

import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.exception.GenericJDBCException;
import org.hibernate.jdbc.ReturningWork;
import org.hibernate.jdbc.Work;
import org.hibernate.tool.schema.spi.SchemaManagementException;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mysql.cj.jdbc.exceptions.MySQLTransactionRollbackException;
import com.mysql.cj.util.StringUtils;

import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.hibernate.AnnotationException;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import vpt.backbone.backend.app.TaskName;
import vpt.backbone.backend.app.entities.MultipleTasksQueue;
import vpt.backbone.backend.app.entities.MultipleTasksQueueManager;

/**
 * 
 * @author edixon
 * 
 */

public class DBDaemonQueue extends DBApp
{
   private SecurityManager sm;
   private List<Class<? extends QueueTask>> qtasks = new ArrayList<Class<? extends QueueTask>>();
   private final String pckg = "vpt.backbone.backend.app.tasks.";
   
   private int maxTotalThreads = Integer.MAX_VALUE;
   
   List<TaskWorkerManager> twmList;
   
   static BigInteger tid = new BigInteger("0");
   
//   Observable eventNotifier = new Observable();
   
   SessionFactory tsf; 
   HashSet<Class<?>> tasksEntities;
   
   public DBDaemonQueue()
   {
      super();
      init();
   }
   
   public DBDaemonQueue(String logname) throws ParseException
   {
      super(logname);
      init();
   }
   
   public DBDaemonQueue(Options options) throws ParseException
   {
      super(options);
      init();
   }
   
   public DBDaemonQueue(String logname, Options options) throws ParseException {
      super(options, logname);
      init();
   }
   
//   private void declareMainFeatures() {
//      // TODO implement here
//      throw new NotImplementedException("");
//   }

//   @Override
//   public void build()
//   {
//      super.build();
//   }
   
   private void init()
   {
      registerEntity(MultipleTasksQueue.class);
   }
   
   @Override
   public void build()
   {
      super.build();

      Logger l = getLogger(); 
      List<Class<? extends QueueTask>> tempTasks; 

      tasksEntities = new HashSet<>();
      
      tasksEntities.addAll(getEntities());
      
      try
      {
         tempTasks = TaskLoader.getPackageClasses("vpt.backbone.backend.app.tasks");
         
         for (Class<? extends QueueTask> t : tempTasks)
         {
            PayloadName payload = t.getAnnotation(PayloadName.class);
            
            if(payload != null)
            {
               if(hasTable(payload.value()))
               {
                  Entities entitiesAnnotation = t.getAnnotation(Entities.class);
                  
                  if(entitiesAnnotation != null)
                  {
                     Class<?>[] entArray = entitiesAnnotation.entityList();
                     tasksEntities.addAll(Arrays.asList(entArray));   
                  }
                  qtasks.add(t);
               }
               else
               {
                  //TODO: add some warning here
               }
            }
         }
         
      }
      catch (IOException e)
      {
         l.severe("0103");
      }
      
   }

   private boolean hasTable(String table)
   {
      return buildSession().doReturningWork(new ReturningWork<Boolean>()
            {
               @Override
               public Boolean execute(Connection connection) throws SQLException
               {
                  DatabaseMetaData dm = connection.getMetaData();
                  ResultSet tableResultSet = dm.getTables(null, null, table, null);
                  return tableResultSet.next();
               }
            });
   }
   
   public void startDequeuing()
   {
      List<Class<? extends QueueTask>> currentTasks = getTaskList();
      
      if (currentTasks.size() == 0) 
      {
         getLogger().log(Level.INFO, "No tasks to work with...");
         return;
      }
      
      Timer monitorTimer = new Timer();
      twmList = new ArrayList<TaskWorkerManager>();

      ThreadFactory taskTf = new ThreadFactoryBuilder().setNameFormat("TaskWorkerRunner-%d").build();
      ThreadPoolExecutor taskExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(currentTasks.size(), taskTf);

      try
      {
         tsf = buildNewSessionFactory(tasksEntities);
      }
      catch(AnnotationException e)
      {
         String st = ExceptionUtils.getStackTrace(e);
         getLogger().log(Level.SEVERE, "0308", new Object[] { e.getMessage(), st });
         return;
      }
      catch(Exception e)
      {
         String st = ExceptionUtils.getStackTrace(e);
         getLogger().log(Level.SEVERE, "0308", new Object[] { e.getMessage(), st });
         return;
      }
      
      currentTasks.forEach(task -> {
         TaskWorkerManager twr;
         twr = new TaskWorkerManager(task);
         twmList.add(twr);
         taskExecutor.submit(twr);
      });

      monitorTimer.scheduleAtFixedRate(new TimerTask() {
         @Override
         public void run()
         {
            try
            {
               HashMap<Class<?>, Float> td = getTaskDistribution();
               float factor = 1.0F;
               
               for (TaskWorkerManager t : twmList)
               {
                  Class<?> taskClass = t.getTaskClass();
                  float d = td.get(taskClass);
                  int ap = getAvailableProcessors(factor);
                  int cp = Math.round(d * (float)ap);
                  int maxAllowedThreads = getMaxTotalThreads();
                  
                  t.setMaxTaskWorkers(cp == 0 ? 1 : cp <= maxAllowedThreads ? cp : maxAllowedThreads);
                  
                  t.setSleepMode(d == 0 ? true : false);
               }
               
            }
            catch(Exception e)
            {
               e.printStackTrace();
            }
         }
         
      }, 0, 1000);
   }
   
   private int getAvailableProcessors(float factor)
   {
      int ap = (int) ((float) Runtime.getRuntime().availableProcessors() * factor);
      return ap;
   }

   private HashMap<Class<?>, Float> getTaskDistribution()
   {
      /*
       * TODO: This should be improved obtaining this values directly with native SQL query.
       * When this were done, It was though doing it with hibernate HQL, then some
       * limitations were found, this lead us to make some native queries.  
       */
      try (Session s = buildSession())
      {
         MultipleTasksQueueManager mngr = new MultipleTasksQueueManager(s);
         Long t = mngr.getTaskDistributionTotal();
         HashMap<Class<?>, Long> td = toClassType(mngr.getTasksDistribution());
         HashMap<Class<?>, Float> tdf = new HashMap<>();
         
         List<Class<? extends QueueTask>> taskList = getTaskList();
         for (Class<?> c : taskList)
         {
            Long p = td.get(c);
            
            tdf.put(c, (p == null || p == 0 ? 0.0f : p / t.floatValue()));
         }

         return tdf;
      }
      catch(Exception e)
      {
         System.out.println("Deadlock captured desde task handler cycle");
      }
      return null;
   }

   public void startDequeuing(BigInteger limit)
   {
      //TODO:
      throw new NotImplementedException("");
   }
   
   public void stopDequeuing() 
   {
      //TODO:
      throw new NotImplementedException("");
   }
   
   private HashMap<Class<?>, Long> toClassType(HashMap<String, Long> i)
   {
      HashMap<Class<?>, Long> res = new HashMap<Class<?>, Long>();
      
      for (Entry<String, Long> cname : i.entrySet())
      {
         try
         {
            Class<?> cls = this.getClass().getClassLoader().loadClass(pckg + cname.getKey());
            
            res.put(cls, cname.getValue());
         } catch (ClassNotFoundException e) {}
      }
      
      return res;
   }
   
   private synchronized void incrementTaskId()
   {
      tid = tid.add(BigInteger.valueOf(1));
   }
   
//   private int waitForAvailableProcessors(int taskSize)
//   {
//      Runtime r = Runtime.getRuntime();
//      
//      int availableProcessors = 0;
//      do{
//         // to avoid taking all cores, by default, it always will take 80% of all
//         return (int)Math.round(r.availableProcessors() * 0.8 / taskSize);
//      } while(availableProcessors == 0);
//   }
   
//   public void setThreadsPerTask(int t)
//   {
//      this.threadsPerTask = t;
//   }
//   
//   public int getThreadsPerTask()
//   {
//      return this.threadsPerTask;
//   }
//
   public synchronized int getMaxTotalThreads()
   {
      return maxTotalThreads;
   }

   public synchronized void setMaxTotalThreads(int max)
   {
      if(max < 0) throw new IllegalArgumentException("Argument can not be less than 0");
      
      this.maxTotalThreads = (max == 0 ? Integer.MAX_VALUE : max);
   }
   
   private class TaskWorkerManager implements Runnable 
   {
      Class<?> c;
      boolean sleepMode = true;
      ThreadPoolExecutor tpe;
      
      public TaskWorkerManager(Class<? extends QueueTask> cons)
      {
         c = cons;
         
         LinkedBlockingQueue<Runnable> bq =  new LinkedBlockingQueue<Runnable>(1);
         
         ThreadFactory tf = new ThreadFactoryBuilder().setNameFormat(cons.getSimpleName() + "->[%d]").build();
         
         tpe = new ThreadPoolExecutor(1, 1, 1, TimeUnit.SECONDS, bq, tf);
      }
      
      public synchronized void setSleepMode(boolean sm)
      {
         sleepMode = sm;
      }
      
      public synchronized boolean isSleepMode()
      {
         return sleepMode;
      }

      public void setMaxTaskWorkers(int cp)
      {
         tpe.setCorePoolSize(cp);
         tpe.setMaximumPoolSize(cp);
      }

      public Class<?> getTaskClass()
      {
         return c;
      }

      private void createThreads()
      {
         tpe.setRejectedExecutionHandler(new RejectedExecutionHandler()
         {
            
            @Override
            public void rejectedExecution(Runnable r, ThreadPoolExecutor executor)
            {
               try
               {
                  executor.getQueue().put(new TaskThread(c));
               } catch (InterruptedException e)
               {
                  // TODO Auto-generated catch block
                  e.printStackTrace();
               }
            }
         });
         
         while(true)
         {
            tpe.submit(new TaskThread(c));
            
            if(isSleepMode())
            {
               try { Thread.sleep(1000); } catch (InterruptedException e) {}
            }
         }
      }
      
      @Override
      public void run()
      {
         createThreads();
      }
   }
   
   class TaskThread implements Runnable
   {
      Class<?> c;

      public TaskThread(Class<?> cons)
      {
         this.c = cons;
      }

      @Override
      public void run()
      {
         incrementTaskId();
         
         String taskName;
         String payloadName;
         PayloadName payloadAnnotation;
         QueueTask q;

         try
         {
            q = (QueueTask) c.newInstance();

            TaskName nameAnnotation = c.getAnnotation(TaskName.class);
            payloadAnnotation = c.getAnnotation(PayloadName.class);

            if(payloadAnnotation == null) return;
            
            taskName = nameAnnotation == null ? c.getName() : nameAnnotation.value();
            payloadName = payloadAnnotation.value();
            
         } catch (InstantiationException | IllegalAccessException | IllegalArgumentException e)
         {
            getLogger().log(Level.WARNING, "0106");
            
            return;
         }
         
         try (Session s = buildSession())
         {
            String stackTrace;
            final String unhandledExceptionStatus = "unhandled-exception";
            final String unhandledExceptionMessage = "record processing result in an unhandled exception";
            String additionalMessage = "";
            
            TaskQueueManager mngr = new MultipleTasksQueueManager(s);
            MultipleTasksQueue mtq = mngr.dequeue(taskName, tid, payloadName);

            if(mtq == null) { return; }
            
            try
            {
               TaskResult tres = null;
               
               try(Session processPayloadSession = tsf.openSession())
               {
                  tres = q.processPayload(mtq, processPayloadSession);
               }

               additionalMessage = StringUtils.isNullOrEmpty(tres.Message) ? "" : tres.Message;               
               
               if (tres == TaskResult.OK || tres == null)
               {
                  mtq.setStatus("finished");
                  mtq.setResultDetail("processed with no issues");
               } else if (tres == TaskResult.ERROR)
               {
                  mtq.setStatus("error");
                  mtq.setResultDetail("process finished with error state. additional information: " + additionalMessage);
               } else
               {
                  mtq.setStatus("mishandled");
                  mtq.setResultDetail("this record was processed incorrectly, will be ignored. additional information: " + additionalMessage);
               }
            }
            catch (Exception e)
            {
               stackTrace = ExceptionUtils.getStackTrace(e);
               mtq.setStatus(unhandledExceptionStatus);
               mtq.setResultDetail(unhandledExceptionMessage + (StringUtils.isNullOrEmpty(additionalMessage) ? "" : ". additional information: " + additionalMessage) + ". Stack trace: " + stackTrace);
            }
            catch (Throwable e)
            {
               stackTrace = ExceptionUtils.getStackTrace(e);
               mtq.setStatus(unhandledExceptionStatus);
               mtq.setResultDetail(unhandledExceptionMessage + (StringUtils.isNullOrEmpty(additionalMessage) ? "" : ". additional information: " + additionalMessage) + ". Stack trace: " + stackTrace);
            }
            
            closeTask(mtq, s);

         }
         catch (PersistenceException e)
         {
            Throwable firstCause = e.getCause();
            if(firstCause != null)
            {
               //TODO: it is necessary to find a way to replicate deadlocks
               if(firstCause.getCause() instanceof MySQLTransactionRollbackException)
               {
                  getLogger().log(Level.SEVERE, "0307", e);
               }   
            }
         }
         catch (Exception e)
         {
            getLogger().log(Level.SEVERE, "0105", e);
         } catch(Throwable e) {
            getLogger().log(Level.SEVERE, "0105", e);
         }
      }
   }
   
   private void closeTask(MultipleTasksQueue mtq, Session s)
   {
      mtq.incrementAttempts();
      
      Transaction tx = s.beginTransaction();
      s.saveOrUpdate(mtq);
      tx.commit();
   }
   
   public List<Class< ? extends QueueTask>> getTaskList(){
      return qtasks;
   }
}