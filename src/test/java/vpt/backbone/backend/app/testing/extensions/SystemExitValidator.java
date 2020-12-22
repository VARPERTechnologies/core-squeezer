package vpt.backbone.backend.app.testing.extensions;

import java.security.Permission;

public abstract class SystemExitValidator
{
   SecurityManager oldSM;
   SecurityManager newSM;

   private Integer firstExitStatusCode;

   public SecurityManager getOldSM()
   {
      return oldSM;
   }

   public Integer getFirstExitStatusCode()
   {
      return firstExitStatusCode;
   }
   
   public SecurityManager getNewSM()
   {
      return newSM;
   }
   
   public void preventSystemExit(boolean b)
   {
      if (b)
      {
         setNewSecurityManager();
      } else
      {
         setOldSecurityManager();
      }
   }

   private void setOldSecurityManager()
   {
      System.setSecurityManager(oldSM);
   }

   private void setNewSecurityManager()
   {
      if (!getClass().isAnnotationPresent(ExpectSystemExit.class))
      {
         newSM = new SecurityManager()
         {
            @Override
            public void checkExit(final int statusCode)
            {
//               if (firstExitStatusCode == null)
//               {
                  firstExitStatusCode = statusCode;
//               }
               throw new UnexpectedExitException(statusCode, "Application is trying to System.exit(" + statusCode
                     + "). It is forbidden during unit testing. You should use @ExpectSystemExit annotation");
            }

            @Override
            public void checkPermission(Permission perm)
            {
               if (oldSM != null)
               {
                  oldSM.checkPermission(perm);
               }
            }
         };
         oldSM = System.getSecurityManager();
         System.setSecurityManager(newSM);
      }
   }
}
