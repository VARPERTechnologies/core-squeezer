package classes.only.testing;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import org.hibernate.annotations.ColumnDefault;

/**
 * A class only for testing purposes, it shouldn't be loaded by DBDaemonQueue
 * @author edixon
 *
 */

@Entity(name = "CarDefinitionEntityValidatorTesting")
@Table(name = "CarDefinitionEntityValidatorTesting")
public class CarDefinitionEntityValidatorTesting
{
   @Id
   String plate;
   
   @Column(name="model", nullable=false)
   String model;
   
   @Column(name="wheel_size", nullable=true)
   Double wheelSize;
   
   @ColumnDefault(value = "sedan")
   String type;
   
}
