package vpt.backbone.backend.app;

import org.hibernate.cfg.DefaultNamingStrategy;

public class VNamingStrategy extends DefaultNamingStrategy
{
   /**
    * 
    */
   private static final long serialVersionUID = -709588692498364391L;

   @Override
   public  String tableName(String tableName) {
      return tableName;
   }
}
