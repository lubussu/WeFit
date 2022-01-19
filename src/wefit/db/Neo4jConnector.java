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
    private Driver graph_driver;

    public Neo4jConnector(String conn, String username, String password){
        graph_driver = GraphDatabase.driver( "bolt://localhost:7687", AuthTokens.basic( "neo4j", "wefit" ) );
    }

    //function for change profile's properties in the db
    public void changeProfile(Document user){
        try ( Session session = graph_driver.session() ) {
            session.run("MATCH (a:User) WHERE a.user_id = $user "+
                    "SET a.name=$name, a.gender=$gender, a.birth=$birth, a.level=$level, a.trainer=$trainer",
                    parameters("user", user.getString("user_id"), "name", user.getString("name"),
                            "gender", user.getString("gender"), "birth", user.getString("birth"),
                            "level", user.getString("level"), "trainer", user.getString("trainer")));
        };
    }

    //function for follow a user (add relation in the db)
    public void followUser(String user, String followed){
        try ( Session session = graph_driver.session() ) {
            session.run("MATCH (a:User) MATCH (b:User) " +
                            "WHERE a.user_id = $user AND b.user_id = $followed " +
                            "MERGE (a)-[:FOLLOW]->(b) RETURN a,b",
                    parameters("user", user, "followed", followed));
        };
        System.out.println("User " + user +" successfully follow!");
    }

    //function for vote a routine (add relation in the db)
    public void insertVote(String user_id, String routine_id, int vote){
        try ( Session session = graph_driver.session() ) {
            session.run("MATCH (a:User) MATCH (b:Routine) " +
                            "WHERE a.user_id = $user AND b._id = routine " +
                            "CREATE (a)-[:VOTE{vote:$vote}]->(b) RETURN a,b",
                    parameters("user", user_id, "routine", routine_id, "vote", vote));
        };
        System.out.println("Routine voted successfully!");
    }

    //function for add a new user (add a node in the db)
    public void insertUser(Document user) {
        try ( Session session = graph_driver.session() )
        {
            session.run("CREATE (a:User {user_id:$user_id, name:$name, gender:$gender, birth:$birth, level:$level, trainer:$trainer})",
                    parameters("user_id", user.getString("user_id"), "name", user.getString("name"), "gender", user.getString("gender"),
                            "birth",user.getString("birth"),"level",user.getString("level"), "trainer", user.getString("trainer")));
        };
    }

    //function for add a new routine (add a node in the db)
    public void insertRoutine(Document routine) {
        try ( Session session = graph_driver.session() )
        {
            session.run("MATCH (a:User {user_id:$user}) MATCH (b:User {user_id:$trainer})" +
                    "CREATE (r:Routine {user:$user, trainer:$trainer, level:$level, starting_day:$s_day, end_day:$e_day}) "+
                    "CREATE (a)-[:HAS_ROUTINE]->(r) CREATE (b)-[:CREATE_ROUTINE]->(r)",
                    parameters("user", routine.getString("user"), "trainer", routine.getString("trainer"), "level", routine.getString("level"),
                            "s_day",routine.getString("starting_day"),"e_day",routine.getString("end_day")));
        };
    }

    public String mostFollowedUsers(int num){
        String query = "MATCH ()-[f:FOLLOW]->(u:User) "+
                "WITH u, COUNT(f) AS num_followers "+
                "RETURN u AS user, num_followers ORDER BY num_followers DESC LIMIT $num";

        ArrayList<Record> trainers;
        try ( Session session = graph_driver.session() ) {
            trainers = (ArrayList<Record>) session.readTransaction(tx -> {
                List<Record> records;
                records = tx.run(query, parameters("num", num)).list();
                return records;
            });

            System.out.printf("%5s %10s %20s %10s %10s %15s", "     ", "User_Id", "Name", "Gender", "Trainer", "Followers\n");
            System.out.println("--------------------------------------------------------------------------------------------------------");
            for(int i=0; i<trainers.size(); i++) {
                Record r = trainers.get(i);
                System.out.printf("%5s %10s %20s %10s %10s %15s", (i+1)+") ", r.get("user").get("user_id").toString().replace("\"",""), r.get("user").get("name").toString().replace("\"",""),
                        r.get("user").get("gender").toString().replace("\"",""), r.get("user").get("trainer").toString().replace("\"",""),
                        r.get("num_followers").toString());
                System.out.println("\n");
            }
        };
        return selectUser(trainers);
    }

    public String mostRatedTrainers(int num){
        String query = "MATCH ()-[v:VOTE]->(r:Routine)<-[:CREATE_ROUTINE]-(u:User) "+
                "WITH u, AVG(toInteger(v.vote)) AS avg_vote "+
                "RETURN u AS user, avg_vote ORDER BY avg_vote DESC LIMIT $num";

        ArrayList<Record> trainers;
        try ( Session session = graph_driver.session() ) {
            trainers = (ArrayList<Record>) session.readTransaction(tx -> {
                List<Record> records;
                records = tx.run(query, parameters("num", num)).list();
                return records;
            });

            System.out.printf("%5s %10s %20s %10s %10s", "     ", "User_Id", "Name", "Gender", "Vote\n");
            System.out.println("--------------------------------------------------------------------------------------------------------");
            for(int i=0; i<trainers.size(); i++) {
                Record r = trainers.get(i);
                System.out.printf("%5s %10s %20s %10s %10s", (i+1)+") ", r.get("user").get("user_id").toString().replace("\"",""), r.get("user").get("name").toString().replace("\"",""),
                        r.get("user").get("gender").toString().replace("\"",""), r.get("avg_vote").toString());
                System.out.println("\n");
            }
        };
        return selectUser(trainers);
    }

    //function for print summary information of the given routines
    public void printRoutines(ArrayList<Record> rec){
        System.out.printf("%5s %10s %15s %15s %15s", "     ", "Trainer", "Level", "Starting day", "End day\n");
        System.out.println("--------------------------------------------------------------------------------------------------------");
        for(int i=0; i<rec.size(); i++) {
            Record r = rec.get(i);
            System.out.printf("%5s %10s %15s %15s %15s", (i+1)+") ", r.get("routine").get("trainer").toString().replace("\"",""),r.get("routine").get("level").toString().replace("\"",""),
                    r.get("routine").get("starting_day").toString().replace("\"",""),r.get("routine").get("end_day").toString().replace("\"",""));
            System.out.println("\n");
        }
    }

    //function for print summary information of the given users
    public void printUsers(ArrayList<Record> rec) {
        System.out.printf("%5s %10s %20s %10s %15s %15s %10s", "     ", "User_Id", "Name", "Gender", "Year of birth", "Level","Trainer\n");
        System.out.println("--------------------------------------------------------------------------------------------------------");
        for(int i=0; i<rec.size(); i++) {
            Record r = rec.get(i);
            System.out.printf("%5s %10s %20s %10s %15s %15s %10s", (i+1)+") ", r.get("user").get("user_id").toString().replace("\"",""), r.get("user").get("name").toString().replace("\"",""),
                    r.get("user").get("gender").toString().replace("\"",""), r.get("user").get("birth").toString().replace("\"",""),
                    r.get("user").get("level").toString().replace("\"",""), r.get("user").get("trainer").toString().replace("\"",""));
            System.out.println("\n");
        }
    }
    
    //function to select a user from given users
    public String selectUser(ArrayList<Record> users){
        String input;
        while (true) {
            System.out.println("Press the number of the user you want to select\n" +
                    "or press 0 to return to the main menu");

            Scanner sc = new Scanner(System.in);
            input = sc.next();
            if (!input.matches("[0-9.]+"))
                System.out.println("Please select an existing option!");
            else if ((Integer.parseInt(input)) > users.size())
                System.out.println("Please select an existing option!\n");
            else
                break;
        }
        switch (input) {
            case "0":
                return null;
            default:
                String id = users.get(Integer.parseInt(input)-1).get("user").get("user_id").toString();
                String trainer = users.get(Integer.parseInt(input)-1).get("user").get("trainer").toString().replace("\"", "");
                System.out.println("Press 1 to see user's details\n"+
                        "or press 2 to see user's routines");

                Scanner sc = new Scanner(System.in);
                input = sc.next();
                switch (input){
                    case "1":
                        return "u:"+id.replace("\"","");
                    case"2": {
                        if (trainer.equals("yes")){
                            System.out.println("Press 1 to see own routines\n"+
                                    "or press 2 to see routine's created");

                            input = sc.next();
                            String routine=null;
                            switch (input){
                                case "1":
                                    routine = showRoutines(id.replace("\"", ""), "all");
                                    if (routine != null)
                                        return "r:" + routine;
                                    System.out.println("Result not found");
                                    break;
                                case "2":
                                    //routine = showTrainerRoutines(id.replace("\"", ""), "all"); ---> DA FARE
                                    if (routine != null)
                                        return "r:" + routine;
                                    System.out.println("Result not found");
                                    break;
                            }
                        }
                        else {
                            String routine = showRoutines(id.replace("\"", ""), "all");
                            if (routine != null)
                                return "r:" + routine;
                        }
                    }
                }
        }
        return null;
    }

    //function for retry the list of followers/followed users (and select one of them)
    public String showFollowUsers(String user, String option){
        String query;
        if(option.equals("followed"))
            query="MATCH (a:User)-[:FOLLOW]->(b:User) WHERE a.user_id = $user RETURN b AS user";
        else
            query="MATCH (a:User)-[:FOLLOW]->(b:User) WHERE b.user_id = $user RETURN a AS user";
        ArrayList<Record> users = new ArrayList<>();
        try ( Session session = graph_driver.session() ) {
            users = (ArrayList<Record>) session.readTransaction(tx -> {
                List<Record> persons;
                persons = tx.run(query, parameters("user", user)).list();
                return persons;
            });
            printUsers(users);
        };
            return selectUser(users);
    }

    public void showRecommended(String id) {
        try (Session session = graph_driver.session()) {
            ArrayList<Record> recommended = (ArrayList<Record>) session.readTransaction(tx -> {
                List<Record> persons;
                persons = tx.run("MATCH (a:User)-[:FOLLOW]->(b:User)-[:FOLLOW]->(c:User) WHERE a.user_id = $user " +
                                "AND NOT exists((a)-[:FOLLOW]->(c)) AND " +
                                "a.level = c.level" +
                                "RETURN c AS user LIMIT 5",
                        parameters("user", id)).list();
                return persons;
            });
            printUsers(recommended);
        }
    }

    // function that show how many users gained a level in the interval specified
    public void showLvlUpBI(String start, String end){
        try (Session session = graph_driver.session()) {
            ArrayList<Record> recommended = (ArrayList<Record>) session.readTransaction(tx -> {
                List<Record> persons;
                persons = tx.run("MATCH (s:Routine)<-[:HAS_ROUTINE]-(a:User)-[:HAS_ROUTINE]->(r:Routine) " +
                                "WHERE r.starting_day >= $start AND s.end_day <= $end AND r.level = \"Beginner\" AND s.level = \"Intermediate\" AND s.starting_day > r.starting_day " +
                                "RETURN count(DISTINCT a) AS user",
                        parameters("start", start, "end", end)).list();
                return persons;
            });
            Record r = recommended.get(0);
            System.out.printf("%22s", r.get("user").toString());
        }
    }

    public void showLvlUpIE(String start, String end){
        try (Session session = graph_driver.session()) {
            ArrayList<Record> recommended = (ArrayList<Record>) session.readTransaction(tx -> {
                List<Record> persons;
                persons = tx.run("MATCH (s:Routine)<-[:HAS_ROUTINE]-(a:User)-[:HAS_ROUTINE]->(r:Routine) " +
                                "WHERE r.starting_day >= $start AND s.end_day <= $end AND r.level = \"Intermediate\" AND s.level = \"Expert\" AND s.starting_day > r.starting_day " +
                                "RETURN count(DISTINCT a) AS user",
                        parameters("start", start, "end", end)).list();
                return persons;
            });
            Record r = recommended.get(0);
            System.out.printf("%26s", r.get("user").toString());
        }
    }

    public void showLvlUpBE(String start, String end){
        try (Session session = graph_driver.session()) {
            ArrayList<Record> recommended = (ArrayList<Record>) session.readTransaction(tx -> {
                List<Record> persons;
                persons = tx.run("MATCH (s:Routine)<-[:HAS_ROUTINE]-(a:User)-[:HAS_ROUTINE]->(r:Routine) " +
                                "WHERE r.starting_day >= $start AND s.end_day <= $end AND r.level = \"Beginner\" AND s.level = \"Expert\" AND s.starting_day > r.starting_day " +
                                "RETURN count(DISTINCT a) AS user",
                        parameters("start", start, "end", end)).list();
                return persons;
            });
            Record r = recommended.get(0);
            System.out.printf("%26s", r.get("user").toString());
        }
    }

    public String showMostFidelityUsers(int num){
        String date = LocalDate.now().toString();
        try (Session session = graph_driver.session()) {
            ArrayList<Record> users = (ArrayList<Record>) session.readTransaction(tx -> {
                List<Record> persons;
                persons = tx.run("MATCH (a:User)-[:HAS_ROUTINE]->(r:Routine) " +
                                "WHERE r.end_day < $date " +
                                "RETURN a AS user, COUNT(r) AS past_routines, min(r.starting_day) AS first_routine ORDER BY past_routines DESC, first_routine LIMIT $num",
                        parameters("date", date, "num", num)).list();
                return persons;
            });

            System.out.printf("%5s %10s %15s %15s", "     ", "User", "Past routines", "First routine\n");
            System.out.println("--------------------------------------------------------------------------------------------------------");
            for (int i = 0; i < users.size(); i++) {
                Record r = users.get(i);
                System.out.printf("%5s %10s %15s %15s", (i + 1) + ") ", r.get("user").get("user_id").toString().replace("\"", ""), r.get("past_routines").toString().replace("\"", ""),
                        r.get("first_routine").toString().replace("\"", ""));
                System.out.println("\n");
            }
            return selectUser(users);
        }
    }

    //function to show all the routines (past or current) of the given user
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

    //function for unfollow a user (add relation in the db)
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
