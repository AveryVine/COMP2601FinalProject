package edu.carleton.COMP2601.finalproject;

/**
 * Created by AveryVine on 2017-03-15.
 */

public interface EventStream extends EventInputStream, EventOutputStream {
    public void close();
}
