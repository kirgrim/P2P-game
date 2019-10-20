package Task1;

import java.util.Date;

public class Game {
    int myNumber=-1;
    int opponentNumer=-1;
    Date myTime;
    Date opponentTime;
    public Game(){}
    public Game(int myNumber,int opponentNumer){
        this.opponentNumer=opponentNumer;
        this.myNumber=myNumber;
    }
    boolean done(){
        return myTurnDone()&&opponentTurnDone();
    }
    boolean myTurnDone(){
        return myNumber>=0;
    }
    boolean opponentTurnDone(){
        return opponentNumer>=0;
    }
    void myGuess(int m,Date d){
        myTime=d;
        myNumber=m ;
    }
    void opponentGuess(int m,Date d){
        opponentTime=d;
        opponentNumer=m ;
    }
    String winner(){
         return done()?(myTime.before(opponentTime)^(myNumber+opponentNumer)%2==0?"you":"opponent"):"unknown";
    }
    @Override
    public String toString() {
        return " my ="+myNumber+",op ="+(myTurnDone()?opponentNumer:"x")+(done()?"winner:"+winner():"");
    }
}
