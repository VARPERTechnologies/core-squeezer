package vpt.backbone.backend.app;

public enum TaskResult
{
   OK,
   ERROR;
   
   //FIXME: this message is going to database so it can be used to inject SQL
   public String Message;
   
}
