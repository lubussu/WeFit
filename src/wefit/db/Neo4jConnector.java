package wefit.db;

import com.mongodb.ConnectionString;
import com.mongodb.MongoException;
import com.mongodb.client.*;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertOneResult;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.neo4j.driver.*;
import org.neo4j.driver.Record;
import org.neo4j.driver.internal.InternalPath;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;

import static com.mongodb.client.model.Aggregates.*;
import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Projections.*;
import static org.neo4j.driver.Values.parameters;

public class Neo4jConnector {
    Driver graph_driver;

    public Neo4jConnector(String conn, String username, String password){
        graph_driver = GraphDatabase.driver( "bolt://localhost:7687", AuthTokens.basic( "neo4j", "wefit" ) );
    }

    public void followUser(String user, String followed){
        try ( Session session = graph_driver.session() ) {
            session.run("MATCH (a:User) MATCH (b:User) " +
                            "WHERE a.user_id = $user AND b.user_id = $followed " +
                            "MERGE (a)-[:FOLLOW]->(b) RETURN a,b",
                    parameters("user", user, "followed", followed));
        };
        System.out.println("User " + user +" succesfully follow!");
    }

    public void printRoutines(ArrayList<Record> rec){
        System.out.printf("%3s %10s %15s %15s %15s", "   ", "Trainer", "Level", "Starting day", "End day\n");
        System.out.println("--------------------------------------------------------------------------------------------------------");
        for(int i=0; i<rec.size(); i++) {
            Record r = rec.get(i);
            System.out.printf("%3s %10s %15s %15s %15s", (i+1)+") ", r.get("routine").get("trainer").toString().replace("\"",""),r.get("routine").get("level").toString().replace("\"",""),
                    r.get("routine").get("starting_day").toString().replace("\"",""),r.get("routine").get("end_day").toString().replace("\"",""));
            System.out.println("\n");
        }
    }

    public void printUsers(ArrayList<Record> rec) {
        System.out.printf("%3s %10s %20s %10s %15s %15s %10s", "   ", "User_Id", "Name", "Gender", "Year of birth", "Level","Trainer\n");
        System.out.println("--------------------------------------------------------------------------------------------------------");
        for(int i=0; i<rec.size(); i++) {
            Record r = rec.get(i);
            System.out.printf("%3s %10s %20s %10s %15s %15s %10s", (i+1)+") ", r.get("user").get("user_id").toString().replace("\"",""), r.get("user").get("name").toString().replace("\"",""),
                    r.get("user").get("gender").toString().replace("\"",""), r.get("user").get("birth").toString().replace("\"",""),
                    r.get("user").get("level").toString().replace("\"",""), r.get("user").get("trainer").toString().replace("\"",""));
            System.out.println("\n");
        }
    }

    public String showFollowedUsers(String user){
        try ( Session session = graph_driver.session() ) {
            ArrayList<Record> followed = (ArrayList<Record>) session.readTransaction(tx-> {
                List<Record> persons;
                persons = tx.run("MATCH (a:User) -[:FOLLOW]->(b:User) WHERE a.user_id = $user RETURN b AS user",
                        parameters("user",user)).list();
                return persons;
            });
            printUsers(followed);

            String input;
            while (true) {
                System.out.println("Press the number of the user you want to select\n" +
                        "or press 0 to return to the main menu");

                Scanner sc = new Scanner(System.in);
                input = sc.next();
                if (!input.matches("[0-9.]+"))
                    System.out.println("Please select an existing option!");
                else if ((Integer.parseInt(input)) > followed.size())
                    System.out.println("Please select an existing option!\n");
                else
                    break;
            }
            switch (input) {
                case "0":
                    return null;
                default:
                    String id = followed.get(Integer.parseInt(input)-1).get("user").get("user_id").toString();
                    System.out.println("Press 1 to see user's details\n"+
                                        "or press 2 to see user's routines");

                    Scanner sc = new Scanner(System.in);
                    input = sc.next();
                    switch (input){
                        case "1":
                            return "u:"+id.replace("\"","");
                        case"2": {
                            String routine = showRoutines(id.replace("\"", ""), "all");
                            if(routine != null)
                                return "r:" + routine;
                        }
                    }
            }
        };
        return null;
    }

    public String showRoutines(String user, String period){
        ArrayList<Record> followed = new ArrayList<>();
        try ( Session session = graph_driver.session() ) {
            followed = (ArrayList<Record>) session.readTransaction((TransactionWork<List<Record>>) tx-> {
                List<Record> routines;
                String c_day = LocalDate.now().toString();
                if(period.equals("all"))
                    routines = tx.run("MATCH (a:User) -[:HAS_ROUTINE]->(b:Routine) WHERE a.user_id = $user RETURN b AS routine",parameters("user",user)).list();
                else if(period.equals("past"))
                    routines = tx.run("MATCH (a:User) -[:HAS_ROUTINE]->(b:Routine) WHERE a.user_id = $user AND b.end_day < $date RETURN b AS routine",
                            parameters("user",user, "date", c_day)).list();
                else
                    routines = tx.run("MATCH (a:User) -[:HAS_ROUTINE]->(b:Routine) WHERE a.user_id = $user AND b.end_day >= $date RETURN b AS routine",
                            parameters("user",user, "date", c_day)).list();
                ArrayList<Record> results = new ArrayList<>();
                return routines;
            });
        };
        printRoutines(followed);

        String input;
        while (true) {
            System.out.println("Press the number of the routine you want to select\n" +
                    "or press 0 to return to the main menu");

            Scanner sc = new Scanner(System.in);
            input = sc.next();
            if (!input.matches("[0-9.]+"))
                System.out.println("Please select an existing option!");
            else if ((Integer.parseInt(input)) > followed.size())
                System.out.println("Please select an existing option!\n");
            else
                break;
        }
        switch (input) {
            case "0":
                return null;
            default:
                return followed.get(Integer.parseInt(input)-1).get("routine").get("_id").toString().replace("\"","");
        }
    }

    public void unfollowUser(String user, String followed){
        try ( Session session = graph_driver.session() ) {
            session.run("MATCH (a:User)-[f:FOLLOW]->(b:User) " +
                            "WHERE a.user_id = $user AND b.user_id = $followed " +
                            "DELETE f",
                        parameters("user", user, "followed", followed));
        };
        System.out.println("User " + user +" succesfully unfollow!");
    }

}
