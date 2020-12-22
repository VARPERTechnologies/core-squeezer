package vpt.backbone.backend.app;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.NotImplementedException;

public class DBDaemon extends DBApp
{
      
   public DBDaemon(String logname) 
   {
      super(logname);
      // TODO Auto-generated constructor stub
   }

   public DBDaemon(Options options) throws ParseException
   {
      super(options);
      // TODO Auto-generated constructor stub
   }

   public DBDaemon()
   {
      super();
      // TODO Auto-generated constructor stub
   }

   public DBDaemon(Options options, String logname) throws ParseException
   {
      super(options, logname);
      // TODO Auto-generated constructor stub
   }
   
   private void init() {
      throw new NotImplementedException("");
   }
   
}
