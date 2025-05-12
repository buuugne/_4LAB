package model.abstractions;

public abstract class AbstractPerson {
    protected String id;
    protected String name;

    public AbstractPerson(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public abstract String getRole();  // Abstraktus metodas, kurį įgyvendins paveldėtos klasės
}