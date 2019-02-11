package network;

import java.io.Serializable;

public class Person implements Serializable {
    private String name;
    private int age = 25;

    public Person(){
        this.name = "Pelle";
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }
}
