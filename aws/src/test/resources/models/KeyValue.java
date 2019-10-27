package models;

import com.nimbusframework.nimbuscore.annotations.keyvalue.KeyValueStore;
import com.nimbusframework.nimbuscore.annotations.persistent.Attribute;

import java.util.Objects;

@KeyValueStore(keyType = Integer.class)
public class KeyValue {

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KeyValue keyValue = (KeyValue) o;
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
