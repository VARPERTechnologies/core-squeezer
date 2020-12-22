package vpt.backbone.backend.app;

import java.io.File;
import java.util.*;

/**
 * 
 */
public class ConfigFile extends File
{
   /**
    * 
    */
   private static final long serialVersionUID = 2200319089205147876L;
   
   /**
    * 
    */
   private ConfigNamespace namespaces;

   /**
    * @param path
    */
   public ConfigFile(String path) {
      super(path);
      // TODO implement here
   }

   /**
    * @return
    */
   public HashSet<ConfigNamespace> getNamespaceList() {
      // TODO implement here
      return null;
   }

   /**
    * 
    */
   private void parse() {
      // TODO implement here
   }

   /**
    * @param name 
    * @return
    */
   private boolean isVariable(String name) {
      // TODO implement here
      return false;
   }

   /**
    * @param str 
    * @return
    */
   private boolean isComment(String str) {
      // TODO implement here
      return false;
   }

   /**
    * @param val 
    * @return
    */
   private boolean isValidValue(String val) {
      // TODO implement here
      return false;
   }

   /**
    * @param val 
    * @return
    */
   private boolean isNumeric(String val) {
      // TODO implement here
      return false;
   }

   /**
    * @param val 
    * @return
    */
   private boolean isJSON(String val) {
      // TODO implement here
      return false;
   }

   /**
    * @param val 
    * @return
    */
   private boolean isXML(String val) {
      // TODO implement here
      return false;
   }
   
   /**
    * @param str 
    * @return
    */
   private boolean resolveEnvVars(String str) {
      // TODO implement here
      return false;
   }

}