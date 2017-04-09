package edu.carleton.COMP2601.finalproject;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by AveryVine on 2017-03-15.
 */

public class Server {
    public static int PORT;

    private ServerSocket listener;
    private Socket s;
    private Reactor r;
    private EventStreamImpl es;
    private ThreadWithReactor twr;
    private ConcurrentHashMap<String, ThreadWithReactor> clients;
    private ConcurrentHashMap<String, ConcurrentHashMap<String, ConcurrentHashMap<String, String>>> rooms;
        /*
        ConcurrentHashMap1 <RoomNames, ConcurrentHashMap2>
            ConcurrentHashMap2 <OccupantNames, ConcurrentHashMap3>
                ConcurrentHashMap3 <AttributeNames, AttributeValues>
         */
    private int numberOfRooms;



    public Server() {
        PORT = 7000;
        numberOfRooms = 4;

        r = new Reactor();
        clients = new ConcurrentHashMap<>();
        rooms = new ConcurrentHashMap<>();
        generateRoomNames();
    }



    public void init() {
        r.register("CONNECT_REQUEST", new EventHandler() {
            @Override
            public void handleEvent(Event event) {
                String id = (String) event.get(Fields.ID);
                System.out.println("Server: " + id + " connected");

                ThreadWithReactor twr = (ThreadWithReactor) Thread.currentThread();
                clients.put(id, twr);

                event = new Event("CONNECTED_RESPONSE");
                sendEvent(event, clients.get(id));
                sendRoomList();
            }
        });
        r.register("DISCONNECT_REQUEST", new EventHandler() {
            @Override
            public void handleEvent(Event event) {
                String id = (String) event.get(Fields.ID);
                System.out.println("Server: " + id + " disconnected");

                clients.remove(id);
            }
        });
        r.register("GET_USERS", new EventHandler() {
            @Override
            public void handleEvent(Event event) {
                String id = (String) event.get(Fields.ID);
                String roomName = (String) event.get(Fields.ROOM);
                String activity = (String) event.get(Fields.ACTIVITY);
                System.out.println(roomName + ": " + id + " connected");

                Set<String> recipients;
                if (activity.equals("RoomActivity")) {
                    recipients = rooms.get(roomName).keySet();
                    rooms.get(roomName).put(id, new ConcurrentHashMap<String, String>());
                }
                else {
                    recipients = new HashSet<>();
                    recipients.add(id);
                }
                sendRoomOccupantList(roomName, activity, recipients);
            }
        });
        r.register("LEAVE_ROOM", new EventHandler() {
            @Override
            public void handleEvent(Event event) {
                String id = (String) event.get(Fields.ID);
                String roomName = (String) event.get(Fields.BODY);
                System.out.println(roomName + ": " + id + " disconnected");

                rooms.get(roomName).remove(id);
                sendRoomOccupantList(roomName, "RoomActivity", rooms.get(roomName).keySet());
            }
        });
        r.register("START_GAME_REQUEST", new EventHandler() {
            @Override
            public void handleEvent(Event event) {
                String id = (String) event.get(Fields.ID);
                String roomName = (String) event.get(Fields.ROOM);
                System.out.println(roomName + ": " + id + " started the game");

                event = new Event("START_GAME");
                broadcastEvent(event, rooms.get(roomName).keySet());
            }
        });
        r.register("CONNECTED_TO_GAME", new EventHandler() {
            @Override
            public void handleEvent(Event event) {
                String id = (String) event.get(Fields.ID);
                String roomName = (String) event.get(Fields.ROOM);
                System.out.println(roomName + ": " + id + " connected to the game");

                event = new Event("CASH_DEPOSIT");
                event.put(Fields.BODY, 1000);
                sendEvent(event, clients.get(id));
            }
        });

        try {
            listener = new ServerSocket(PORT);
            run();
            s.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    public void passEventToRecipient(Event event) {
        ThreadWithReactor twr = clients.get(event.get(Fields.RECIPIENT));
        EventStream es = twr.getEventSource();
        try {
            es.putEvent(event);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    public void sendRoomList() {
        Event event = new Event("ROOM_LIST");
        ArrayList<String> listOfRooms = new ArrayList<>();
        listOfRooms.addAll(rooms.keySet());
        event.put(Fields.BODY, listOfRooms);
        broadcastEvent(event, clients.keySet());
    }



    public void sendRoomOccupantList(String roomName, String activity, Set<String> recipients) {
        Event event = new Event("USERS");
        event.put(Fields.ROOM, roomName);
        event.put(Fields.ACTIVITY, activity);
        ArrayList<String> listOfUsers = new ArrayList<>();
        listOfUsers.addAll(rooms.get(roomName).keySet());
        event.put(Fields.BODY, listOfUsers);
        broadcastEvent(event, recipients);
    }



    public void broadcastEvent(Event event, Set<String> recipients) {
        for (String id : recipients) {
            event.put(Fields.ID, id);
            sendEvent(event, clients.get(id));
            twr = clients.get(id);
        }
    }



    public void sendEvent(Event event, ThreadWithReactor twr) {
        try {
            twr.getEventSource().putEvent(event);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    public void generateRoomNames() {
        Random random = new Random();
        String[] potentialRoomNames = {"Alfa", "Bravo", "Charlie", "Delta", "Echo", "Foxtrot",
                "Golf", "Hotel", "India", "Juliett", "Kilo", "Lima", "Mike", "November", "Oscar",
                "Papa", "Quebec", "Romeo", "Sierra", "Tango", "Uniform", "Victor", "Whiskey",
                "Xray", "Yankee", "Zulu"};
        if (numberOfRooms > potentialRoomNames.length)
            numberOfRooms = potentialRoomNames.length;
        for (int i = 0; i < numberOfRooms; i++) {
            boolean taken = false;
            int roomNameIndex = random.nextInt(potentialRoomNames.length);
            for (String key : rooms.keySet()) {
                if (key.equals(potentialRoomNames[roomNameIndex])) {
                    taken = true;
                    break;
                }
            }
            if (!taken)
                rooms.put(potentialRoomNames[roomNameIndex] + " Room", new ConcurrentHashMap<String, ConcurrentHashMap<String, String>>());
        }
    }



    public void run() {
        try {
            System.out.println("Server is listening on port " + PORT);
            while (true) {
                s = listener.accept();
                es = new EventStreamImpl(s);
                twr = new ThreadWithReactor(es, r);
                twr.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    public static void main(String[] args) {
        Server ns = new Server();
        ns.init();
    }
}
