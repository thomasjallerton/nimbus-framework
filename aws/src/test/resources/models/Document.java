package models;

import com.nimbusframework.nimbuscore.annotations.document.DocumentStore;
import com.nimbusframework.nimbuscore.annotations.persistent.Attribute;
import com.nimbusframework.nimbuscore.annotations.persistent.Key;

import java.util.Objects;

@DocumentStore
public class Document {

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Document document = (Document) o;
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
