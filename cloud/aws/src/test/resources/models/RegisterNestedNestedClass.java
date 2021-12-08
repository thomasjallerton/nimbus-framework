package models;

import com.nimbusframework.nimbusaws.annotation.annotations.nativeimage.RegisterForReflection;

public class RegisterNestedNestedClass {

    private String unrelated;

    class Nested {

        @RegisterForReflection
        class SuperNested {

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

}
