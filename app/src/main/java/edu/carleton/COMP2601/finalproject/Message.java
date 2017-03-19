package edu.carleton.COMP2601.finalproject;

import java.io.Serializable;

/**
 * Created by AveryVine on 2017-03-15.
 */

public class Message implements Serializable {
    private static final long serialVersionUID = 6394396411894185136L;
    public Header header;
    public Body body;

    public Message() {
        header = new Header();
        body = new Body();
    }

    public Message(String type) {
        header = new Header(type);
        body = new Body();
    }
}
