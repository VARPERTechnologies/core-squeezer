package vpt.backbone.backend.app.entities;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.persistence.LockModeType;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaBuilder.In;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Root;
import org.hibernate.Transaction;
import org.hibernate.LockMode;
import org.hibernate.LockOptions;
import org.hibernate.Session;
import org.hibernate.query.Query;

import com.mchange.lang.ArrayUtils;

import vpt.backbone.backend.app.MySQLQueue;
import vpt.backbone.backend.app.TableQueueManager;
import vpt.backbone.backend.app.TaskQueueManager;

public class MultipleTasksQueueManager implements TaskQueueManager
{
   Session currentSession;
   MultipleTasksQueue mtq;
   TableQueueManager queueProcessor;
   
//   public MultipleTasksQueueManager(){}

   public MultipleTasksQueueManager(Session s)
   {
      currentSession = s;
      
      setTableQueueManager(new MySQLQueue(s));
   }
   
   @Override
   public MultipleTasksQueue dequeue(String taskName, BigInteger ownerId, String payload) 
   {
      return queueProcessor.dequeue(taskName, ownerId, payload);
   }

   public void setTableQueueManager(TableQueueManager qm) {
      queueProcessor = qm; 
   }
   
   @Override
   public HashMap<String, Long> getTasksDistribution()
   {
      List<?> qrecord =
            currentSession.createQuery("select c.taskName, count(*) from MultipleTasksQueue c where c.status = 'new' and ownerId is null group by c.taskName")
            .list();
      HashMap<String, Long> res = new HashMap<String, Long>();
      for (Object objects : qrecord)
      {
         Object[] cols = (Object[])objects;
         res.put((String) cols[0], (Long)cols[1]);
      }
      return res;
   }
   
   public Long getTaskDistributionTotal()
   {
      Long qrecord = currentSession.createQuery("select count(*) from MultipleTasksQueue c where c.status = 'new' and ownerId is null ", Long.class).getSingleResult();
      
      return qrecord;
   }
}
