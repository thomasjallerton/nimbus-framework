package models;

import com.nimbusframework.nimbusaws.annotation.annotations.nativeimage.RegisterForReflection;

import java.util.LinkedList;
import java.util.List;

@RegisterForReflection
public class People {

    private List<NestedPerson> people = new LinkedList<>();

    public List<NestedPerson> getPeople() {
        return people;
    }

    public void setPeople(List<NestedPerson> people) {
        this.people = people;
    }

}
