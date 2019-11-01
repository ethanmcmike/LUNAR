package ethanmcmike.ndb.models;

public class Event {


    public String name;
    private int id;
    public boolean complete;

    public Event(String name, int id) {
        this.name = name;
        this.id = id;
    }
}
