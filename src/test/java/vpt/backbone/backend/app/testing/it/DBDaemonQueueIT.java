package vpt.backbone.backend.app.testing.it;

import static tools.HibernateDBTools.buildSessionFactory;
import static vpt.backbone.backend.app.utils.DBToolsVariation.resetDb;
import static vpt.backbone.backend.app.utils.DBToolsVariation.insertIntoMultipleTaskQueue;
import static vpt.backbone.backend.app.utils.DBToolsVariation.insertIntoMultipleTaskQueueWithRandomPayload;
import static vpt.backbone.backend.app.utils.DBToolsVariation.createPayloadTables;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

import org.apache.commons.lang3.NotImplementedException;
import org.hibernate.Session;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.mockito.Mockito;
import com.google.common.io.Files;

import javassist.CannotCompileException;
import javassist.ClassClassPath;
import javassist.ClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.NotFoundException;
import vpt.backbone.backend.app.DBDaemonQueue;
import vpt.backbone.backend.app.QueueTask;
import vpt.backbone.backend.app.entities.MultipleTasksQueue;
import vpt.backbone.backend.app.exceptions.MissingMandatoryPropertyException;
import vpt.backbone.backend.app.tasks.BasicQueueTask;
import vpt.backbone.backend.app.tasks.TaskExecutingLongMathOperationAndExecutesOK;
import vpt.backbone.backend.app.tasks.TaskReturningError;
import vpt.backbone.backend.app.tasks.TaskThrowingUnhandledException;
import vpt.backbone.backend.app.utils.DBToolsVariation;
import vpt.backbone.backend.app.utils.TaskIdentifierStorage;

//TODO: missing tests tasks with real database connections
//TODO: missing test one queue row and multiple payload rows

class DBDaemonQueueIT
{   
   @SuppressWarnings("unused")
   private CtClass createQueueTaskClass(String name) throws NotFoundException
   {
      ClassPool cp = ClassPool.getDefault();
      cp.insertClassPath(new ClassClassPath(BasicQueueTask.class));
      CtClass queueTaskBase = cp.get("vpt.backbone.backend.app.QueueTask");
      CtClass ctExample = cp.makeClass(name, queueTaskBase);

      return ctExample;
   }
   
   @SuppressWarnings("unused")
   private CtClass createBasicQueueTaskClassWithProcessQueueMethod(String name, String methodBody)
         throws NotFoundException, CannotCompileException, IOException, URISyntaxException
   {
      File pathDBDaemonQueueJar = new File(DBDaemonQueue.class.getProtectionDomain().getCodeSource().getLocation().toURI());
      
      ClassPool cp = ClassPool.getDefault();
      ClassPath path = cp.insertClassPath(new ClassClassPath(BasicQueueTask.class));
      CtClass queueTaskBase = cp.get("vpt.backbone.backend.app.QueueTask");
      CtClass ctRes = cp.makeClass("vpt.backbone.backend.app.tasks." + name, queueTaskBase);
      CtClass ctReturningType = cp.get("vpt.backbone.backend.app.TaskResult");
      CtClass[] ctPar = new CtClass[] { cp.get("vpt.backbone.backend.app.entities.MultipleTasksQueue"), cp.get("org.hibernate.Session") };
      CtClass[] ctExceptions = new CtClass[] {};

      CtMethod m = CtNewMethod.make(ctReturningType, "processPayload", ctPar, ctExceptions, methodBody, ctRes);
      ctRes.addMethod(m);
      // ctRes.writeFile();

      try(JarFile jf = new JarFile(pathDBDaemonQueueJar))
      {
         try (JarOutputStream tempJar = new JarOutputStream(new FileOutputStream(pathDBDaemonQueueJar.toString() + ".tmp")))
         {
            JarEntry je = new JarEntry(ctRes.getName().replace(".", "/") + ".class");

            tempJar.putNextEntry(je);
            tempJar.write(ctRes.toBytecode());

            Enumeration<JarEntry> jarEntries = jf.entries();
            while (jarEntries.hasMoreElements())
            {
               JarEntry entry = jarEntries.nextElement();

               tempJar.putNextEntry(entry);

               try (InputStream entryInputStream = jf.getInputStream(entry))
               {
                  byte[] buffer = new byte[1024];
                  int bytesRead = 0;
                  while ((bytesRead = entryInputStream.read(buffer)) != -1) {
                     tempJar.write(buffer, 0, bytesRead);
                 }
               }
            }
         }
         jf.close();
      }
      
      Files.move(new File(pathDBDaemonQueueJar.toString() + ".tmp"), new File(pathDBDaemonQueueJar.toString()));
      return ctRes;
   }

   @DisplayName("Simple test with one record processed succesfully and one task to validate final status of the record")
   @Test
   final void getTaskList_Return_RegisteredTasks() throws SQLException, IOException, URISyntaxException, MissingMandatoryPropertyException, InterruptedException, RuntimeException, NotFoundException, CannotCompileException
   {
      // Arrange
      String dbname = "it_" + new Object() {}.getClass().getEnclosingMethod().getName();
      resetDb(dbname);
      createPayloadTables(dbname);
      
      
      int expected;
      int actual;

      DBDaemonQueue app = new DBDaemonQueue();

      // Act
      expected = 4;
      app.parseArgs(new String[] {"--server", "localhost", "--db", dbname, "--user", "user_it", "--pass", "pass_it"});
      app.build();
      actual = app.getTaskList().size();
      
      //Assert
      Assertions.assertEquals(expected, actual);
   }
      
   @SuppressWarnings("serial")
   @DisplayName("Simple test with one record processed succesfully and one task to validate final status of the record")
   @Test
   @Timeout(20)
   final void processOneRegisterOneTask() throws SQLException, IOException, URISyntaxException, MissingMandatoryPropertyException, InterruptedException
   {
      // Arrange
      String dbname = "it_" + new Object() {}.getClass().getEnclosingMethod().getName();
      resetDb(dbname);
      
      DBDaemonQueue app = Mockito.spy(new DBDaemonQueue());
      List<Class<? extends QueueTask>> taskList = new ArrayList<>();
      MultipleTasksQueue actual;
      MultipleTasksQueue expected = new MultipleTasksQueue();
      Session s = buildSessionFactory("localhost", dbname, null, "user_it", "pass_it", new ArrayList<Class<?>>() { { add(MultipleTasksQueue.class); }}).openSession();
      insertIntoMultipleTaskQueueWithRandomPayload(1, BasicQueueTask.class.getSimpleName(), BasicQueueTask.class.getSimpleName() + "Payload", dbname);
      
      taskList.add(BasicQueueTask.class);
      Mockito.when(app.getTaskList()).thenReturn(taskList);
      
      // Act
      app.parseArgs(new String[] {"--server", "localhost", "--db", dbname, "--user", "user_it", "--pass", "pass_it"});
      app.build();
      app.startDequeuing();
      
      Thread.sleep(1000);
      
      actual = s.createQuery("select m from MultipleTasksQueue m", MultipleTasksQueue.class).setMaxResults(1).getResultList().get(0);
      expected.setStatus("finished");
      expected.setResultDetail("processed with no issues");
      expected.setOwner(BigInteger.valueOf(1));
      
      // Assert
      Mockito.verify(app, Mockito.atLeastOnce()).getTaskList();
      
      Assertions.assertEquals(expected.getStatus(), actual.getStatus());
      Assertions.assertEquals(expected.getResultDetail(), actual.getResultDetail());
      
      Assertions.assertEquals(1, actual.getAttempts());
      Assertions.assertNotNull(actual.getLastUpdate());
   }
   
   @SuppressWarnings("serial")
   @DisplayName("Running a task which returns an error status, then should set status column to 'error' value")
   @Test
   @Timeout(20)
   final void taskReturningError_FinishInErrorState() throws SQLException, IOException, URISyntaxException, MissingMandatoryPropertyException, InterruptedException
   {
      // Arrange
      String dbname = "it_" + new Object() {}.getClass().getEnclosingMethod().getName();
      resetDb(dbname);
      
      DBDaemonQueue app = Mockito.spy(new DBDaemonQueue());
      List<Class<? extends QueueTask>> taskList = new ArrayList<>();
      MultipleTasksQueue actual;
      MultipleTasksQueue expected = new MultipleTasksQueue();
      Session s = buildSessionFactory("localhost", dbname, null, "user_it", "pass_it", new ArrayList<Class<?>>() { { add(MultipleTasksQueue.class); }}).openSession();
      insertIntoMultipleTaskQueueWithRandomPayload(1, TaskReturningError.class.getSimpleName(), TaskReturningError.class.getSimpleName() + "Payload", dbname);
      
      taskList.add(TaskReturningError.class);
      Mockito.when(app.getTaskList()).thenReturn(taskList);
      
      // Act
      app.parseArgs(new String[] {"--server", "localhost", "--db", dbname, "--user", "user_it", "--pass", "pass_it"});
      app.build();
      app.startDequeuing();
      
      Thread.sleep(1000);
      
      actual = s.createQuery("select m from MultipleTasksQueue m", MultipleTasksQueue.class).setMaxResults(1).getResultList().get(0);
      expected.setStatus("error");
      expected.setResultDetail("process finished with error state. additional information: ");
      expected.setOwner(BigInteger.valueOf(1));
      
      // Assert
      Mockito.verify(app, Mockito.atLeastOnce()).getTaskList();
      
      Assertions.assertEquals(expected.getStatus(), actual.getStatus());
      Assertions.assertEquals(expected.getResultDetail(), actual.getResultDetail());
      
      Assertions.assertEquals(1, actual.getAttempts());
      Assertions.assertNotNull(actual.getLastUpdate());
   }
   

   @SuppressWarnings("serial")
   @DisplayName("Running a task which throws an exceptions, then should set status column to 'unhandled-exception' value")
   @Test
   final void taskThrowingException_SetColumn_UnhandledException() throws SQLException, IOException, URISyntaxException, MissingMandatoryPropertyException, InterruptedException
   {
      // Arrange
      String dbname = "it_" + new Object() {}.getClass().getEnclosingMethod().getName();
      resetDb(dbname);
      
      final int records = 1; 
      
      DBDaemonQueue app = Mockito.spy(new DBDaemonQueue());
      List<Class<? extends QueueTask>> taskList = new ArrayList<>();
      MultipleTasksQueue actual;
      MultipleTasksQueue expected = new MultipleTasksQueue();
      Session s = buildSessionFactory("localhost", dbname, null, "user_it", "pass_it", new ArrayList<Class<?>>() { { add(MultipleTasksQueue.class); }}).openSession();
      insertIntoMultipleTaskQueueWithRandomPayload(records, TaskThrowingUnhandledException.class.getSimpleName(), TaskThrowingUnhandledException.class.getSimpleName() + "Payload", dbname);
      
      taskList.add(TaskThrowingUnhandledException.class);
      Mockito.when(app.getTaskList()).thenReturn(taskList);
      
      // Act
      app.parseArgs(new String[] {"--server", "localhost", "--db", dbname, "--user", "user_it", "--pass", "pass_it"});
      app.build();
      app.startDequeuing();
      
      Thread.sleep(2000);
      
      actual = s.createQuery("select m from MultipleTasksQueue m", MultipleTasksQueue.class).setMaxResults(1).getResultList().get(0);
      expected.setStatus("unhandled-exception");
      expected.setResultDetail("record processing result in an unhandled exception");
      
      // Assert
      Mockito.verify(app, Mockito.atLeastOnce()).getTaskList();
      
      Assertions.assertEquals(expected.getStatus(), actual.getStatus());
      Assertions.assertEquals(true, actual.getResultDetail().startsWith(expected.getResultDetail()));
      
      Assertions.assertEquals(1, actual.getAttempts());
      Assertions.assertNotNull(actual.getLastUpdate());
   }
   
   @SuppressWarnings("serial")
   @DisplayName("Running bunch of all tasks should generate correct states")
   @Test
   final void runningAllTasks_ShouldFinishWithRightValues() throws SQLException, IOException, URISyntaxException, MissingMandatoryPropertyException, InterruptedException
   {
      // Arrange
      String dbname = "it_" + new Object() {}.getClass().getEnclosingMethod().getName();
      resetDb(dbname);
      
      DBDaemonQueue app = Mockito.spy(new DBDaemonQueue());
      app.parseArgs(new String[] {"--server", "localhost", "--db", dbname, "--user", "user_it", "--pass", "pass_it"});
      app.build();

      resetDb(dbname);
      
      final int records = 400 / app.getTaskList().size();
      List<?> actual;
      List<Object[]> expected = new ArrayList<>();
      Session s = buildSessionFactory("localhost", dbname, null, "user_it", "pass_it", new ArrayList<Class<?>>() { { add(MultipleTasksQueue.class); }}).openSession();
      insertIntoMultipleTaskQueueWithRandomPayload(records, BasicQueueTask.class.getSimpleName(), BasicQueueTask.class.getSimpleName() +"Payload", dbname);
      insertIntoMultipleTaskQueueWithRandomPayload(records, TaskExecutingLongMathOperationAndExecutesOK.class.getSimpleName(), TaskExecutingLongMathOperationAndExecutesOK.class.getSimpleName() + "Payload", dbname);
      insertIntoMultipleTaskQueueWithRandomPayload(records, TaskReturningError.class.getSimpleName(), TaskReturningError.class.getSimpleName() + "Payload", dbname);
      insertIntoMultipleTaskQueueWithRandomPayload(records, TaskThrowingUnhandledException.class.getSimpleName(), TaskThrowingUnhandledException.class.getSimpleName() + "Payload", dbname);
            
      // Act
      app.startDequeuing();
      
      Thread.sleep(20000);

      //Should be inserted in order by taskName ascending 
      expected.add(new Object[] {BasicQueueTask.class.getSimpleName(), "finished", new Long(100)});
      expected.add(new Object[] {TaskExecutingLongMathOperationAndExecutesOK.class.getSimpleName(), "finished", new Long(100)});
      expected.add(new Object[] {TaskReturningError.class.getSimpleName(), "error", new Long(100)});
      expected.add(new Object[] {TaskThrowingUnhandledException.class.getSimpleName(), "unhandled-exception", new Long(100)});
      
      actual = s.createQuery("select m.taskName, m.status, count(*) from MultipleTasksQueue m group by taskName, status order by m.taskName").list();

      // Assert
      Assertions.assertEquals(actual.size(), app.getTaskList().size());
      for (int i = 0; i < actual.size(); i++)
      {
         Object[] cols = (Object[])actual.get(i);
         Assertions.assertArrayEquals(expected.get(i), cols);
      }
   }
   
   @DisplayName("Should not process anything if there is no tasks, but must log message")
   @Test
   @Disabled
   final void noTasks_ShowMessage() throws SQLException, IOException, URISyntaxException, MissingMandatoryPropertyException, InterruptedException
   {
      //TODO: Implement
      throw new NotImplementedException("This test method is not implemented yet");
   }
   
   @DisplayName("We will process a bunch of created tasks and all IDs must be different")
   @Test
   final void taskProcessingIDs_ShoulNotBe_repeated() throws SQLException, IOException, URISyntaxException, MissingMandatoryPropertyException, InterruptedException
   {
      // Arrange
      String dbname = "it_" + new Object() {}.getClass().getEnclosingMethod().getName();
      resetDb(dbname);
      
      int records = 1000;
      DBDaemonQueue app = Mockito.spy(new DBDaemonQueue());
      List<Class<? extends QueueTask>> taskList = new ArrayList<>();
      insertIntoMultipleTaskQueueWithRandomPayload(records, BasicQueueTask.class.getSimpleName(), BasicQueueTask.class.getSimpleName() + "Payload", dbname);
      int repeatedIds = 0;
      
      taskList.add(BasicQueueTask.class);
      Mockito.when(app.getTaskList()).thenReturn(taskList);
      
      // Act
      app.parseArgs(new String[] {"--server", "localhost", "--db", dbname, "--user", "user_it", "--pass", "pass_it"});
      app.build();
      app.startDequeuing();
      
      Thread.sleep(20000);
            
      for (BigInteger id : TaskIdentifierStorage.taskIdentifiers)
      {
         if(TaskIdentifierStorage.taskIdentifiers.contains(id))
         {
            repeatedIds++;
         }
      }
      
      // Assert
      Mockito.verify(app, Mockito.atLeastOnce()).getTaskList();
      
      Assertions.assertEquals(records, repeatedIds);
   }
   
   @DisplayName("Will try to simulate a deadlock when dequeueing tasks")
   @Test
   //TODO: Disabled because I didn't find any reliable way to replicate a deadlock
   @Disabled
   final void taskDeadlock_ShouldReprocess_Record() throws SQLException, IOException, URISyntaxException, MissingMandatoryPropertyException, InterruptedException
   {
      // Arrange
      String dbname = "it_" + new Object() {}.getClass().getEnclosingMethod().getName();
      resetDb(dbname);
      DBToolsVariation.setMySQLGlobalVariable("innodb_lock_wait_timeout", "5", dbname);
      
      int records = 10;
      DBDaemonQueue app = Mockito.spy(new DBDaemonQueue());
      List<Class<? extends QueueTask>> taskList = new ArrayList<>();
      
      insertIntoMultipleTaskQueue(records, "BasicQueueTask", dbname);
      insertIntoMultipleTaskQueue(records, "TaskReturningError", dbname);
            
      taskList.add(BasicQueueTask.class);
      taskList.add(TaskReturningError.class);
      Mockito.when(app.getTaskList()).thenReturn(taskList);
      
      // Act
      app.parseArgs(new String[] {"--server", "localhost", "--db", dbname, "--user", "user_it", "--pass", "pass_it"});
      app.build();
      
//      DBTools.startLock("update vpt_multiple_tasks_queue set status = 'other' where id = 1", dbname);
      Thread.sleep(500);
      
      app.startDequeuing();
      
      Thread.sleep(240000000);
      
      
      // Assert
      Mockito.verify(app, Mockito.atLeastOnce()).getTaskList();
      
//      Assertions.assertEquals(records, repeatedIds);
   }
   
}
