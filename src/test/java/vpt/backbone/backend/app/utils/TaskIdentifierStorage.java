package vpt.backbone.backend.app.utils;

import java.math.BigInteger;
import java.util.concurrent.ConcurrentLinkedQueue;

public class TaskIdentifierStorage
{

   public static ConcurrentLinkedQueue<BigInteger> taskIdentifiers = new ConcurrentLinkedQueue<BigInteger>();

}
