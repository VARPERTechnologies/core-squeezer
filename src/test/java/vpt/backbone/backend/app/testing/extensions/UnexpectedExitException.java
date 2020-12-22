package vpt.backbone.backend.app.testing.extensions;

public class UnexpectedExitException extends SecurityException 
{
   private static final long serialVersionUID = 6111134791561647357L;
   
   int exitcode;
   String message;
   
   public UnexpectedExitException(int exitcode, String message)
   {
      super();
      this.exitcode = exitcode;
      this.message = message;
   }
   
   @Override
   public String getMessage()
   {
      return message;
   }

   public int getExitCode()
   {
      return exitcode;
   }
}
