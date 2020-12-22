package vpt.backbone.backend.app;

import java.math.BigInteger;

import vpt.backbone.backend.app.entities.MultipleTasksQueue;

public interface TableQueueManager
{
   public MultipleTasksQueue dequeue(String taskName, BigInteger ownerId, String payload);
}
