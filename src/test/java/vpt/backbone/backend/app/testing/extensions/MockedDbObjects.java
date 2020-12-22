package vpt.backbone.backend.app.testing.extensions;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.mockito.Mockito;

public interface MockedDbObjects
{
   SessionFactory mockedSessionFactory = Mockito.mock(SessionFactory.class);
   Session mockedSession = Mockito.mock(Session.class);
   Transaction mockedTransaction = Mockito.mock(Transaction.class);
   
   default void buildMocks() 
   {
      Mockito.when(mockedSessionFactory.openSession()).thenReturn(mockedSession);
      Mockito.when(mockedSession.beginTransaction()).thenReturn(mockedTransaction);
   }
}
