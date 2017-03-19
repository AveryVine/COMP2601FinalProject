package edu.carleton.COMP2601.finalproject;

import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by AveryVine on 2017-03-17.
 */

public class EventReactor {
    private static EventReactor instance;

    private Socket s;
    private String username, address, room;
    private EventStreamImpl es;
    private int port;



    public EventReactor() {
        instance = this;
        address = "192.168.0.26";
        port = 7000;
        room = Fields.DEFAULT;
    }



    public void connect(String username) {
        this.username = username;
        try {
            s = new Socket(address, port);
            System.out.println("Connected (address " + address + ", port " + port + ")");
            es = new EventStreamImpl(s.getOutputStream(), s.getInputStream());
            ThreadWithReactor twr = new ThreadWithReactor(es);
            twr.register("CONNECTED_RESPONSE", new EventHandler() {
                @Override
                public void handleEvent(Event event) {
                    System.out.println("Received CONNECTED_RESPONSE");
                    MainActivity.getInstance().connectedResponse();
                }
            });
            twr.register("ROOM_LIST", new EventHandler() {
                @Override
                public void handleEvent(Event event) {
                    System.out.println("Received ROOM_LIST");
                    ArrayList<String> listOfRooms = (ArrayList<String>) event.get(Fields.BODY);
                    MainActivity.getInstance().roomList(listOfRooms);
                }
            });
            twr.register("ROOM_OCCUPANT_LIST", new EventHandler() {
                @Override
                public void handleEvent(Event event) {
                    System.out.println("Received ROOM_OCCUPANT_LIST");
                    ArrayList<String> listOfUsers = (ArrayList<String>) event.get(Fields.BODY);
                    RoomActivity.getInstance().occupantList(listOfUsers);
                }
            });
            twr.register("START_GAME", new EventHandler() {
                @Override
                public void handleEvent(Event event) {
                    System.out.println("Received START_GAME");
                    RoomActivity.getInstance().startGame();
                }
            });
            twr.register("CASH_DEPOSIT", new EventHandler() {
                @Override
                public void handleEvent(Event event) {
                    System.out.println("Received CASH_DEPOSIT");
                    int cashDeposit = (int) event.get(Fields.BODY);
                    GameActivity.getInstance().cashDeposit(cashDeposit);
                }
            });
            twr.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    public void request(Event event) {
        if (event.type.equals("LOAD_ROOM"))
            room = (String) event.get(Fields.BODY);

        event.assignEventStream(es);
        event.put(Fields.ID, username);
        event.put(Fields.ROOM, room);

        try {
            es.putEvent(event);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (event.type.equals("LEAVE_ROOM"))
            room = Fields.DEFAULT;
    }



    public static EventReactor getInstance() { return instance; }
}
