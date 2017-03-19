package edu.carleton.COMP2601.finalproject;

import java.io.Serializable;

/**
 * Created by AveryVine on 2017-03-15.
 */

public class Header implements Serializable {
    private static final long serialVersionUID = -7729816603167728273L;
    public String id;	// Identity of sender; e.g., Bob
    public String recipient; // Identity of recipient; e.g. Joe
    public long seqNo;	// Sequence number for message
    public String retId;	// Return identity for routing
    public String type;		// Type of message (for reactor usage)

    public Header() {
        id = Fields.DEFAULT;
        recipient = Fields.DEFAULT;
        retId = Fields.DEFAULT;
        type = Fields.NO_ID;
        seqNo = Fields.DEFAULT_SEQ_ID;
    }

    public Header(String type) {
        id = Fields.DEFAULT;
        recipient = Fields.DEFAULT;
        retId = Fields.DEFAULT;
        this.type = type;
        seqNo = Fields.DEFAULT_SEQ_ID;
    }
}
