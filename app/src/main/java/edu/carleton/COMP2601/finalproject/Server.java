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
    private ArrayList<String> disabledRooms;
    private ConcurrentHashMap<String, ConcurrentHashMap<String, ConcurrentHashMap<String, String>>> rooms;
        /*
        ConcurrentHashMap1 <RoomNames, ConcurrentHashMap2>
            ConcurrentHashMap2 <OccupantNames, ConcurrentHashMap3>
                ConcurrentHashMap3 <AttributeNames, AttributeValues>
         */
    private int numberOfRooms;

    /*----------
    - Description: Constructor initializes port, number of rooms, reactor,
                   clients/rooms concurrenthashmap and generates room names.
    ----------*/
    public Server() {
        PORT = 7000;
        numberOfRooms = 4;

        r = new Reactor();
        clients = new ConcurrentHashMap<>();
        disabledRooms = new ArrayList<>();
        rooms = new ConcurrentHashMap<>();
        generateRoomNames();
    }

    /*----------
    - Description: initializes the reactor
    - Input: none
    - Return: none
    ----------*/
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
                System.out.println("Room: " + rooms.get(roomName).keySet());
                String activity = (String) event.get(Fields.ACTIVITY);
                System.out.println(roomName + ": " + id + " requested users");

                Set<String> recipients;
                if (activity.equals("RoomActivity")) {
                    recipients = rooms.get(roomName).keySet();
                    rooms.get(roomName).put(id, new ConcurrentHashMap<String, String>());
                }
                else {
                    recipients = new HashSet<>();
                    System.out.println("Sending back the user list to recipient: " + id);
                    recipients.add(id);
                }
                sendRoomOccupantList(roomName, activity, recipients);
            }
        });
        r.register("GET_ROOMS", new EventHandler() {
            @Override
            public void handleEvent(Event event) {
                sendRoomList();
            }
        });
        r.register("GET_LOCATION", new EventHandler() {
            @Override
            public void handleEvent(Event event) {
                String id = (String) event.get(Fields.ID);
                String roomName = (String) event.get(Fields.ROOM);
                System.out.println("Room: " + rooms.get(roomName).keySet());
                String recipient = (String) event.get(Fields.RECIPIENT);

                System.out.println(roomName + ": " + id + " requested location information from " + recipient);
                passEventToRecipient(event);
            }
        });
        r.register("SEND_LOCATION", new EventHandler() {
            @Override
            public void handleEvent(Event event) {
                String id = (String) event.get(Fields.ID);
                String roomName = (String) event.get(Fields.ROOM);
                System.out.println("Room: " + rooms.get(roomName).keySet());
                String recipient = (String) event.get(Fields.RECIPIENT);

                System.out.println(roomName + ": " + id + " is sending location information to " + recipient);
                passEventToRecipient(event);
            }
        });
        r.register("LEAVE_ROOM", new EventHandler() {
            @Override
            public void handleEvent(Event event) {
                String id = (String) event.get(Fields.ID);
                String roomName = (String) event.get(Fields.BODY);
                System.out.println(roomName + ": " + id + " disconnected");

                rooms.get(roomName).remove(id);
                if (rooms.get(roomName).size() < 2 && disabledRooms.contains(roomName)) {
                    event = new Event("YOU_WIN");
                    disabledRooms.remove(roomName);
                    broadcastEvent(event, rooms.get(roomName).keySet());
                    sendRoomList();
                }
                sendRoomOccupantList(roomName, "RoomActivity", rooms.get(roomName).keySet());
            }
        });
        r.register("START_GAME_REQUEST", new EventHandler() {
            @Override
            public void handleEvent(Event event) {
                String id = (String) event.get(Fields.ID);
                String roomName = (String) event.get(Fields.ROOM);
                System.out.println("Room: " + rooms.get(roomName).keySet());
                System.out.println(roomName + ": " + id + " started the game");

                disabledRooms.add(roomName);
                event = new Event("START_GAME");
                broadcastEvent(event, rooms.get(roomName).keySet());
            }
        });
        r.register("CONNECTED_TO_GAME", new EventHandler() {
            @Override
            public void handleEvent(Event event) {
                String id = (String) event.get(Fields.ID);
                String roomName = (String) event.get(Fields.ROOM);
                System.out.println("Room: " + rooms.get(roomName).keySet());
                System.out.println(roomName + ": " + id + " connected to the game");

                event = new Event("CASH_DEPOSIT");
                event.put(Fields.BODY, 1000);
                sendEvent(event, clients.get(id));
            }
        });
        r.register("PHOTO_EVENT", new EventHandler() {
            @Override
            public void handleEvent(Event event) {
                String id = (String) event.get(Fields.ID);
                String roomName = (String) event.get(Fields.ROOM);
                System.out.println("Room: " + rooms.get(roomName).keySet());
                String recipient = (String) event.get(Fields.RECIPIENT);
                System.out.println(roomName + ": " + id + " is sending a photo to " + recipient);

                passEventToRecipient(event);
            }
        });
        r.register("KILL_CONFIRMED", new EventHandler() {
            @Override
            public void handleEvent(Event event) {
                String id = (String) event.get(Fields.ID);
                String roomName = (String) event.get(Fields.ROOM);
                System.out.println("Room: " + rooms.get(roomName).keySet());
                String recipient = (String) event.get(Fields.RECIPIENT);
                System.out.println(roomName + ": " + id + " is sending a confirmed kill response to " + recipient);

                Set<String> recipients = rooms.get(roomName).keySet();
                recipients.remove(id);
                broadcastEvent(event, recipients);
            }
        });
        r.register("TARGET_ESCAPED", new EventHandler() {
            @Override
            public void handleEvent(Event event) {
                String id = (String) event.get(Fields.ID);
                String roomName = (String) event.get(Fields.ROOM);
                System.out.println("Room: " + rooms.get(roomName).keySet());
                String recipient = (String) event.get(Fields.RECIPIENT);
                System.out.println(roomName + ": " + id + " is sending a target escaped response to " + recipient);

                passEventToRecipient(event);
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


    /*----------
    - Description: directly passes the event to the recipient
    - Input: the event to be passed
    - Return: none
    ----------*/
    public void passEventToRecipient(Event event) {
        ThreadWithReactor twr = clients.get(event.get(Fields.RECIPIENT));
        EventStream es = twr.getEventSource();
        try {
            es.putEvent(event);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*----------
    - Description: Creates a new ROOM_LIST event, adds listOfRooms to it and broadcasts the event
    - Input: none
    - Return: none
    ----------*/
    public void sendRoomList() {
        Event event = new Event("ROOM_LIST");
        ArrayList<String> listOfRooms = new ArrayList<>();
        listOfRooms.addAll(rooms.keySet());
        System.out.println("Disabled: " + disabledRooms);
        for (String disabledRoom: disabledRooms) {
            listOfRooms.remove(disabledRoom);
        }
        System.out.println("Final: " + listOfRooms);
        event.put(Fields.BODY, listOfRooms);
        broadcastEvent(event, clients.keySet());
    }

    /*----------
    - Description: Creates a new USERS event, adds rooms, activity name and list of users to the event.
                   Then broadcasts the event.
    - Input: roomName, activity, recipients
    - Return: none
    ----------*/
    public void sendRoomOccupantList(String roomName, String activity, Set<String> recipients) {
        Event event = new Event("USERS");
        event.put(Fields.ROOM, roomName);
        event.put(Fields.ACTIVITY, activity);
        ArrayList<String> listOfUsers = new ArrayList<>();
        listOfUsers.addAll(rooms.get(roomName).keySet());
        event.put(Fields.BODY, listOfUsers);
        broadcastEvent(event, recipients);
    }

    /*----------
    - Description: Sends the event to every single user in recipients
    - Input: event, recipients
    - Return: none
    ----------*/
    public void broadcastEvent(Event event, Set<String> recipients) {
        for (String id : recipients) {
            event.put(Fields.ID, id);
            sendEvent(event, clients.get(id));
            twr = clients.get(id);
        }
    }

    /*----------
    - Description: Sends the event back to the client
    - Input: event, twr
    - Return: none
    ----------*/
    public void sendEvent(Event event, ThreadWithReactor twr) {
        try {
            System.out.println("Sending event");
            twr.getEventSource().putEvent(event);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*----------
    - Description: Generates a random list of room names
    - Input: none
    - Return: none
    ----------*/
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

    /*----------
    - Description: runs the server loop, listens for connections
    - Input: none
    - Return: none
    ----------*/
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

    /*----------
    - Description: starts the server
    ----------*/
    public static void main(String[] args) {
        Server ns = new Server();
        ns.init();
    }
}
