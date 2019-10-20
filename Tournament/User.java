package Task1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class User {
	static Set<Player> players = new HashSet<Player>();
	static Map<Player,Game> games=new HashMap<Player,Game>();
	static String serverHostName;
	static Integer serverPort = 0;
	static Player player;
	static DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

	private static class Server implements Runnable { //server part,defining information about this player and running thread to interact with clients
		@Override
		public void run() {

			ServerSocket echoServer = null;
			Socket clientSocket = null;
			try {
				//System.out.println("Trying to create server socket");
				echoServer = new ServerSocket(0);
				serverHostName = InetAddress.getLocalHost().getHostAddress();
				serverPort = echoServer.getLocalPort();
				System.out.println(serverHostName + ":" + serverPort);
				//System.out.println("Socket created");
				player = new Player(serverHostName, serverPort);
				players.add(player);
			} catch (IOException e) {
				System.out.println(e);
			}


			while (true) {
				try {
					//System.out.println("Wait for accept");
					clientSocket = echoServer.accept();
					System.out.println("Client connected");
					//InetAddress address = clientSocket.getInetAddress();
					//int port = clientSocket.getPort();
					//System.out.println("from address " + address.toString() + ":" + port);
					(new Thread(new ClientThread(clientSocket))).start();
				} catch (IOException e) {
					System.out.println(e);
				}
			}
		}
	}

	private static class ClientThread implements Runnable {
		Socket clientSocket = null;
		String clientName = null;

		ClientThread(Socket socket) { //here described server part to communicate which responds on clients messages
			clientSocket = socket;
			clientName = clientSocket.getInetAddress().toString() + ":" + clientSocket.getPort();
		}

		@Override
		public void run() {
			BufferedReader in = null;
			PrintWriter out = null;

			try {
				in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				out = new PrintWriter(clientSocket.getOutputStream(), true);
				String line = in.readLine();

				//while ((line = in.readLine()) != null) {
				//System.out.println(clientName + ": readed: " + line);
				if (line.startsWith("JOIN")) {
					//System.out.println("got join");
					Player p = new Player(line.substring(5));
					if (p.equals(player)) {
						//System.out.println("send yourself");
						out.println("YOURSELF");

					} else {
						players.add(p);
						if(!p.equals(player)&&!games.containsKey(p)){
							games.put(p,new Game());
						}
					}
					out.println("PLAYERS:" + players);
				}
				else if(line.startsWith("PLAY:")){
					String opponent=line.substring(5).split("~")[0];
					int opponentNumber=Integer.parseInt(line.substring(5).split("~")[1]);
					Date opponentTime=df.parse(line.substring(5).split("~")[2]);
					Game game=games.get(new Player(opponent));
					if(game!=null){
						game.opponentGuess(opponentNumber,opponentTime);
					}
				}
				else if (line.startsWith("QUIT")) {
					//System.out.println("got quit");
					Player p = new Player(line.substring(5));
					if (p.equals(player)) {
						//System.out.println("send yourself");
						out.println("YOURSELF");

					} else {
						players.remove(p);
						if(!p.equals(player)&&!games.containsKey(p)){
							games.remove(p);
						}
					}
				}
				//System.out.println("Client " + clientName + " disconnected");
				in.close();
				out.close();
				clientSocket.close();
			} catch (IOException e) {
				System.out.println(e);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
	}

	static String sendJoin(String hostname,int port){
		Socket echoSocket = null;
		PrintWriter out = null;
		BufferedReader in = null;
		String line=null;
		try {
			//System.out.println("trying to create a socket");
			echoSocket = new Socket(hostname, port);
			//System.out.println("trying to create output stream");
			out = new PrintWriter(echoSocket.getOutputStream(), true);
			//System.out.println("trying to create input stream");
			in = new BufferedReader(new InputStreamReader(echoSocket.getInputStream()));
			out.println("JOIN:" + player);
			line = in.readLine();
			System.out.println(">>" + line);
			out.close();
			in.close();
			echoSocket.close();
		} catch (UnknownHostException e) {
			System.err.println("Unknown host: " + hostname + ".");
			System.exit(1);
		} catch (IOException e) {
			System.err.println("Error connecting with " + hostname + ".");
			System.exit(1);
		}
		return line;
	}
	static void playGame(String hostname, int port,Scanner in){
		Socket echoSocket = null;
		PrintWriter out = null;
		Game game = games.get(new Player(hostname,port));
		int n=-1;
		if(game==null){
			System.out.println("unable to create a game");
			return;
		}
		if(!game.myTurnDone()) {
			try {
				//System.out.println("trying to create a socket");
				echoSocket = new Socket(hostname, port);
				//System.out.println("trying to create output stream");
				out = new PrintWriter(echoSocket.getOutputStream(), true);
				//System.out.println("trying to create input stream");
				System.out.println("enter your digit");
				while(n<0){
					String s=in.nextLine();
					n=Integer.parseInt(s);
				}

				game.myGuess(n,new Date());

				out.println("PLAY:" + player+"~"+ game.myNumber+"~"+df.format(game.myTime));
				out.close();
				echoSocket.close();
			} catch (UnknownHostException e) {
				System.err.println("Unknown host: " + hostname + ".");
				System.exit(1);
			} catch (IOException e) {
				System.err.println("Error connecting with " + hostname + ".");
				System.exit(1);
			}
		}

		else if(game.opponentTurnDone()){
			System.out.println("game is over,winner is:"+game.winner());
		}
		else System.out.println("waiting for opponent");
	}
	static void sendQuit(String hostname,int port){
		Socket echoSocket = null;
		PrintWriter out = null;
		String line=null;
		try {
			//System.out.println("trying to create a socket");
			echoSocket = new Socket(hostname, port);
			//System.out.println("trying to create output stream");
			out = new PrintWriter(echoSocket.getOutputStream(), true);
			//System.out.println("trying to create input stream");
			out.println("QUIT:" + player);
			out.close();
			echoSocket.close();
		} catch (UnknownHostException e) {
			System.err.println("Unknown host: " + hostname + ".");
			System.exit(1);
		} catch (IOException e) {
			System.err.println("Error connecting with " + hostname + ".");
			System.exit(1);
		}
	}
	public static void main(String args[]) {

		new Thread(new Server()).start();
		String hostname;
		String line;
		int port;
		while (serverPort.equals(0)) { //wait a bit until server thread will assign value to its players port
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		Scanner scan=new Scanner(System.in);
		String userInput;
			do {
				System.out.print("enter command >>");
				userInput = scan.nextLine();

				if (userInput.equals("list")) {
					//TODO show players
					System.out.println(players);
					System.out.println(games);
				} else if (userInput.startsWith("host")) {
					//TODO connect new user
					String s = userInput.substring(4).trim();
					hostname = s.split(":")[0];
					port = Integer.parseInt(s.split(":")[1]);
					line = sendJoin(hostname, port);
					if (line.equals("YOURSELF")) {
						System.out.println("cannot connect to yourself");
					}
					if (line.startsWith("PLAYERS:")) {
						line = line.replaceAll("\\[", "").replaceAll("\\]", "");
						for (String x : line.substring(8).split(",")) {
							Player p = new Player(x.trim());
							players.add(p);
							if (!p.equals(player) && !games.containsKey(p)) {
								games.put(p, new Game());
							}
							sendJoin(p.getAddress(), p.getPort());
						}
						System.out.println(players);
					}
				} else if (userInput.startsWith("play")) {
					//TODO make your move
					String s = userInput.substring(4).trim();
					hostname = s.split(":")[0];
					port = Integer.parseInt(s.split(":")[1]);
					playGame(hostname, port,scan);
				} else if (userInput.equals("quit")) {
					//TODO check exit
					if (games.values().stream().filter(q -> q.done()).collect(Collectors.toList()).size() != games.values().size()) {
						//check if played with everyone
						userInput = "not ready to exit";
						System.out.println(userInput);
					}
					else {
						for(Game g:games.values()){
							System.out.println(g);
						}
						for (Player p : players) {
							sendQuit(p.getAddress(), p.getPort());
						}
					}
				} else System.out.println("command unknown");

			} while (!userInput.equals("quit"));

		scan.close();
		System.exit(0);
	}
}