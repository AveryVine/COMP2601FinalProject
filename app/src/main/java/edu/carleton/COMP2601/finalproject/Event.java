package edu.carleton.COMP2601.finalproject;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;

/**
 * Created by AveryVine on 2017-03-15.
 */

public class Event implements EventStream, Serializable {
    /**
     * Class can be used to write itself or read/write other events from/to
     * an EventStream. The es attribute is made public so that events which
     * are read from an event stream are marked as having been obtained from
     * a particular stream. The es attribute is transient as it makes no
     * sense to share it when serializing the event.
     */
    private static final long serialVersionUID = 1L;
    public final String type;
    public transient EventStream es;
    private HashMap<String, Serializable> map;

    public Event(String type) {
        this.type = type;
        this.es = null;
        this.map = new HashMap<String, Serializable>();
    }

    public Event(String type, EventStream es) {
        this.type = type;
        this.es = es;
        this.map = new HashMap<String, Serializable>();
    }

    public Event(String type, EventStream es, HashMap<String, Serializable> map) {
        this.type = type;
        this.es = es;
        this.map = map;
    }

    public void assignEventStream(EventStream es) {
        this.es = es;
    }

    public void put(String key, Serializable value) {
        map.put(key, value);
    }

    public Serializable get(String key) {
        return map.get(key);
    }

    public void putEvent() throws ClassNotFoundException, IOException {
        putEvent(this);
    }

    public void putEvent(Event e) throws IOException, ClassNotFoundException {
        if (es != null)
            es.putEvent(e);
        else
            throw new IOException("No event stream defined");
    }

    public Event getEvent() throws ClassNotFoundException, IOException {
        if (es != null) {
            return es.getEvent();
        } else
            throw new IOException("No event stream defined");
    }

    public void close() {
        if (es != null) {
            es.close();
        }
    }
}
