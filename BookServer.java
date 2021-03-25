import java.io.IOException;
import java.io.PrintWriter;
import java.net.*;
import java.util.Scanner;

class UDPServerThread extends Thread{
  DatagramPacket datapacket, returnpacket;
  DatagramSocket datasocket;
  UDPServerThread(DatagramPacket dp, DatagramSocket dS) {
    datasocket = dS;
    datapacket = dp;
  }
  @Override
  public void run(){
    byte[] buffer = new byte[0];
    String command = new String(datapacket.getData());
    buffer = command.getBytes();
    String[] tokens = command.split(" ");
//    if (tokens[0].equals("setmode")) {
//      // TODO: set the mode of communication for sending commands to the server
//    } else if (tokens[0].equals("borrow")) {
//      // TODO: send appropriate command to the server and display the
//      // appropriate responses form the server
//    } else if (tokens[0].equals("return")) {
//      // TODO: send appropriate command to the server and display the
//      // appropriate responses form the server
//    } else if (tokens[0].equals("inventory")) {
//      // TODO: send appropriate command to the server and display the
//      // appropriate responses form the server
//    } else if (tokens[0].equals("list")) {
//      // TODO: send appropriate command to the server and display the
//      // appropriate responses form the server
//    } else if (tokens[0].equals("exit")) {
//      // TODO: send appropriate command to the server
//    } else {
//      System.out.println("ERROR: No such command");
//    }
    DatagramPacket returnpacket = new DatagramPacket(
            buffer,
            buffer.length,
            datapacket.getAddress(),
            datapacket.getPort());
    try {
      datasocket.send(returnpacket);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
class UDPHandler extends Thread{
  DatagramPacket datapacket, returnpacket;
  DatagramSocket datasocket;
  int len = 10000;
  byte[] buf;
  int portNum;
  UDPHandler(int port) {
    portNum = port;
  }

  @Override
  public void run(){
    DatagramSocket datasocket = null;
    try {
      datasocket = new DatagramSocket(portNum);
    } catch (SocketException e) {
      e.printStackTrace();
    }
    while (true) {
      try {
        byte[] buf = new byte[len];
        datapacket = new DatagramPacket(buf, buf.length);
        datasocket.receive(datapacket);
        Thread t = new UDPServerThread(datapacket, datasocket);
        t.start();

      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }
}


class TCPServerThread extends Thread{
  Socket theClient;
  TCPServerThread(Socket s){
    theClient = s;
  }
  @Override
  public void run() {
    try {
      Scanner sc = new Scanner(theClient.getInputStream());
      PrintWriter pout = new PrintWriter(theClient.getOutputStream(), true);
      while (sc.hasNextLine()) {
        String cmd = sc.nextLine();
        String[] tokens = cmd.split(" ");

        if (tokens[0].equals("setmode")) {
          // TODO: set the mode of communication for sending commands to the server
        }
        else if (tokens[0].equals("borrow")) {
          // TODO: send appropriate command to the server and display the
          // appropriate responses form the server
        } else if (tokens[0].equals("return")) {
          // TODO: send appropriate command to the server and display the
          // appropriate responses form the server
        } else if (tokens[0].equals("inventory")) {
          // TODO: send appropriate command to the server and display the
          // appropriate responses form the server
        } else if (tokens[0].equals("list")) {
          // TODO: send appropriate command to the server and display the
          // appropriate responses form the server
        } else if (tokens[0].equals("exit")) {
          // TODO: send appropriate command to the server
        } else {
          System.out.println("ERROR: No such command");
        }
        pout.println(cmd);
      }

    }catch (IOException e) {
      e.printStackTrace();
    }

  }
}

class TCPHandler extends Thread{
  int port;
  public TCPHandler(int tcpPort) {
    port = tcpPort;
  }
  @Override
  public void run() {
    try {
      ServerSocket listener = new ServerSocket(port);
      Socket s;
      System.out.println("waiting to connect ");
      while ( (s = listener.accept()) != null) {
        Thread t = new TCPServerThread(s);
        t.start();
      }
    } catch (IOException e) {
      System.err.println("Server aborted:" + e);
    }
  }
}




public class BookServer {
  public static void main (String[] args) {
    int tcpPort;
    int udpPort;
    if (args.length != 1) {
      System.out.println("ERROR: Provide 1 argument: input file containing initial inventory");
      System.exit(-1);
    }
    String fileName = args[0];
    tcpPort = 7000;
    udpPort = 8000;

    // parse the inventory file
    Thread tCP = new TCPHandler(tcpPort);
    Thread uDP = new UDPHandler(udpPort);
    tCP.start();
    uDP.start();


    // TODO: handle request from clients
  }
}

public class BookInventory {
  Set<Book> inventory;
  int rid;
  HashMap<String, ArrayList<Book>> studentList;

  public class Book {
      String name;
      int quantity;
      int id;

      Book(String n, int amount) {
        name = n;
        quantity = amount;
        id = -1;
      }
  }

  BookInventory() {
    inventory = new HashSet<Book>();
    rid = 0;
    studentList = new HashMap<>();
    // Change to hashmap.
  }


  public void addBook(String bookname, int amount) {
    Book newbook = new Book(bookname, amount);
    inventory.add(newbook);
  }


  /*
  Borrow command returns true if book is available to borrow, thus set to client.
  Else returns false
   */
  public synchronized String borrow(String bookname, String client) {
    String s = "";
  for(Book b : inventory) {
    if((b.name).equals(bookname)) {
      if (b.quantity == 0) {
        s = "Request Failed - Book not available";
        return s;
      }
      if(studentList.containsKey(client)) {
        //Insert book into client's invent
        ArrayList<Book> temp = studentList.get(client);
        Book bb = b;
        rid++;
        b--;
        bb.id = rid;
        temp.add(bb);
        studentList.replace(client,temp);
      } else {
        ArrayList<Book> temp = new ArrayList<Book>();
        Book bb = b;
        rid++;
        b--;
        bb.id = rid;
        temp.add(bb);
        studentList.put(client, temp);
      }
      s = "Your request has been approved, " + rid.toString();
      return s;
    }
  }
  s = "Request Failed - We do not have this book";
  return s;
  }
  /*
Return command returns recordid of book associated with the passed id.
 */
  public synchronized String returning(int recordid) {
    //Access book here...
    String s = "";

    // Use record class.
    s = recordid + " is returned";
    // else return
    s = recordid + " not found";
    return s;
  }
  /*
Lists all books borrowed by student.
Returns message if no record.
 */
  public synchronized String list(String client) {
    String s = "";
    ArrayList<Book> temp = studentList.get(client);
    for(int i = 0; i <temp.length(); i++) {
      s += temp.get(i).name + " " + temp.get(i).record;
    }
    if(s == "") {
      "No record found for " + client;
    }
    // Concatenate into some long string


    return s;
  }

  /*
Lists All Books in inventory.
*/
  public synchronized String getInventory() {
    String s = "";
    for(Book b : inventory) {
      System.out.println(b.name + " " + b.record);
      // change to add to s
    }
    return s;
  }



}