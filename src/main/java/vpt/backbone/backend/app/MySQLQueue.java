package vpt.backbone.backend.app;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;
import javax.persistence.criteria.CriteriaBuilder;

import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.LockOptions;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import vpt.backbone.backend.app.entities.MultipleTasksQueue;

public class MySQLQueue implements TableQueueManager
{
   Session currentSession;

   public MySQLQueue(Session s)
   {
      currentSession = s;
   }

   //TODO: Improve payload definition, SQL injection prone 
   @Override
   public MultipleTasksQueue dequeue(String taskName, BigInteger ownerId, String payload)
   {
      
      Transaction t = currentSession.beginTransaction();
      
      MultipleTasksQueue qrecord = null;
      
      try
      {
         qrecord = currentSession.createNativeQuery("select * from vpt_multiple_tasks_queue c where c.task_name=:taskName and owner_id is null and status='new' order by c.id asc limit 1 for update skip locked", MultipleTasksQueue.class)
               .setParameter("taskName", taskName)
               .getSingleResult();
      }
      catch(NoResultException e)
      {
         t.commit();
         return null;
      }

      Query<?> upd = currentSession.createNamedQuery("updateStatusRecordById").setParameter("status", "taken")
            .setParameter("ownerId", ownerId).setParameter("id", qrecord.getId());

      upd.executeUpdate();
      
      t.commit();
      
      try
      {
         List<?> tblPayload = currentSession.createNativeQuery("select * from " + payload + " p where p.task_id=:taskId")
               .setParameter("taskId", qrecord.getId())
               .list();
         
         qrecord.setPayload(tblPayload);
      }
      catch(PersistenceException e)
      {
         
      }
      catch(Exception e)
      {
         
      }
      
      return qrecord;
   }
   
   
   public MultipleTasksQueue dequeue2(String taskName, BigInteger ownerId)
   {
      // CriteriaQuery<MultipleTasksQueue> cq =
      // builder.createQuery(MultipleTasksQueue.class);
      // CriteriaUpdate<MultipleTasksQueue> cu =
      // builder.createCriteriaUpdate(MultipleTasksQueue.class);

      // Root<MultipleTasksQueue> rq = cq.from(MultipleTasksQueue.class);
      // Root<MultipleTasksQueue> ru = cu.from(MultipleTasksQueue.class);

      //
      // cq.select(rq.get("id"))
      // .where(
      // builder.equal(rq.get("taskName"), taskName),
      // builder.equal(rq.get("ownerId"), null),
      // builder.equal(rq.get("status"), "new")
      // )
      // .orderBy(builder.asc(rq.get("id")));

      // Query<MultipleTasksQueue> query = currentSession.createQuery(cq)
      // .setLockMode(LockModeType.PESSIMISTIC_WRITE)
      // .setMaxResults(1);

      // TODO: PESSIMISTIC_WRITE uses for update hint, but we need "for update skip
      // locked" not supported by hibernate
      // This could be solved with "update first" approach
      // Query<MultipleTasksQueue> query =

      Transaction t = currentSession.beginTransaction();

      List<MultipleTasksQueue> qrecord = currentSession.createNamedQuery("takeRecord", MultipleTasksQueue.class)
            .setParameter("taskName", taskName)
            // .setLockMode(LockModeType.PESSIMISTIC_WRITE)
            .setLockOptions(new LockOptions(LockMode.PESSIMISTIC_WRITE).setTimeOut(LockOptions.SKIP_LOCKED))
            // .setLockOptions(new LockOptions(LockMode.UPGRADE_SKIPLOCKED))
            .setMaxResults(1).list();

      if (qrecord.size() == 0)
      {
         t.commit();
         return null;
      }

      ArrayList<BigInteger> ids = new ArrayList<BigInteger>();

      qrecord.forEach(id ->
      {
         ids.add(id.getId());
      });

      Query<?> upd = currentSession.createNamedQuery("updateStatusRecord").setParameter("status", "taken")
            .setParameter("ownerId", ownerId).setParameterList("idCollection", ids);

      upd.executeUpdate();

      // In<BigInteger> inClause = builder.in(ru.get("id"));
      //
      // for (MultipleTasksQueue multipleTasksQueue : qrecord)
      // {
      // inClause.value(multipleTasksQueue.getId());
      // }
      //
      // cu
      // .set(ru.get("status"), "taken")
      // .set(ru.get("ownerId"), ownerId)
      // .where(inClause);

      t.commit();

      return qrecord.get(0);
   }

}
