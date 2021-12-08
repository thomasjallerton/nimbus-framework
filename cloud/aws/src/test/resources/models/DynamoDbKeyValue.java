package models;

import com.nimbusframework.nimbusaws.annotation.annotations.keyvalue.DynamoDbKeyValueStore;
import com.nimbusframework.nimbuscore.annotations.persistent.Attribute;
import java.util.Objects;

@DynamoDbKeyValueStore(tableName = "keytable", keyType = Integer.class)
public class DynamoDbKeyValue {

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    DynamoDbKeyValue keyValue = (DynamoDbKeyValue) o;
    return Objects.equals(name, keyValue.name) &&
        Objects.equals(owner, keyValue.owner);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, owner);
  }

  @Attribute
  private String name;

  @Attribute
  private Person owner;


  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Person getOwner() {
    return owner;
  }

  public void setOwner(Person owner) {
    this.owner = owner;
  }

  @Override
  public String toString() {
    return "KeyValue(" + name + ", " + owner + ")";
  }

}
