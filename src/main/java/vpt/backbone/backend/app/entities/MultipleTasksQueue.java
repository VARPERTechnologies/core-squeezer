package vpt.backbone.backend.app.entities;

import java.math.BigInteger;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.Index;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.UpdateTimestamp;

@Entity(name = "MultipleTasksQueue")
@Table(name = "vpt_multiple_tasks_queue",
indexes = {
      @Index(columnList = "id, owner_id, status", name = "user_id_hidx")
})
@NamedQueries(value = {
      @NamedQuery(name="takeRecord", query="select c from MultipleTasksQueue c where c.taskName=:taskName and ownerId is null and status='new' order by c.id asc"),
      @NamedQuery(name="takeRecordByOwner", query="select c from MultipleTasksQueue c where c.taskName=:taskName and ownerId=:ownerId and status='taken' order by c.id asc"),
      @NamedQuery(name="updateStatusRecordByIds", query="update MultipleTasksQueue set status=:status, ownerId=:ownerId where id in (:idCollection)"),
      @NamedQuery(name="updateStatusRecordById", query="update MultipleTasksQueue set status=:status, ownerId=:ownerId where id = :id"),
      @NamedQuery(name="updateStatusRecordByStatus", query="update MultipleTasksQueue set status=:status, ownerId=:ownerId where status = 'new' and ownerId is null"),
      @NamedQuery(name="taskDistribution", query="select c.taskName, count(*) from MultipleTasksQueue c where c.status = 'new' group by c.taskName")
})

@DynamicUpdate

public class MultipleTasksQueue
{
   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   BigInteger id;
   
   @Column(name="task_name", nullable=false)
   String taskName;
   
   @Column(name="owner_id", nullable=true)
   BigInteger ownerId;
   
   @Column(nullable=false)
   @ColumnDefault(value = "new")
   String status;
   
   @Column(name="result_detail")
   String resultDetail;
   
//   @Column
//   @ColumnDefault(value="0")
//   Integer priority;
   
   @Column()
   Integer attempts = 0;
   
   @Column(name="creation_time", insertable = false, updatable = false, nullable=false)
   Date creationTime;
   
   @Column(name="last_update")
   @UpdateTimestamp
   Date lastUpdateTime;
   
   @Transient
   List<?> payload;
   
   public void setStatus(String s)
   {
      status = s;
   }

   public BigInteger getId()
   {
      return id;
   }

   public void setResultDetail(String rd)
   {
      resultDetail = rd;
   }

   public void setOwner(BigInteger id)
   {
      ownerId = id;
   }

   public String getStatus()
   {
      return status;
   }

   public String getResultDetail()
   {
      return resultDetail;
   }

   public BigInteger getOwner()
   {
      return ownerId;
   }

   public Integer getAttempts()
   {
      return attempts;
   }

   public Date getLastUpdate()
   {
      return lastUpdateTime;
   }

   public void incrementAttempts()
   {
      if(attempts == null) attempts = 0;
      
      attempts++;
   }
   
   public List<?> getPayload()
   {
      return payload;
   }
   
   public void setPayload(List<?> tblPayload)
   {
      this.payload = tblPayload;
   }
   
   public String getTaskName()
   {
      return taskName;
   }
   
   public void setTaskName(String tname)
   {
      taskName = tname;
   }
}
