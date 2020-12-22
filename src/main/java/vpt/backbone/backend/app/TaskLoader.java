package vpt.backbone.backend.app;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.common.reflect.ClassPath;

public class TaskLoader
{
   @SuppressWarnings("unchecked")
   public static List<Class<? extends QueueTask>> getPackageClasses(String packageName) throws IOException
   {
      final ClassLoader loader = Thread.currentThread().getContextClassLoader();
      ClassPath classpath = ClassPath.from(loader); // scans the class path used by classloader
      
      ArrayList<Class<? extends QueueTask>> res = new ArrayList<Class<? extends QueueTask>>();
      for (ClassPath.ClassInfo classInfo : classpath.getTopLevelClasses(packageName))
      {
         Class<?> cls = classInfo.load();
         Class<?> scls = cls.getSuperclass();
         if(scls.equals(QueueTask.class) && cls.isAnnotationPresent(PayloadName.class)) 
         {
            res.add((Class<? extends QueueTask>) cls);
         }
      }
      return res;
   }
}
