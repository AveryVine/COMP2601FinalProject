package edu.carleton.COMP2601.finalproject;

import android.location.Location;

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
        address = "192.168.0.14";
        port = 7000;
        room = Fields.DEFAULT;
    }



    public void connect(final String username) {
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
            twr.register("USERS", new EventHandler() {
                @Override
                public void handleEvent(Event event) {
                    System.out.println("Received USERS");
                    ArrayList<String> listOfUsers = (ArrayList<String>) event.get(Fields.BODY);
                    String activity = (String) event.get(Fields.ACTIVITY);
                    if (activity.equals("RoomActivity")) {
                        RoomActivity.getInstance().users(listOfUsers);
                    }
                    else if (activity.equals("SendPhotoActivity")){
                        listOfUsers.remove(username);
                        SendPhotoActivity.getInstance().users(listOfUsers);
                    }
                    else if (activity.equals("DeployUavActivity")) {
                        DeployUavActivity.getInstance().updateUserList(listOfUsers);
                    }
                    else if (activity.equals("UavRegionActivity")) {
                        UavRegionActivity.getInstance().updateUserList(listOfUsers);
                    }
                }
            });
            twr.register("GET_LOCATION", new EventHandler() {
                @Override
                public void handleEvent(Event event) {
                    String id = (String) event.get(Fields.ID);
                    String activity = (String) event.get(Fields.ACTIVITY);
                    GameActivity.getInstance().sendClientLocation(id, activity);
                }
            });
            twr.register("SEND_LOCATION", new EventHandler() {
                @Override
                public void handleEvent(Event event) {
                    String id = (String) event.get(Fields.ID);
                    byte[] bytes = (byte[]) event.get(Fields.BODY);
                    String activity = (String) event.get(Fields.ACTIVITY);
                    if (activity.equals("DeployUavActivity")) {
                        DeployUavActivity.getInstance().showLocation(bytes, id);
                    }
                    else if (activity.equals("UavRegionActivity")) {
                        UavRegionActivity.getInstance().showLocation(bytes, id);
                    }
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
            twr.register("PHOTO_EVENT", new EventHandler() {
                @Override
                public void handleEvent(Event event) {
                    System.out.println("Received PHOTO_EVENT");
                    String sender = (String) event.get(Fields.ID);
                    byte[] bytes = (byte[]) event.get(Fields.BODY);
                    GameActivity.getInstance().photoReceived(sender, bytes);
                }
            });
            twr.register("KILL_CONFIRMED", new EventHandler() {
                @Override
                public void handleEvent(Event event) {
                    System.out.println("Received KILL_CONFIRMED");
                    String sender = (String) event.get(Fields.ID);
                    byte[] bytes = (byte[]) event.get(Fields.BODY);
                    GameActivity.getInstance().photoResponseReceived(sender, bytes, true);
                }
            });
            twr.register("TARGET_ESCAPED", new EventHandler() {
                @Override
                public void handleEvent(Event event) {
                    System.out.println("Received TARGET_ESCAPED");
                    String sender = (String) event.get(Fields.ID);
                    byte[] bytes = (byte[]) event.get(Fields.BODY);
                    GameActivity.getInstance().photoResponseReceived(sender, bytes, false);
                }
            });
            twr.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    public void request(final Event event) {
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
                if (event.type.equals("LOAD_ROOM")) {
                    room = (String) event.get(Fields.BODY);
                    event.type = "GET_USERS";
                }

                event.assignEventStream(es);
                event.put(Fields.ID, username);
                event.put(Fields.ROOM, room);

                try {
                    System.out.println("Request: sending event");
                    es.putEvent(event);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (event.type.equals("LEAVE_ROOM"))
                    room = Fields.DEFAULT;
//            }
//        }).start();
    }



    public static EventReactor getInstance() { return instance; }
}
