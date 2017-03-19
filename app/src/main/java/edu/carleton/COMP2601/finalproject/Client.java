package edu.carleton.COMP2601.finalproject;

import java.io.Serializable;

/**
 * Created by AveryVine on 2017-03-19.
 */

public class Client implements Serializable {

    int cash;

    public Client() {
        cash = 0;
    }

    public void depositCash(int deposit) {
        cash += deposit;
    }

    public int getCash() {
        return cash;
    }
}
