package vpt.backbone.backend.app.testing.extensions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Optional;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestExecutionExceptionHandler;
import org.junit.platform.commons.util.AnnotationUtils;

public class SystemExitExtension implements BeforeEachCallback, AfterEachCallback, TestExecutionExceptionHandler {
   private Integer expectedStatusCode;  
   private Integer firstStatusCode;
   
   @Override
   public void beforeEach(final ExtensionContext context) throws Exception
   {
      // Get the expected exit status code, if any
      getAnnotation(context).ifPresent(code -> expectedStatusCode = code.value());
   }

   @Override
   public void handleTestExecutionException(final ExtensionContext context, final Throwable throwable) throws Throwable {
      if (!(throwable instanceof UnexpectedExitException)) {
         throw throwable;
      }
   }

   @Override
   public void afterEach(final ExtensionContext context) throws Exception 
   {
      Object testInstance = context.getTestInstance().get();   
      if (testInstance instanceof SystemExitValidator)
      {
         firstStatusCode = ((SystemExitValidator) testInstance).getFirstExitStatusCode();
      }
      
      if (expectedStatusCode == Integer.MAX_VALUE) {
          assertNotNull(firstStatusCode, "Expected System.exit() to be called, but it was not");
      } else {
          assertEquals(
                  expectedStatusCode,
                  firstStatusCode,
                  "Expected System.exit(" + expectedStatusCode + ") to be called, but it was not."
          );
      }
   }

   // Find the annotation on a method, or failing that, a class.
   private Optional<ExpectSystemExit> getAnnotation(final ExtensionContext context) {
       final Optional<ExpectSystemExit> method = AnnotationUtils.findAnnotation(context.getTestMethod(), ExpectSystemExit.class);

       if (method.isPresent()) {
           return method;
       } else {
           return AnnotationUtils.findAnnotation(context.getTestClass(), ExpectSystemExit.class);
       }
   }

}