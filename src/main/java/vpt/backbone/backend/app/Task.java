package vpt.backbone.backend.app;

import java.math.BigInteger;

public interface Task
{
   public BigInteger getIdentifier();
   
   public void onLoad();
   
   public void onFinish();
   
}
