package vpt.backbone.backend.app.tasks;

import org.hibernate.Session;

import vpt.backbone.backend.app.TaskName;
import vpt.backbone.backend.app.PayloadName;
import vpt.backbone.backend.app.QueueTask;
import vpt.backbone.backend.app.TaskResult;
import vpt.backbone.backend.app.entities.MultipleTasksQueue;
import vpt.backbone.backend.app.utils.TaskIdentifierStorage;

@TaskName("TaskReturningError")
@PayloadName("TaskReturningErrorPayload")
public class TaskReturningError extends QueueTask
{
   @Override
   public TaskResult processPayload(MultipleTasksQueue mtq, Session s)
   {
      TaskIdentifierStorage.taskIdentifiers.add(mtq.getId());
      return TaskResult.ERROR;
   }

}
