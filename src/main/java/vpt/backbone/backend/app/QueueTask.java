package vpt.backbone.backend.app;

import java.math.BigInteger;
import java.util.HashSet;
import org.hibernate.Session;

import vpt.backbone.backend.app.entities.MultipleTasksQueue;

public abstract class QueueTask implements Task, DBTableQueue
{
   BigInteger taskId;

   public BigInteger getIdentifier()
   {
      return taskId;
   }

   public void setIdentifier(BigInteger bigInteger)
   {
      taskId = bigInteger;
   }
   
//   public abstract HashSet<Class<?>> declareEntities();
   
   public abstract TaskResult processPayload(MultipleTasksQueue mtq, Session s);

   public void onLoad() {};
   
   public void onFinish() {};
   
   public void onRecordTaken(Object payload, Session s) {}

//   public abstract TaskResult processPayload(BigInteger mtq, Session s);
   
//   protected String getTableName()
//   {
//      return this.getClass().getSimpleName();
//   }

}
