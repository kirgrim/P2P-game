package Task1;

import java.util.Random;

public class Player implements Comparable{


    String name;
    String address;
    int port;

    public Player(String s) {
       String[] tmp= s.split("/");
        name=tmp[0];
        tmp[1].split(":");
        address=tmp[1].split(":")[0];
        port=Integer.parseInt(tmp[1].split(":")[1]);
    }
    public Player(String name,String address, int port) {
        this.name = name;
        this.address = address;
        this.port = port;
    }
    public Player(String address, int port) {
        this.name = "User"+String.valueOf(new Random().nextInt(10000000));
        this.address = address;
        this.port = port;
    }
    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }

    @Override
    public int hashCode() {
        return address.hashCode()+port;
    }

    @Override
    public boolean equals(Object obj) {
            return compareTo(obj)==0;
    }

    @Override
    public String toString() {
        return name+"/"+address+":"+port;
    }

    @Override
    public int compareTo(Object obj) {
        if((obj instanceof Player) && (obj!=null) ){
            return (((Player)obj ).port==port)&&((Player)obj ).address.equals(address)?0:-1;
        }
        else return -1;
    }
}
