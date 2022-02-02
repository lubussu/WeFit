package it.unipi.wefit.db;

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
import it.unipi.wefit.entities.*;
import org.neo4j.driver.summary.ResultSummary;

import javax.naming.ServiceUnavailableException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;

import static com.mongodb.client.model.Aggregates.*;
import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Projections.*;
import static com.mongodb.client.model.Updates.push;
import static org.neo4j.driver.Values.parameters;

public class Neo4jConnector {
    private Driver graph_driver;

    public Neo4jConnector(String conn, String username, String password){
            graph_driver = GraphDatabase.driver( "bolt://localhost:7687", AuthTokens.basic( "neo4j", "wefit" ) );
    }

    //function for change profile's properties in the db
    public boolean changeProfile(User us){
        try ( Session session = graph_driver.session() ) {
            ResultSummary rs = session.run("MATCH (a:User) WHERE a.user_id = $user "+
                    "SET a.name=$name, a.gender=$gender, a.birth=$birth, a.level=$level, a.trainer=$trainer",
                    parameters("user", us.getUser_id(), "name", us.getName(),
                            "gender", us.getGender(), "birth", us.getYear_of_birth(),
                            "level", us.getLevel(), "trainer", us.getTrainer())).consume();
            return (rs.counters().propertiesSet()>1);
        }
    }

    public boolean deleteComment(String user, String routine){
        try ( Session session = graph_driver.session() ) {
            ResultSummary rs = session.run("MERGE (u:User {user_id:$user})-[c:COMMENT]->(r:Routine {_id:$routine}) " +
                            "ON MATCH SET c.num=c.num-1 " +
                            "UNION ALL " +
                            "MATCH (u:User {user_id:$user})-[c:COMMENT]->(r:Routine {_id:$routine}) " +
                            "WHERE c.num=0 " +
                            "DELETE c ",
                    parameters("user", user, "routine", routine)).consume();
            return(rs.counters().relationshipsDeleted()==1 || rs.counters().propertiesSet()>1);
        }
    }

    //function for follow a user (add relation in the db)
    public boolean followUser(String user, String followed){
        try ( Session session = graph_driver.session() ) {
            ResultSummary rs = session.run("MATCH (a:User) MATCH (b:User) " +
                            "WHERE a.user_id = $user AND b.user_id = $followed " +
                            "MERGE (a)-[:FOLLOW]->(b) RETURN a,b",
                    parameters("user", user, "followed", followed)).consume();

            if(rs.counters().relationshipsCreated()==0)
                System.out.println("You have already follow this user!");
            return (rs.counters().relationshipsCreated()==1);
        }
    }

    //show all the routines commented by a given user
    public ArrayList<Workout> showCommentedRoutines(String user) {
        ArrayList<Record> routines;
        try ( Session session = graph_driver.session() ) {
            routines = (ArrayList<Record>) session.readTransaction((TransactionWork<List<Record>>) tx-> {
                List<Record> records;

                records = tx.run("MATCH (:User {user_id: $user}) -[r:COMMENT]->(b:Routine) RETURN b AS routine",
                        parameters("user",user)).list();

                if(records == null) {
                    System.out.println("You have not commented any routine yet...\n");}
                return records;
            });
        };
        return returnRoutines(routines);
    }

    //show all the routines of a given trainer
    public ArrayList<Workout> showCreatedRoutines(String trainer) {
        ArrayList<Record> routines;
        try ( Session session = graph_driver.session() ) {
            routines = (ArrayList<Record>) session.readTransaction((TransactionWork<List<Record>>) tx-> {
                List<Record> records;


                records = tx.run("MATCH (a:User {user_id: $trainer}) -[:CREATE_ROUTINE]->(b:Routine) return b AS routine",
                        parameters("trainer",trainer)).list();

                if(records == null) {
                    System.out.println("You have not created any routine yet...\n");}
                return (ArrayList<Record>) records;
            });
        };
        return returnRoutines(routines);
    }

    public boolean insertComment(String user, String routine){
        try ( Session session = graph_driver.session() ) {
            ResultSummary rs = session.run("MATCH (a:User) MATCH (b:Routine) " +
                            "WHERE a.user_id = $user AND b._id = $routine " +
                            "MERGE (a)-[c:COMMENT]->(b) "+
                            "ON CREATE SET c.num=1 "+
                            "ON MATCH SET c.num=c.num+1",
                    parameters("user", user, "routine", routine)).consume();
            return (rs.counters().propertiesSet()==1);
        }
    }

    //function for vote a routine (add relation in the db)
    public boolean insertVote(String user, String routine, int vote){
        try ( Session session = graph_driver.session() ) {
            ResultSummary rs = session.run("MATCH (u:User {user_id:$user}) MATCH (r:Routine {_id:$routine}) " +
                            "MERGE (u)-[s:VOTE]->(r) " +
                            "ON CREATE SET s.vote=$vote " +
                            "UNION ALL " +
                            "MATCH (r:Routine {_id:$routine})<-[s:VOTE]-() " +
                            "WITH r, AVG(s.vote) AS avg SET r.vote=round(avg,3)",
                    parameters("user", user, "routine", routine, "vote", vote)).consume();
            if(rs.counters().relationshipsCreated()==0)
                System.out.println("You have already vote this routine!");
            return (rs.counters().relationshipsCreated()==1);
        }
    }

    //function for add a new user (add a node in the db)
    public boolean insertUser(User user) {
        try ( Session session = graph_driver.session() )
        {
            ResultSummary rs = session.run("CREATE (a:User {user_id:$user_id, name:$name, gender:$gender, birth:$birth, level:$level, trainer:$trainer})",
                    parameters("user_id", user.getUser_id(), "name", user.getName(), "gender", user.getGender(),
                            "birth",user.getYear_of_birth(),"level",user.getLevel(), "trainer", user.getTrainer())).consume();
            return (rs.counters().nodesCreated()==1);
        }
    }

    //function for add a new routine (add a node in the db)
    public boolean insertRoutine(Workout routine, String id) {
        try ( Session session = graph_driver.session() )
        {
            ResultSummary rs = session.run("MATCH (a:User {user_id:$user}) MATCH (b:User {user_id:$trainer})" +
                    "CREATE (r:Routine {_id: $id, user:$user, trainer:$trainer, level:$level, starting_day:$s_day, end_day:$e_day}) "+
                    "CREATE (a)-[:HAS_ROUTINE]->(r) CREATE (b)-[:CREATE_ROUTINE]->(r)",
                    parameters("id", id,"user", routine.getUser(), "trainer", routine.getTrainer(), "level", routine.getLevel(),
                            "s_day",routine.getStarting_day(),"e_day",routine.getEnd_day())).consume();
            return (rs.counters().nodesCreated()==1);
        }
    }

    public ArrayList<User> mostFollowedUsers(int num){
        String query = "MATCH ()-[f:FOLLOW]->(u:User) "+
                "WITH u, COUNT(f) AS num_followers "+
                "RETURN u AS user, num_followers ORDER BY num_followers DESC LIMIT $num";

        ArrayList<Record> users;
        try ( Session session = graph_driver.session() ) {
            users = (ArrayList<Record>) session.readTransaction(tx -> {
                List<Record> records;
                records = tx.run(query, parameters("num", num)).list();
                return records;
            });
            System.out.printf("%5s %10s %20s %10s %10s %15s", "     ", "User_Id", "Name", "Gender", "Trainer", "Followers\n");
            System.out.println("--------------------------------------------------------------------------------------------------------");
            for(int i=0; i<users.size(); i++) {
                Record r = users.get(i);
                System.out.printf("%5s %10s %20s %10s %10s %15s", (i+1)+") ", r.get("user").get("user_id").toString().replace("\"",""), r.get("user").get("name").toString().replace("\"",""),
                        r.get("user").get("gender").toString().replace("\"",""), r.get("user").get("trainer").toString().replace("\"",""),
                        r.get("num_followers").toString());
                System.out.println("\n");
            }
        };
        return returnUsers(users);
    }

    public ArrayList<User> mostRatedTrainers(int num){
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
                Double vote = Double.parseDouble(r.get("avg_vote").toString());
                BigDecimal bd = new BigDecimal(vote).setScale(3, RoundingMode.HALF_UP);
                vote = bd.doubleValue();
                System.out.printf("%5s %10s %20s %10s %10s", (i+1)+") ", r.get("user").get("user_id").toString().replace("\"",""), r.get("user").get("name").toString().replace("\"",""),
                        r.get("user").get("gender").toString().replace("\"",""), vote.toString());
                System.out.println("\n");
            }
        };
        return returnUsers(trainers);
    }

    //function for print summary information of the given routines
    public void printRoutines(ArrayList<Record> rec, int num){
        int cycle = 0;
        System.out.printf("%5s %10s %15s %15s %15s %15s", "     ", "Trainer", "Level", "Starting day", "End day","Vote\n");
        System.out.println("--------------------------------------------------------------------------------------------------------");
        for(int i=0; i<rec.size(); i++) {
            Record r = rec.get(i);
            System.out.printf("%5s", (i+1)+") ");
            Workout w = new Workout(null, null, r.get("routine").get("trainer").toString().replace("\"",""),
                    r.get("routine").get("level").toString().replace("\"",""),
                    -1, -1, -1, null, null, null,
                    r.get("routine").get("starting_day").toString().replace("\"",""),
                    r.get("routine").get("end_day").toString().replace("\"",""), null, -1,
                    Double.parseDouble(r.get("routine").get("vote").toString().replace("\"","")));
            w.print();
            cycle++;
            if(cycle == num){
                System.out.println("Insert m to see more or another key to continue...");
                Scanner sc = new Scanner(System.in);
                if(sc.next().equals("m")) cycle = 0;
                else return;
            }
        }
    }

    public ArrayList<Workout> returnRoutines(ArrayList<Record> routines){
        ArrayList<Workout> works = new ArrayList<>();
        for(Record r: routines){
            works.add(new Workout(  r.get("routine").get("_id").toString().replace("\"", ""),
                    r.get("routine").get("user").toString().replace("\"", ""),
                    r.get("routine").get("trainer").toString().replace("\"", ""),
                    r.get("routine").get("level").toString().replace("\"", ""),
                    0, 0, 0, null, null, null,
                    r.get("routine").get("starting_day").toString().replace("\"", ""),
                    r.get("routine").get("end_day").toString().replace("\"", ""),
                    null, 0,
                    Double.parseDouble(r.get("routine").get("vote").toString())));
        }
        return works;
    }

    public ArrayList<User> returnUsers(ArrayList<Record> users){
        ArrayList<User> us = new ArrayList<>();
        for(Record r: users){
            us.add(new User(    r.get("user").get("name").toString().replace("\"", ""),
                                r.get("user").get("gender").toString().replace("\"", ""),
                                r.get("user").get("birth").toString().replace("\"", ""),
                                null, null, null, null, null, null,null,
                                r.get("user").get("level").toString().replace("\"", ""),
                                r.get("user").get("trainer").toString().replace("\"", ""),
                                r.get("user").get("user_id").toString().replace("\"", "")));
        }
        return us;
    }

    //function for retry the list of followers/followed users (and select one of them)
    public ArrayList<User> showFollowUsers(String user, String option){
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
        };
        return returnUsers(users);
    }

    public ArrayList<User> showRecommended(String id) {
        ArrayList<Record> recommended;
        try (Session session = graph_driver.session()) {
            recommended = (ArrayList<Record>) session.readTransaction(tx -> {
                List<Record> persons;
                persons = tx.run("MATCH (a:User)-[:FOLLOW]->(b:User)-[:FOLLOW]->(c:User) WHERE a.user_id = $user " +
                                "AND NOT exists((a)-[:FOLLOW]->(c)) AND " +
                                "a.level = c.level " +
                                "RETURN c AS user ORDER BY RAND()",
                        parameters("user", id)).list();
                return persons;
            });
            return returnUsers(recommended);
        }
    }

    // function that show how many users gained a level in the interval specified
    public void showLvlUpBI(String start, String end){
        try (Session session = graph_driver.session()) {
            ArrayList<Record> recommended = (ArrayList<Record>) session.readTransaction(tx -> {
                List<Record> persons;
                persons = tx.run("MATCH (s:Routine)<-[:HAS_ROUTINE]-(a:User)-[:HAS_ROUTINE]->(r:Routine) " +
                                "WHERE r.starting_day >= $start AND s.end_day <= $end AND " +
                                "r.level = \"Beginner\" AND s.level = \"Intermediate\" AND " +
                                "s.starting_day > r.starting_day " +
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

    public void showLvlUpBE(String start, String end) {
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

    public ArrayList<User> showMostFidelityUsers(int num){

        String date = LocalDate.now().toString();
        try (Session session = graph_driver.session()) {
            ArrayList<Record> users = (ArrayList<Record>) session.readTransaction(tx -> {
                List<Record> persons;
                persons = tx.run("MATCH (a:User)-[:HAS_ROUTINE]->(r:Routine) " +
                                "WHERE r.end_day < $date " +
                                "RETURN a AS user, COUNT(r) AS past_routines, min(r.starting_day) AS first_routine " +
                                "ORDER BY past_routines DESC, first_routine LIMIT $num",
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
            return returnUsers(users);
        }
    }

    //function to show all the routines (past or current) of the given user
    public ArrayList<Workout> showRoutines(String user, String period){
        ArrayList<Record> routines;
        try ( Session session = graph_driver.session() ) {
            routines = (ArrayList<Record>) session.readTransaction((TransactionWork<List<Record>>) tx-> {
                List<Record> records;
                String c_day = LocalDate.now().toString();
                if(period.equals("all"))
                    records = tx.run("MATCH (a:User) -[:HAS_ROUTINE]->(b:Routine) WHERE a.user_id = $user RETURN b AS routine",parameters("user",user)).list();
                else if(period.equals("past"))
                    records = tx.run("MATCH (a:User) -[:HAS_ROUTINE]->(b:Routine) WHERE a.user_id = $user AND b.end_day < $date RETURN b AS routine",
                            parameters("user",user, "date", c_day)).list();
                else
                    records = tx.run("MATCH (a:User) -[:HAS_ROUTINE]->(b:Routine) WHERE a.user_id = $user AND b.end_day >= $date RETURN b AS routine",
                            parameters("user",user, "date", c_day)).list();
                ArrayList<Record> results = new ArrayList<>();
                return records;
            });
        }
        return returnRoutines(routines);
    }

    //function for unfollow a user (add relation in the db)
    public boolean unfollowUser(String user, String followed){
        try ( Session session = graph_driver.session() ) {
            ResultSummary rs = session.run("MATCH (a:User)-[f:FOLLOW]->(b:User) " +
                            "WHERE a.user_id = $user AND b.user_id = $followed " +
                            "DELETE f",
                        parameters("user", user, "followed", followed)).consume();
            if(rs.counters().relationshipsDeleted()==0)
                System.out.println("You don't follow this user!");
            return (rs.counters().relationshipsDeleted()==1);
        }
    }

}
