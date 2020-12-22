package vpt.backbone.backend.app;

import java.math.BigInteger;
import java.util.HashMap;

import vpt.backbone.backend.app.entities.MultipleTasksQueue;

public interface TaskQueueManager
{
   public HashMap<String, Long> getTasksDistribution();
   public Long getTaskDistributionTotal();
   public MultipleTasksQueue dequeue(String taskName, BigInteger ownerId, String payload);
}
