import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;



class UDPServerThread extends Thread{
  DatagramPacket datapacket, returnpacket;
  DatagramSocket datasocket;
  BookInventory bI;
  UDPServerThread(DatagramPacket dp, DatagramSocket dS, BookInventory bI) {
    datasocket = dS;
    datapacket = dp;
    this.bI = bI;
  }
  @Override
  public void run(){
    byte[] buffer = new byte[0];
    String command = new String(datapacket.getData());
    buffer = command.getBytes();
    String[] tokens = command.split(" ");
    if (tokens[0].equals("setmode")) {
      String response = "The communication mode is set to UDP";
      buffer = response.getBytes();
      // TODO: set the mode of communication for sending commands to the server
    } else if (tokens[0].equals("borrow")) {
      String stu = tokens[1];
      String[] arg = command.split("\"");
      String bName = arg[1];
      String response = bI.borrow(stu,bName);
      buffer = response.getBytes();
      // TODO: send appropriate command to the server and display the
      // appropriate responses form the server
    } else if (tokens[0].equals("return")) {
      String id = tokens[1];
      id = id.trim();
      Integer rId = Integer.parseInt(id);
      String response = bI.returning(rId);
      buffer = response.getBytes();
      // TODO: send appropriate command to the server and display the
      // appropriate responses form the server
    } else if (tokens[0].equals("inventory")) {
      String response = bI.getInventory();
      buffer = response.getBytes();
      // TODO: send appropriate command to the server and display the
      // appropriate responses form the server
    } else if (tokens[0].equals("list")) {
      String stu = tokens[1];
      String response = bI.list(stu);
      buffer = response.getBytes();
      // TODO: send appropriate command to the server and display the
      // appropriate responses form the server
    } else if (tokens[0].equals("exit")) {
      String response = bI.getInventory();
      buffer = response.getBytes();
      // TODO: send appropriate command to the server
    } else {
      System.out.println("ERROR: No such command");
    }
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
  BookInventory bI;
  UDPHandler(int port, BookInventory bI) {
    portNum = port;
    this.bI = bI;
  }

  @Override
  public void run(){
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
        Thread t = new UDPServerThread(datapacket, datasocket,bI);
        t.start();

      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }
}


class TCPServerThread extends Thread{
  Socket theClient;
  BookInventory bI;
  TCPServerThread(Socket s,BookInventory bI){
    theClient = s;
    this.bI = bI;
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
          pout.println("The communication mode is set to TCP");
          // TODO: set the mode of communication for sending commands to the server
        }
        else if (tokens[0].equals("borrow")) {
          String stu = tokens[1];
          String[] arg = cmd.split("\"");
          String bName = arg[1];
          String response = bI.borrow(bName,stu);
          pout.println(response);
          // TODO: send appropriate command to the server and display the
          // appropriate responses form the server
        } else if (tokens[0].equals("return")) {
          String id = tokens[1];
          id = id.trim();
          Integer rId = Integer.parseInt(id);
          String response = bI.returning(rId);
          pout.println(response);
          // TODO: send appropriate command to the server and display the
          // appropriate responses form the server
        } else if (tokens[0].equals("inventory")) {
          String response = bI.getInventory();
          pout.println(response);
          // TODO: send appropriate command to the server and display the
          // appropriate responses form the server
        } else if (tokens[0].equals("list")) {
          String stu = tokens[1];
          String response = bI.list(stu);
          pout.println(response);
          // TODO: send appropriate command to the server and display the
          // appropriate responses form the server
        } else if (tokens[0].equals("exit")) {
          String response = bI.getInventory();
          pout.println(response);
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
  BookInventory bI;
  int port;
  public TCPHandler(int tcpPort, BookInventory bI) {
    port = tcpPort;
    this.bI = bI;
  }
  @Override
  public void run() {
    try {
      ServerSocket listener = new ServerSocket(port);
      Socket s;
      System.out.println("waiting to connect ");
      while ( (s = listener.accept()) != null) {
        Thread t = new TCPServerThread(s, bI);
        t.start();
      }
    } catch (IOException e) {
      System.err.println("Server aborted:" + e);
    }
  }
}




public class BookServer {
  public static void main(String[] args) {
    BookInventory bI = new BookInventory();
    int tcpPort;
    int udpPort;
    if (args.length != 1) {
      System.out.println("ERROR: Provide 1 argument: input file containing initial inventory");
      System.exit(-1);
    }
    String fileName = args[0];
    tcpPort = 7000;
    udpPort = 8000;

    Scanner sc = null;
    try {
      sc = new Scanner(new FileReader(fileName));
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }

    while (sc.hasNextLine()) {
      String cmd = sc.nextLine();
      String[] bookName = cmd.split("\"");
      bI.addBook(bookName[1], Integer.parseInt(bookName[2].trim()));
    }

      // parse the inventory file
      Thread tCP = new TCPHandler(tcpPort, bI);
      Thread uDP = new UDPHandler(udpPort, bI);
      tCP.start();
      uDP.start();


      // TODO: handle request from clients
    }
  }


 class BookInventory {
  ArrayList<Book> inventory;
  //Set<Book> inventory;
  Integer rid;
  HashMap<String, ArrayList<Record>> studentList;
  HashMap<Integer, Record> recordList;
  public class Book {
      String name;
      int quantity;

      Book(String n, int amount) {
        name = n;
        quantity = amount;
      }
  }

  class Record {
    // class that holds information about borrowed books
    String bookName;
    String stuName;
    Integer recordID;
    Integer booklocation;
    Record(String name, String student, Integer id, Integer bookID){
      bookName = name;
      stuName = student;
      recordID = id;
      booklocation = bookID;
    }

  }

  BookInventory() {
    inventory = new ArrayList<>();
    recordList = new HashMap<>();
    //inventory = new HashSet<Book>();
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
      if (b.quantity <= 0) {
        s = "Request Failed - Book not available";
        return s;
      }
      Integer index = inventory.indexOf(b);

     // Record temp = new Record(book, studentName, RecordID, i);

      if(studentList.containsKey(client)) {
        //Insert book into client's invent
        //quantiy --;
        b.quantity--;
        inventory.set(index,b);
        rid++;
        ArrayList<Record> temp = studentList.get(client);
        Record tempR = new Record(b.name,client,rid,index);
        temp.add(tempR);
        studentList.replace(client,temp);
        recordList.put(rid, tempR);
      } else {
        b.quantity--;
        inventory.set(index,b);
        rid++;
        ArrayList<Record> temp = new ArrayList<Record>();
        Record tempR = new Record(b.name,client,rid,index);
        temp.add(tempR);
        studentList.put(client, temp);
        recordList.put(rid, tempR);
      }
      s = "Your request has been approved, " + rid.toString() + " " + client + " \"" + b.name + "\" ";
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
    if(recordList.containsKey(recordid)){
      Record rReturned = recordList.remove(recordid);
      Book b = inventory.get(rReturned.booklocation);
      b.quantity++;
      inventory.set(rReturned.booklocation, b);
      ArrayList<Record> temp = studentList.get(rReturned.stuName);
      temp.remove(rReturned);
      // Use record class.
      s = recordid + " is returned";
    }else{
      // else return
      s = recordid + " not found, no such borrow record";
    }
    return s;
  }
  /*
Lists all books borrowed by student.
Returns message if no record.
 */
  public synchronized String list(String client) {
    String s = "";
    if(studentList.containsKey(client)){
      ArrayList<Record> temp = studentList.get(client);
      for(int i = 0; i <temp.size(); i++) {
        s += temp.get(i).recordID.toString() + " \"" + temp.get(i).bookName + "\"" + "$";
      }
    }else{
      s = "No record found for " + client;
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
     String str = ("\"" + b.name + "\" " + b.quantity + "$");
      s = s + str;
      // change to add to s
    }
    return s;
  }

}
