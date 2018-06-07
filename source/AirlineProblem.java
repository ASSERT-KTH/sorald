import java.util.ArrayList;
import java.util.Scanner;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class AirlineProblem {

    public static void main(String[] args){
        Scanner scannerToReadAirlines = null;
        try{
            scannerToReadAirlines = new Scanner(new File("airlines.txt"));
        }
        catch(IOException e){
            System.out.println("Could not connect to file airlines.txt.");
            System.exit(0);
        }
        if(scannerToReadAirlines != null){
            ArrayList<Airline> airlinesPartnersNetwork = new ArrayList<Airline>();
            Airline newAirline;
            String lineFromFile;
            String[] airlineNames;
            
            while( scannerToReadAirlines.hasNext() ){
                lineFromFile = scannerToReadAirlines.nextLine();
                airlineNames = lineFromFile.split(",");
                newAirline = new Airline(airlineNames);
                airlinesPartnersNetwork.add( newAirline );
            }
            System.out.println(airlinesPartnersNetwork);
            Scanner keyboard = new Scanner(System.in);
            System.out.print("Enter airline miles are on: ");
            String start = keyboard.nextLine();
            System.out.print("Enter goal airline: ");
            String goal = keyboard.nextLine();
            ArrayList<String> pathForMiles = new ArrayList<String>();
            ArrayList<String> airlinesVisited = new ArrayList<String>();
            if( canRedeem(start, goal, pathForMiles, airlinesVisited, airlinesPartnersNetwork))
                System.out.println("Path to redeem miles: " + pathForMiles);
            else
                System.out.println("Cannot convert miles from " + start + " to " + goal + ".");
        }
    }
    public static void emp(int x,int y)
    {

    }
    private static boolean canRedeem(String current, String goal,
            ArrayList<String> pathForMiles, ArrayList<String> airlinesVisited,
            ArrayList<Airline> network){
        if(current.equals(goal)){
            //base case 1, I have found a path!
            pathForMiles.add(current);
            return true;
        }
        else if(airlinesVisited.contains(current))
            // base case 2, I have already been here
            // don't go into a cycle
            return false;
        else{
            // I have not been here and it isn't
            // the goal so check its partners
            // now I have been here
            airlinesVisited.add(current);
            
            // add this to the path
            pathForMiles.add(current);
            
            // find this airline in the network
            int pos = -1;
            int index = 0;
            while(pos == -1 && index < network.size()){
                if(network.get(index).getName().equals(current))
                    pos = index;
                index++;
            }
            //if not in the network, no partners
            if( pos == - 1)
                return false;
            
            // loop through partners
            index = 0;
            String[] partners = network.get(pos).getPartners();
            boolean foundPath = false;
            while( !foundPath && index < partners.length){
                foundPath = canRedeem(partners[index], goal, pathForMiles, airlinesVisited, network);
                index++;
            }
            if( !foundPath )
                pathForMiles.remove( pathForMiles.size() - 1);
            return foundPath;
        }
    }

    private static class Airline{
        private String name;
        private ArrayList<String> partners;
        
        //pre: data != null, data.length > 0
        public Airline(String[] data){
            assert data != null && data.length > 0 : "Failed precondition";
            name = data[0];
            partners = new ArrayList<String>();
            for(int i = 1; i < data.length; i++)
                partners.add( data[i] );
        }
        
        public String[] getPartners(){
            return partners.toArray(new String[partners.size()]);
        }
        
        public boolean isPartner(String name){
            return partners.contains(name);
        }
        
        public String getName(){
            return name;
        }
        
        public String toString(){
            return name + ", partners: " + partners;
        }
    }
}
