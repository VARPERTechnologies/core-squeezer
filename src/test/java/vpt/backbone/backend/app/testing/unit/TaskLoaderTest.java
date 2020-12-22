package vpt.backbone.backend.app.testing.unit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import vpt.backbone.backend.app.QueueTask;
import vpt.backbone.backend.app.TaskLoader;
import vpt.backbone.backend.app.tasks.BasicQueueTask;
import vpt.backbone.backend.app.tasks.ClassWithoutRequiredImplementations;
import vpt.backbone.backend.app.tasks.TaskExecutingLongMathOperationAndExecutesOK;
import vpt.backbone.backend.app.tasks.TaskReturningError;
import vpt.backbone.backend.app.tasks.TaskThrowingUnhandledException;

class TaskLoaderTest
{
   @DisplayName("getPackageClasses should return all QueueTask classes")
   @Test
   public void test1() throws IOException
   {
      List<Class<? extends QueueTask>> obtained;
      List<Class<?>> expected;
      
      expected = new ArrayList<Class<?>>();
      expected.add(BasicQueueTask.class);
      expected.add(TaskExecutingLongMathOperationAndExecutesOK.class);
      expected.add(TaskReturningError.class);
      expected.add(TaskThrowingUnhandledException.class);
      obtained = TaskLoader.getPackageClasses("vpt.backbone.backend.app.tasks");
      
      Assertions.assertIterableEquals(expected, obtained);
   }
}
