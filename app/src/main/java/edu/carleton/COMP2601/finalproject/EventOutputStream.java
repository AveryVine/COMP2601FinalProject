package edu.carleton.COMP2601.finalproject;

import java.io.IOException;

/**
 * Created by AveryVine on 2017-03-15.
 */

public interface EventOutputStream {
    public void putEvent(Event e) throws IOException, ClassNotFoundException;
}
