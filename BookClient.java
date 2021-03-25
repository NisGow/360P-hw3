import java.net.*;
import java.util.Scanner;
import java.io.*;
import java.util.*;
public class BookClient {
  public static void main (String[] args) {
    Scanner din = null;
    PrintStream pout = null;
    Socket server = null;
    DatagramSocket datasocket = null;
    Scanner udpDin = null;
    InetAddress ia = null;
    DatagramPacket sPacket = null;
    DatagramPacket rPacket = null;
    byte[] rbuffer = new byte[10000];
    String hostAddress;
    Boolean tcp = false;
    int tcpPort;
    int udpPort;
    int clientId;

    if (args.length != 2) {
      System.out.println("ERROR: Provide 2 arguments: commandFile, clientId");
      System.out.println("\t(1) <command-file>: file with commands to the server");
      System.out.println("\t(2) client id: an integer between 1..9");
      System.exit(-1);
    }


    String commandFile = args[0];
    clientId = Integer.parseInt(args[1]);
    hostAddress = "localhost";
    tcpPort = 7000;// hardcoded -- must match the server's tcp port
    udpPort = 8000;// hardcoded -- must match the server's udp port
    Boolean outFileExists = false;
    String outputFile= "";
    String invenFile = "inventory.txt";
    try {
      outputFile = "out_" + Integer.toString(clientId) + ".txt";
      File myObj = new File(outputFile);
      File invenObj = new File(invenFile);
      if (myObj.createNewFile()) {
        System.out.println("File created: " + myObj.getName());
      }
      if (invenObj.createNewFile()) {
        System.out.println("File created: " + myObj.getName());
      }
    } catch (IOException e) {
      System.out.println("An error occurred.");
      e.printStackTrace();
    }
    try {
        PrintWriter output = new PrintWriter(new FileWriter((outputFile)),true);
        PrintWriter Invenoutput = new PrintWriter(new FileWriter((invenFile)),true);
        Scanner sc = new Scanner(new FileReader(commandFile));
        String retstring= "";
        while(sc.hasNextLine()) {
          String cmd = sc.nextLine();
          String[] tokens = cmd.split(" ");
          if (tokens[0].equals("setmode")) {
            if(tokens[1].equals("T")){
              tcp = true;
              server = new Socket(hostAddress, tcpPort);
              din = new Scanner(server.getInputStream());
              pout = new PrintStream(server.getOutputStream(),true);
              pout.println("setmode T");
              retstring = (din.nextLine());
            }else{
              if(tcp){
                tcp = false;
                server.close();
              }
              ia = InetAddress.getByName(hostAddress);
              datasocket = new DatagramSocket();
              byte[] buffer = cmd.getBytes();
              sPacket = new DatagramPacket(buffer, buffer.length, ia, udpPort);
              datasocket.send(sPacket);
              rPacket = new DatagramPacket(rbuffer, rbuffer.length);
              datasocket.receive(rPacket);
               retstring = new String(rPacket.getData(), 0,
                      rPacket.getLength());
              System.out.println("Received from Server:" + retstring);
            }
            output.println(retstring);

            // TODO: set the mode of communication for sending commands to the server 
          }
          else if (tokens[0].equals("borrow")) {
            if(tcp){
              pout.println(cmd);
              retstring = (din.nextLine());
            }else{
              byte[] buffer = cmd.getBytes();
              sPacket = new DatagramPacket(buffer, buffer.length, ia, udpPort);
              datasocket.send(sPacket);
              rPacket = new DatagramPacket(rbuffer, rbuffer.length);
              datasocket.receive(rPacket);
               retstring = new String(rPacket.getData(), 0,
                      rPacket.getLength());
              System.out.println("Received from Server:" + retstring);
            }
            output.println(retstring);
            // TODO: send appropriate command to the server and display the
            // appropriate responses form the server
          } else if (tokens[0].equals("return")) {
            if(tcp){
              pout.println(cmd);
              retstring =(din.nextLine());
            }else{
              byte[] buffer = cmd.getBytes();
              sPacket = new DatagramPacket(buffer, buffer.length, ia, udpPort);
              datasocket.send(sPacket);
              rPacket = new DatagramPacket(rbuffer, rbuffer.length);
              datasocket.receive(rPacket);
               retstring = new String(rPacket.getData(), 0,
                      rPacket.getLength());
              System.out.println("Received from Server:" + retstring);
            }
            output.println(retstring);
            // TODO: send appropriate command to the server and display the
            // appropriate responses form the server
          } else if (tokens[0].equals("inventory")) {
            if(tcp){
              pout.println(cmd);
              retstring = din.nextLine();
            }else{
              byte[] buffer = cmd.getBytes();
              sPacket = new DatagramPacket(buffer, buffer.length, ia, udpPort);
              datasocket.send(sPacket);
              rPacket = new DatagramPacket(rbuffer, rbuffer.length);
              datasocket.receive(rPacket);
              retstring = new String(rPacket.getData(), 0,
                      rPacket.getLength());
              System.out.println("Received from Server:" + retstring);
            }
            retstring = retstring.replace("$", "\n");
            output.println(retstring);
            // TODO: send appropriate command to the server and display the
            // appropriate responses form the server
          } else if (tokens[0].equals("list")) {
            if(tcp){
              pout.println(cmd);
               retstring = (din.nextLine());
            }else{
              byte[] buffer = cmd.getBytes();
              sPacket = new DatagramPacket(buffer, buffer.length, ia, udpPort);
              datasocket.send(sPacket);
              rPacket = new DatagramPacket(rbuffer, rbuffer.length);
              datasocket.receive(rPacket);
              retstring = new String(rPacket.getData(), 0,
                      rPacket.getLength());
              System.out.println("Received from Server:" + retstring);
            }
            retstring = retstring.replace("$", "\n");
            output.print(retstring);
            // TODO: send appropriate command to the server and display the
            // appropriate responses form the server
          } else if (tokens[0].equals("exit")) {
            if(tcp){
              pout.println(cmd);
              retstring = (din.nextLine());
            }else{
              byte[] buffer = cmd.getBytes();
              sPacket = new DatagramPacket(buffer, buffer.length, ia, udpPort);
              datasocket.send(sPacket);
              rPacket = new DatagramPacket(rbuffer, rbuffer.length);
              datasocket.receive(rPacket);
               retstring = new String(rPacket.getData(), 0,
                      rPacket.getLength());
              System.out.println("Received from Server:" + retstring);
            }
            retstring = retstring.replace("$", "\n");
            Invenoutput.println(retstring);
            // TODO: send appropriate command to the server 
          } else {
            System.out.println("ERROR: No such command");
          }
        }
    } catch (FileNotFoundException e) {
	e.printStackTrace();
    } catch (UnknownHostException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
