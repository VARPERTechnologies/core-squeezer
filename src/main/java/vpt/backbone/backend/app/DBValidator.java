package vpt.backbone.backend.app;

import java.util.*;

import vpt.backbone.backend.app.dbobjects.*;

/**
 * 
 */
public class DBValidator {

   /**
    * Default constructor
    */
   public DBValidator() {
   }

   /**
    * 
    */
   public List<Table> tables;

   /**
    * 
    */
   public List<Index> indexes;

   /**
    * 
    */
   public List<ForeignKey> foreignKeys;

   /**
    * 
    */
   public List<Check> checks;

   /**
    * 
    */
   public void checkDBModel() {
      // TODO implement here
   }

   /**
    * 
    */
   public void installModel() {
      // TODO implement here
   }

   /**
    * @param table
    */
   public void addTable(Table table) {
      // TODO implement here
   }

   /**
    * @param table
    */
   public void removeTable(String table) {
      // TODO implement here
   }

   /**
    * @param name 
    * @param onTable 
    * @param refTable 
    * @param fields
    */
   public void addForeignKey(String name, String onTable, String refTable, List<String> fields) {
      // TODO implement here
   }

   /**
    * @param name 
    * @param onTable 
    * @param refTable 
    * @param refFields
    */
   public void addForeignKey(String name, Table onTable, Table refTable, Column refFields) {
      // TODO implement here
   }

   /**
    * @param name 
    * @param onTable 
    * @param condition
    */
   public void addCheck(String name, String onTable, String condition) {
      // TODO implement here
   }

   /**
    * @param name 
    * @param onTable 
    * @param condition
    */
   public void addCheck(String name, Table onTable, String condition) {
      // TODO implement here
   }

   /**
    * @param name 
    * @param type IndexType 
    * @param onTable 
    * @param columns IndexColumnDefinition[1..*]
    */
   public void addIndex(String name, IndexType type, Table onTable, IndexColumn columns) {
      // TODO implement here
   }

   /**
    * @param name 
    * @param typeIndexType 
    * @param onTable 
    * @param columns IndexColumnDefinition[1..*]
    */
   public void addIndex(String name, IndexType type, String onTable, IndexColumn columns ) 
   {
      // TODO implement here
   }

   /**
    * @return
    */
   public String getCheckResult() {
      // TODO implement here
      return "";
   }

}