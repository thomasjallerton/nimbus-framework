package models;


import com.nimbusframework.nimbusaws.annotation.annotations.document.DynamoDbDocumentStore;
import com.nimbusframework.nimbuscore.annotations.persistent.Attribute;
import com.nimbusframework.nimbuscore.annotations.persistent.Key;
import java.util.Objects;

@DynamoDbDocumentStore
public class DynamoDbDocument {

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    DynamoDbDocument document = (DynamoDbDocument) o;
    return Objects.equals(name, document.name) &&
        Objects.equals(owner, document.owner);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, owner);
  }

  @Key
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
    return "Document(" + name + ", " + owner + ")";
  }
}
