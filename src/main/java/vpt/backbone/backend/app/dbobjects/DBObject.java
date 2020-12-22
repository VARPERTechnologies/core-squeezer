package vpt.backbone.backend.app.dbobjects;

import java.util.*;

/**
 * 
 */
public abstract class DBObject {

   /**
    * Default constructor
    */
   public DBObject() {
   }

   /**
    * 
    */
   protected String ownerTableName;

   /**
    * 
    */
   protected String name;

   /**
    * 
    */
   protected List<Column> columns;

   /**
    * 
    */
   protected List<Hint> hints;

   /**
    * @param name
    */
   public void setOwnerTable(String name) {
      // TODO implement here
   }

   /**
    * @return
    */
   public String getOwnerTable() {
      // TODO implement here
      return "";
   }

   /**
    * @param name
    */
   public void setName(String name) {
      // TODO implement here
   }

   /**
    * @return
    */
   public String getName() {
      // TODO implement here
      return "";
   }

   /**
    * @param cols
    */
   public void setColumns(Set<Column> cols) {
      // TODO implement here
   }

   /**
    * @return
    */
   public Set<Column> getColumns() {
      // TODO implement here
      return null;
   }

   /**
    * @param col 
    * @return
    */
   public Set<Column> addColumn(Column col) {
      // TODO implement here
      return null;
   }

   /**
    * @param index
    */
   public void removeColumn(int index) {
      // TODO implement here
   }

   /**
    * @param index 
    * @return
    */
   public Column getColumn(int index) {
      // TODO implement here
      return null;
   }

   /**
    * @param prop 
    * @return
    */
   public List<Hint> addProperty(Hint prop) {
      // TODO implement here
      return null;
   }

   /**
    * @param index
    */
   public void removeProperty(int index) {
      // TODO implement here
   }

   /**
    * @param props
    */
   public void setProperties(List<Hint> props) {
      // TODO implement here
   }

   /**
    * @return
    */
   public List<Hint> getProperties() {
      // TODO implement here
      return null;
   }

   /**
    * @param index 
    * @return
    */
   public Hint getProperty(int index) {
      // TODO implement here
      return null;
   }

}