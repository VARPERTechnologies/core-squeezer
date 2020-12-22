package vpt.backbone.backend.app;

import java.util.logging.Level;

public class MessageCatalog
{
   private String code;
   private String description;
   private Level severity;
   
   public MessageCatalog(String code, String description, Level severity)
   {
      this.code = code;
      this.description = description;
      this.severity = severity;
   }

   /**
    * @return the code
    */
   public String getCode()
   {
      return code;
   }

   /**
    * @param code the code to set
    */
   public void setCode(String code)
   {
      this.code = code;
   }

   /**
    * @return the message
    */
   public String getPrintableMessage()
   {
      return "Code " + code + ". "+ description;
   }
   
   /**
    * @param description the message to set
    */
   public String getDescription()
   {
      return description;
   }
   
   /**
    * @param description the message to set
    */
   public void setDescription(String description)
   {
      this.description = description;
   }

   /**
    * @return the severity
    */
   public Level getSeverity()
   {
      return severity;
   }

   /**
    * @param severity the severity to set
    */
   public void setSeverity(Level severity)
   {
      this.severity = severity;
   }
   
   @Override
   public int hashCode() { 
       return this.code.hashCode(); 
   }
   @Override
   public boolean equals(Object obj) {
       return this.code.equals(((MessageCatalog) obj).getCode());
   }
   
}
