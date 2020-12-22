package vpt.backbone.backend.app;

import org.hibernate.Session;

public interface DBTableQueue
{
//   public void onExtractRecords(Session s);
   
   public void onRecordTaken(Object payload, Session s);
   
//   public <T> CriteriaQuery<T> getPollingCriteria(CriteriaBuilder b, Session s);
   
}
