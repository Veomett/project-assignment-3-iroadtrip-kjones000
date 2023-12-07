import java.util.List;
import java.util.*;
import java.io.*;

public class IRoadTrip {
    private HashMap<String, String> idToCountry; //given by state_name.tsv
    private HashMap<String, HashMap<String, Double>> adjacencyMap; //given by borders.txt
    private HashMap<String, HashMap<String, Integer>> capitalsMap; //given by capdist.csv


    public IRoadTrip (String [] args) {
        //System.out.println(args[0]); -> borders.txt
        //System.out.println(args[1]); -> capdist.csv
        //System.out.println(args[2]); -> state_name.tsv
        //TODO => CREATE SEPARATE METHODS TO READ EACH TYPE OF FILE AND STORE THE DATA IN THEIR RESPECTIVE MAPS
        idToCountry = new HashMap<>();
        adjacencyMap = new HashMap<>();
        capitalsMap = new HashMap<>();

        readBordersTXT(args[0]);
        readCapDistCSV(args[1]);
        readStateNameTSV(args[2]);

    }

    public void readBordersTXT (String filename){
        //TODO: have a test case so that @line 5 it outputs no value bc there are no adjacent countries
        //HOW TO ACCESS A COUNTRY'S ADJACENCY LIST THAT IS ALREADY IN THE MAP:
            //.contains(country)? -> value.put(key, value) [key = country, value = distance]

        try (FileReader f = new FileReader(filename);
             BufferedReader br = new BufferedReader(f)) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] edgeInfo = line.split("="); //separates the source vertex from its outgoing edges
                //if !.contains(;) -> te edge to there is only one outgoing edge -> ONLY split by " "
                String sourceVertex = edgeInfo[0];
                HashMap<String, Double> edges = new HashMap<>();
                if (edgeInfo[1].contains(";")){//there is more than one edge/adjacent country
                    String[] outgoingEdges = edgeInfo[1].split(";");
                    //NEED A CASE THAT HANDLES WHETHER THE DISTANCE CONTAINS A COMMA: EX. 2,940 km
                    //NEED A CASE THAT HANDLES SPACES IN A COUNTRY NAME -> TO GET THE DISTANCE NOW..
                        //GET THE LENGTH OF THE COUNTRY NAME, CREATE A SUBSTRING, THEN SPLICE VIA " "
                    for (int i = 0; i < outgoingEdges.length; i++){
                        String country = countryName(outgoingEdges[i]);
                        String distance = outgoingEdges[i].substring(country.length());
                        String[] parseDistance = distance.split(" "); //"2690 km"
                        if (parseDistance[0].contains(",")){
                            String dist = wholeNumStr(parseDistance[0]);
                            edges.put(country, Double.parseDouble(dist));
                        }
                        else {
                            edges.put(country, Double.parseDouble(parseDistance[0]));
                        }
                    }
                }
                else{//only one edge to process
                    if (edgeInfo[1].equals(" ")){ //the country is set equal to nothing
                        edges.put("No out-going edge.", 0.0);
                    }
                    else {
                        String country = countryName(edgeInfo[1]);
                        String distance = edgeInfo[1].substring(country.length());
                        //country.length() takes us to the space character BEFORE the distance value
                        String[] parseDistance = distance.split(" "); //"2690 km"
                        if (parseDistance[0].contains(",")){
                            String dist = wholeNumStr(parseDistance[0]);
                            edges.put(country, Double.parseDouble(dist));
                        }
                        else {
                            edges.put(country, Double.parseDouble(parseDistance[0]));
                        }
                    }
                }
                adjacencyMap.put(sourceVertex, edges);
            }
        }
        catch (IOException e) {
            System.out.println("Cannot access file.");
        }
    }
    public String wholeNumStr (String distance){
        StringBuilder wholeDist = new StringBuilder();
        for (int i = 0; i < distance.length(); i++){
            if (distance.charAt(i) != ','){
                wholeDist.append(distance.charAt(i));
            }
        }
        return String.valueOf(wholeDist);
    }

    public String countryName (String wholeEdgeData){
        //reads the edge into a str builder bc some country names have spaces in them and the regex cannot differentiate
        StringBuilder country = new StringBuilder();
        for (int i = 0; i < wholeEdgeData.length(); i++){
            if (wholeEdgeData.charAt(i) >= 48 && wholeEdgeData.charAt(i) <= 57){//the character is a digit
                break; //leave the loop, the country name has been fully appended and now we are looking at the dist#
            }
            else{
                country.append(wholeEdgeData.charAt(i));
            }
        }
        return String.valueOf(country);
    }

    public void readCapDistCSV (String filename){
        //.csv files are separated by commas ","

        //!IMPORTANT! this file does not dictate adjacency, that is given by borders.txt
        //!IMPORTANT! IGNORE THE FIRST LINE OF CAPDIST BC IT DOES NOT CONTAIN READABLE DATA

        //of the six fields:
            //read field 2[country code/3 letter abbrev.], field 4[country code/3 letter abbrev, field 5[km dist]
        //km dist == distance between the capital of field 2 and the capital of field 4

        try (FileReader f = new FileReader(filename);
             BufferedReader csv = new BufferedReader(f)) {
            int lineNum = 1;
            String line;
            while ((line = csv.readLine()) != null) {
                if (lineNum >= 2) {
                    String[] cDist = line.split(",");
                    //TODO: OPTIMIZE SO THE SAME CODE IS NOT RE-WRITTEN
                    if (capitalsMap.containsKey(cDist[1]) && capitalsMap.containsKey(cDist[3])) {
                        //access the values map for both so that the distance between is can be accessed from both capitals
                        //first connection:
                        HashMap<String, Integer> valueMap = capitalsMap.get(cDist[1]);
                        valueMap.put(cDist[3], Integer.parseInt(cDist[4]));
                        capitalsMap.put(cDist[1], valueMap);
                        //second connection:
                        valueMap = capitalsMap.get(cDist[3]);
                        valueMap.put(cDist[1], Integer.parseInt(cDist[4]));
                        capitalsMap.put(cDist[3], valueMap);

                    } else {
                        //one of the capitals is not in the map
                        //this needs to be its own case bc the hashmap needs to be created if the country is "new"
                        if (!(capitalsMap.containsKey(cDist[1])) && capitalsMap.containsKey(cDist[3])) {
                            //COUNTRY A IS NOT IN THE MAP
                            HashMap<String, Integer> valueMap = new HashMap<>();
                            valueMap.put(cDist[3], Integer.parseInt(cDist[4]));
                            capitalsMap.put(cDist[1], valueMap);

                            valueMap = capitalsMap.get(cDist[3]);
                            valueMap.put(cDist[1], Integer.parseInt(cDist[4]));
                            capitalsMap.put(cDist[3], valueMap);
                        } else if (capitalsMap.containsKey(cDist[1]) && !(capitalsMap.containsKey(cDist[3]))) {
                            //COUNTRY B IS NOT IN THE MAP
                            HashMap<String, Integer> valueMap = capitalsMap.get(cDist[1]);
                            valueMap.put(cDist[3], Integer.parseInt(cDist[4]));
                            capitalsMap.put(cDist[1], valueMap);

                            valueMap = new HashMap<>();
                            valueMap.put(cDist[1], Integer.parseInt(cDist[4]));
                            capitalsMap.put(cDist[3], valueMap);
                        } else if (!(capitalsMap.containsKey(cDist[1])) && !(capitalsMap.containsKey(cDist[3]))) {
                            //NEITHER COUNTRY ARE IN THE MAP
                            HashMap<String, Integer> valueMap = new HashMap<>();
                            valueMap.put(cDist[3], Integer.parseInt(cDist[4]));
                            capitalsMap.put(cDist[1], valueMap);

                            valueMap = new HashMap<>();
                            valueMap.put(cDist[1], Integer.parseInt(cDist[4]));
                            capitalsMap.put(cDist[3], valueMap);
                        }
                    }
                }
                lineNum++;
            }
        }
        catch (IOException e) {
            System.out.println("Cannot access file.");
        }

    }

    public void readStateNameTSV (String filename){
        //.tsv files are separated by "\t"

        //FIRST: check the fifth field [index 4 of the parsed string] to see whether the date contains 2020
        try (FileReader f = new FileReader(filename);
             BufferedReader tsv = new BufferedReader(f)) {
            String line;
            while ((line = tsv.readLine()) != null) {
                String[] stateInfo = line.split("\t");

                if (recentDataCheck(stateInfo[4])){
                    //YEM is in the map
                    idToCountry.put(stateInfo[1], stateInfo[2]);
                    //              USA           United States of America
                }
            }
        }
        catch (IOException e) {
            System.out.println("Cannot access file.");
        }
    }

    public boolean recentDataCheck (String date){
        //parses the date via "-" to make sure @index0 has 2020
        String[] d = date.split("-");
        if (d[0].equals("2020")){
            return true;
        }
        else {
            return false;
        }
    }
    public int getDistance (String country1, String country2) {
        //DIFFERENCE FROM FINDPATH(): RETURNING THE MIN OF THE HEAP
        PriorityQueue<Node> minHeap = new PriorityQueue<>();
        Node sourceNode = new Node(country1, 0);
        HashMap<String, Integer> visitMap = new HashMap<>();
        //HashMap<String, Double> country1Edges = adjacencyMap.get(country1);
        visitMap.put(country1, 0);
        for (String allEdge: adjacencyMap.keySet()){
            visitMap.put(allEdge, Integer.MAX_VALUE);
        }
        minHeap.add(sourceNode);

        boolean c2Found = false;
        while (!(minHeap.isEmpty()) && !c2Found){
            Node min = minHeap.poll();
            for (Map.Entry<String, Double> distance: adjacencyMap.get(min.countryVertex).entrySet()){
                Double v = distance.getValue();
                Integer uWeight = capitalsMap.get(min.countryVertex).get(distance.getKey());

                if (v > uWeight){
                    visitMap.put(distance.getKey(), uWeight);
                    Node addToHeap = new Node(distance.getKey(), uWeight);
                    minHeap.add(addToHeap);
                }

                if (distance.getKey().equals(country2)){
                    c2Found = true;
                    break;
                }
            }

        }

        return Objects.requireNonNull(minHeap.poll()).distance; //country 2 was still found and the shortest distance should be the top of the
                                                                //heap
    }


    public List<String> findPath (String country1, String country2) {
        //will use the capitalMap to get the distance between country capitals
        //DIJKSTRA'S ALGORITHM:
        PriorityQueue<Node> minHeap = new PriorityQueue<>();
        List<String> path = new ArrayList<>();

        //source vertex == country1
        //use the adjacecnymap to access the adjacent countries
            // -> use the capdist map to get the distance betweent those two countries capitals
        //need to initialize the distances to infinity so that when theyre updated, the queue will heapify:
        Node sourceNode = new Node(country1, 0);
        HashMap<String, Integer> visitMap = new HashMap<>();
        //HashMap<String, Double> country1Edges = adjacencyMap.get(country1); //returns the adjacent countries of c1
        //iterate through the countryEdges, add only the keys, and the value infinity and put it in the visitMap
        visitMap.put(country1, 0);
        for (String allEdge: adjacencyMap.keySet()){
            visitMap.put(allEdge, Integer.MAX_VALUE);
        }
        minHeap.add(sourceNode);
        //need to iteratively go throguh the minheap and extract the min
        //THEN loop through the adjacent edges of the min
        //vertex + edge value == distance to capital?
        // if v > u + weight WHERE v == distance stored in the node && u + weight == distance between capitals
        boolean c2Found = false;
        while (!(minHeap.isEmpty()) && !c2Found){
            Node min = minHeap.poll();
            for (HashMap.Entry<String, Double> distance : adjacencyMap.get(min.countryVertex).entrySet()) {
                Double v = distance.getValue();
                Integer uWeight = capitalsMap.get(min.countryVertex).get(distance.getKey());

                if (v > uWeight) {
                    visitMap.put(distance.getKey(), uWeight);
                    Node addToHeap = new Node(distance.getKey(), uWeight);
                    minHeap.add(addToHeap);
                }

                if (distance.getKey().equals(country2)) {
                    c2Found = true;
                    break;
                }
            }


        }
        for (Map.Entry<String, Integer> pathOrder: visitMap.entrySet()){
            StringBuilder sb = new StringBuilder();
            sb.append(minHeap.poll().countryVertex + " --> "+ pathOrder.getKey() + ": " + getDistance(country1, country2));
            path.add(String.valueOf(sb));
        }

        return null;
    }



    public void acceptUserInput() {
        //UNDIRECTED GRAPH -> can assume the connection appears twice: from src && from dst
        Scanner scan = new Scanner(System.in);
        String countryA = "valid";
        String countryB = "valid";

        while (!countryA.equals("EXIT") || !countryB.equals("EXIT")){
            System.out.println("Enter the name of the first country (type EXIT to quit): ");
            countryA = scan.next(); //so the input appears on the same line as the prompt
            while (!(idToCountry.containsValue(countryA)) && !countryA.equals("EXIT")){ //if condition is not met, entire loop skipped
                System.out.println("Invalid country name. Please enter a valid country name.");
                System.out.println("Enter the name of the first country (type EXIT to quit): ");
                countryA = scan.next();
            }
            if (countryA.equals("EXIT")){
                break;
            }

            System.out.println("Enter the name of the second country (type EXIT to quit): ");
            countryB = scan.next();
            while (!(idToCountry.containsValue(countryB)) && !countryB.equals("EXIT")){
                //if condition is not met, entire loop skipped
                System.out.println("Invalid country name. Please enter a valid country name.");
                System.out.println("Enter the name of the first country (type EXIT to quit): ");
                countryB = scan.next();
            }


            if (countryB.equals("EXIT")){
                break;
            }
            else{
                System.out.println("Route from "+countryA+" to "+countryB);
                List<String> path = findPath(countryA, countryB);
                for (int i = 0; i < path.size(); i++){
                    System.out.println(path.get(i));
                }
            }
        }
    }
    public class Node implements Comparable{
        String countryVertex;
        Integer distance; //distance from the source vertex

        public Node(String vertex, Integer dist){
            this.countryVertex = vertex;
            this.distance = dist;
        }

        @Override
        public int compareTo(Object o) {
            return 0;
        }
    }


    public static void main(String[] args) {
        IRoadTrip a3 = new IRoadTrip(args);

        a3.acceptUserInput();
    }

}

