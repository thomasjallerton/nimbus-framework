package models;

import com.nimbusframework.nimbusaws.annotation.annotations.nativeimage.RegisterForReflection;

import java.util.LinkedList;
import java.util.List;

public class RegisterNestedClass {

    private String unrelated;

    @RegisterForReflection
    class RegisterNestedClassIdentifier {

        private String identifier = "";
        private int count = 0;

        public String getIdentifier() {
            return identifier;
        }

        public void setIdentifier(String identifier) {
            this.identifier = identifier;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }
    }

}
