package vpt.backbone.backend.app.tasks;

import org.hibernate.Session;

import vpt.backbone.backend.app.TaskName;
import vpt.backbone.backend.app.PayloadName;
import vpt.backbone.backend.app.QueueTask;
import vpt.backbone.backend.app.TaskResult;
import vpt.backbone.backend.app.entities.MultipleTasksQueue;
import vpt.backbone.backend.app.utils.TaskIdentifierStorage;

@TaskName("TaskThrowingUnhandledException")
@PayloadName("TaskThrowingUnhandledExceptionPayload")
public class TaskThrowingUnhandledException extends QueueTask
{
   @Override
   public TaskResult processPayload(MultipleTasksQueue mtq, Session s)
   {
      TaskIdentifierStorage.taskIdentifiers.add(mtq.getId());
      
      double value = 5 / 0;
      value++;
      System.out.print(value);
      return TaskResult.OK;
   }
}
