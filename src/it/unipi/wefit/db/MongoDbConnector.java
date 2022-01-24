package it.unipi.wefit.db;
import com.mongodb.BasicDBObject;
import com.mongodb.ConnectionString;
import com.mongodb.DBObject;
import com.mongodb.MongoException;
import com.mongodb.client.*;
import com.mongodb.client.model.Accumulators;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertOneResult;
import it.unipi.wefit.entities.User;
import org.bson.Document;

import java.awt.image.AreaAveragingScaleFilter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Sorts.descending;
import static com.mongodb.client.model.Updates.*;
import static com.mongodb.client.model.Aggregates.*;
import static com.mongodb.client.model.Accumulators.sum;


import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import it.unipi.wefit.entities.*;

import static com.mongodb.client.model.Projections.*;
import java.io.*;
import java.util.regex.Pattern;

import javax.print.Doc;

public class MongoDbConnector {
    private ConnectionString uri;
    private MongoClient myClient;
    private MongoDatabase db;

    private MongoCollection<Document> workout;
    private MongoCollection<Document> users;

    private User user;

    public MongoDbConnector(String conn, String db_name){
        uri = new ConnectionString(conn);
        myClient = MongoClients.create(uri);
        db = myClient.getDatabase(db_name);

        workout = db.getCollection("workout");
        users = db.getCollection("users");
    }

    //function for change profile's properties in the db
    public void changeProfile(User user){
        try {
            DeleteResult result = users.deleteOne(eq("user_id", user.getUser_id()));
        } catch (MongoException me) {
            System.err.println("Unable to delete due to an error: " + me);
        }
        try {
            InsertOneResult result = users.insertOne(user.toDocument());
            System.out.println("Success! The profile has been updated.");
        } catch (MongoException me) {
            System.err.println("Unable to insert due to an error: " + me);
        }
    }

    public void deleteComment(String comment, String routine){
        Bson filter = eq("_id", new ObjectId(routine));
        Bson change = pull("comments_id", comment);
        workout.updateOne(filter, change);
        System.out.println("Success! Your comment has been deleted.");
    }

    public Bson getFilter(String field, String value, String option){

        switch(option){
            case "eq":
                return (match(regex(field, ".*"+value+".*", "i")));
            case "lt":
                return (match(lt(field,value)));
            case "lte":
                return (match(lte(field,value)));
            case "gt":
                return (match(gt(field,value)));
            case "gte":
                return (match(gte(field,value)));
        }
        return null;
    }

    public Bson getFilter(String field, int value, String option){
        switch(option){
            case "eq":
                return (match(eq(field,value)));
            case "lt":
                return (match(lt(field,value)));
            case "lte":
                return (match(lte(field,value)));
            case "gt":
                return (match(gt(field,value)));
            case "gte":
                return (match(gte(field,value)));
        }
        return null;
    }

    //function to get a user by the name
    public Document getUser(String search){ //restituisce l'id dell'utente cercato, se più di uno stampa gli id e fa scegliere
        Bson filter;
        if(!search.matches("[0-9.]+"))
            filter = regex("name", ".*"+search+".*", "i");
        else
            filter = match(eq("user_id",search));

        Document user = new Document();
        ArrayList<Document> docs = new ArrayList<>();
        users.find(filter).into(docs);
        if(docs.size()==0)
            return null;
        if(docs.size()==1)
            return docs.get(0);
        System.out.println("\nList of users with the insert name");
        for(Document document :  docs){
            System.out.println(document.getString("user_id")+"\t"+document.getString("name"));
        }
        System.out.println("\nSelect a user_id");
        Scanner sc = new Scanner(System.in);
        String id = sc.next();
        Bson id_condition = new Document("$eq", id);
        Bson id_filter = new Document("user_id", id_condition);
        for(Document document :  users.find(id_filter)){
            user = document;
        }
        return user;
    }

    //function to insert a comment in the db
    public void insertComment(Comment comment, String id){
        Bson filter = eq("_id", new ObjectId(id));
        Document com = comment.toDocument().append("_id", new ObjectId());
        Bson change = push("comments", com);
        workout.updateOne(filter, change);
        System.out.println("Success! Your comment has been inserted.");
    }

    //function to insert a new exercise in the db
    public void insertNewExercise(Exercise exercise){
        try {
            InsertOneResult result = workout.insertOne(exercise.toDocument());
            System.out.println("Success! Your new exercise has been inserted.");
        } catch (MongoException me) {
            System.err.println("Unable to insert due to an error: " + me);
        }
    }

    //function to insert a routine in the db
    public String insertRoutine(Workout routine){
        try {
            InsertOneResult result = workout.insertOne(routine.toDocument());
            System.out.println("Success! Your routine has been inserted.");
            return result.getInsertedId().toString();
        } catch (MongoException me) {
            System.err.println("Unable to insert due to an error: " + me);
        }
        return null;
    }

    //function to insert a vote in the db
    public void insertVote(String routine_id, int vote){
        double score;
        int nVotes;
        Document routine = workout.find(eq("_id", new ObjectId(routine_id))).first();

        score = routine.getDouble("vote");
        nVotes = routine.getInteger("num_votes");

        BigDecimal bd = new BigDecimal(((score*nVotes)+vote)/(nVotes+1)).setScale(2, RoundingMode.HALF_UP);
        nVotes = nVotes+1;
        score = bd.doubleValue();

        System.out.println(nVotes + " " + score);

        workout.updateOne(eq("_id", new ObjectId(routine_id)), set("vote", score));
        workout.updateOne(eq("_id", new ObjectId(routine_id)), set("num_votes", nVotes));
        System.out.println("Success! Your comment has been inserted.");
    }

    //function to get the user_id of the last user (to insert a new user)
    public int lastUser(){
        int last_user =  users.find(ne("last_user", null)).first().getInteger("last_user");
        last_user ++;
        users.updateOne(ne("last_user", null), set("last_user", last_user));
        return last_user;
    }

    public void mostUsedEquipment(String muscle){

        ArrayList<Bson> filters = new ArrayList<>();

        Bson unwind = unwind("$exercises");
        filters.add(unwind);
        if(muscle != null){
            Bson match_muscle = match(eq("exercises.muscle_targeted",muscle));
            filters.add(match_muscle);
        }
        else
            muscle = "ALL";

        Bson group = group("$exercises.equipment", sum("count", 1));
        Bson match = match(ne("_id","Body Only"));
        Bson sort = sort(descending("count"));
        Bson limit = limit(1);
        filters.add(group);
        filters.add(match);
        filters.add(sort);
        filters.add(limit);

        Document aggregation = workout.aggregate(filters).first();

        System.out.printf("%15s %15s %10s", muscle, aggregation.getString("_id"), aggregation.getInteger("count").toString());
        System.out.println("\n");
    }

    public void mostVotedPresentExercises(int max_vote, int max_ex){
        Bson order_vote = sort(descending("vote"));
        Bson limit_vote = limit(max_vote);
        Bson unwind = unwind("$exercises");
        Bson group = group("$exercises.name", sum("count",1));
        Bson order_ex = sort(descending("count"));
        Bson limit_ex = limit(max_ex);

        List<Bson> most_pipeline = Arrays.asList(order_vote, limit_vote, unwind, group, order_ex, limit_ex);
        ArrayList<Document> most_result = new ArrayList<>();
        workout.aggregate(most_pipeline).into(most_result);

        System.out.printf("%55s %10s", "EXERCISE", "COUNT");
        System.out.println();
        System.out.println("---------------------------------------------------------------------------");
        for( Document doc : most_result){
            System.out.printf("%55s %10s", doc.getString("_id"), doc.getInteger("count").toString());
            System.out.println();
        }
        System.out.println();
    }

    //function for print summary information of the given exercises
    public void printExercises(ArrayList<Document> docs, int num){
        int cycle = 0;
        System.out.printf("%5s %50s %20s %15s %15s", "     ", "Name", "Muscle Targeted", "Equipment", "Type\n");
        System.out.println("------------------------------------------------------------------------------------------------------------");
        for(int i=0;i<docs.size();i++) {
            System.out.printf("%5s", (i+1)+") ");
            Exercise e = new Exercise(docs.get(i),true);
            e.print();
            cycle++;
            if(cycle == num){
                System.out.println("Insert m to see more or another key to continue...");
                Scanner sc = new Scanner(System.in);
                if(sc.next().equals("m")) cycle = 0;
                else return;
            }
        }
    }

    //function for print summary information of the given routines
    private void printRoutines(ArrayList<Document> docs, int num) {
        int cycle = 0;
        System.out.printf("%5s %10s %15s %15s %15s %15s", "     ", "Trainer", "Level", "Starting day", "End day","Vote\n");
        System.out.println("------------------------------------------------------------------------------------------------------------");
        for(int i=0; i<docs.size(); i++) {
            System.out.printf("%5s", (i+1)+") ");
            Workout w = new Workout(docs.get(i));
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

    //function for print summary information of the given users
    public void printUsers(ArrayList<Document> docs, int num) {
        int cycle = 0;
        System.out.printf("%5s %10s %20s %10s %15s %15s %10s", "     ", "User_Id", "Name", "Gender", "Year of birth", "Level","Trainer\n");
        System.out.println("--------------------------------------------------------------------------------------------------------");
        for(int i=0; i<docs.size(); i++) {
            System.out.printf("%5s", (i+1)+") ");
            User u = new User(docs.get(i));
            u.print();
            cycle++;
            if(cycle == num){
                System.out.println("Insert m to see more or another key to continue...");
                Scanner sc = new Scanner(System.in);
                if(sc.next().equals("m")) cycle = 0;
                else return;
            }
        }
    }

    //function for search exercise(s) using the given filters
    public Document searchExercises(String ex, boolean print, String muscle, String type){
        ArrayList<Bson> filters = new ArrayList<>();
        filters.add(match(regex("name", ".*"+ex+".*", "i")));

        if(muscle != null)
            filters.add(match(eq("muscle_targeted", muscle)));

        if(type != null)
            filters.add(match(eq("type", type)));
        else
            filters.add(match(ne("type",null)));

        ArrayList<Document> docs = new ArrayList<>();
        workout.aggregate(filters).into(docs);
        if(docs.size()==0){
            System.out.println("Exercise not found\nPress any key to continue..");
            Scanner sc = new Scanner(System.in);
            sc.next();
            return null;
        }

        printExercises(docs, 10);
        return selectExercise(docs, print);
    }

    //function for search routine(s) using the given filters
    public String searchRoutines(List<Bson> filters){
        ArrayList<Document> docs = new ArrayList<>();
        filters.add(match(ne("user",null)));
        workout.aggregate(filters).into(docs);
        if(docs.size()==0)
            System.out.println("Results not found");
        else {
            printRoutines(docs, 10);
            return selectRoutine(docs);
        }
        return null;
    }

    //function for search user(s) using the given filters
    public String searchUsers(List<Bson> filters){
        ArrayList<Document> docs = new ArrayList<>();
        users.aggregate(filters).into(docs);
        if(docs.size()==0)
            System.out.println("Results not found");
        else {
            printUsers(docs, 10);
            return selectUser(docs);
        }
        return null;
    }

    //function to select an exercise from given exercises
    public Document selectExercise(ArrayList<Document> docs, boolean print){
        String input;
        while (true) {
            System.out.println("\nPress the number of the exercise you want to select\n" +
                    "or press 0 to return");

            Scanner sc = new Scanner(System.in);
            input = sc.next();
            if (!input.matches("[0-9.]+"))
                System.out.println("Please select an existing option!");
            else if ((Integer.parseInt(input)) > docs.size())
                System.out.println("Please select an existing option!\n");
            else
                break;
        }
        switch (input) {
            case "0":
                return null;
            default:
                if(print==true)
                    showExercisesDetails(docs.get(Integer.parseInt(input)-1));
                return docs.get(Integer.parseInt(input)-1);
        }
    }

    //function to select a routine from given routines
    public String selectRoutine(ArrayList<Document> docs){
        String input;
        while (true) {
            System.out.println("Press the number of the routine you want to select\n" +
                    "or press 0 to return to the main menu");

            Scanner sc = new Scanner(System.in);
            input = sc.next();
            if (!input.matches("[0-9.]+"))
                System.out.println("Please select an existing option!");
            else if ((Integer.parseInt(input)) > docs.size())
                System.out.println("Please select an existing option!\n");
            else
                break;
        }
        switch (input) {
            case "0":
                return null;
            default:
                String id = docs.get(Integer.parseInt(input)-1).getObjectId("_id").toString();
                return showRoutineDetails(id);
        }
    }

    //function to select a user from given users
    public String selectUser(ArrayList<Document> docs) {
        String input;
        while (true) {
            System.out.println("Press the number of the user you want to select\n" +
                    "or press 0 to return to the main menu");

            Scanner sc = new Scanner(System.in);
            input = sc.next();
            if (!input.matches("[0-9.]+"))
                System.out.println("Please select an existing option!");
            else if ((Integer.parseInt(input)) > docs.size())
                System.out.println("Please select an existing option!\n");
            else
                break;
        }

        System.out.println("Press 1 to see user's details\n" +
                "press 2 to follow the user\n" +
                "press 3 to unfollow the user");

        String id = docs.get(Integer.parseInt(input)-1).getString("user_id");
        Scanner sc = new Scanner(System.in);
        input = sc.next();
        switch (input) {
            case "1":
                return "d:" + id.replace("\"", "");
            case "2":
                return "f:" + id.replace("\"", ""); //follow user
            case "3":
                return "u:" + id.replace("\"", ""); //unfollow user
        }
        return null;
    }

    public void setUser(User u){
        user = u;
    }

    //function for signIn in the app (search credentials in the db)
    public Document signIn(String username, String password) {
        Document doc = users.find(and(eq("email",username),eq("password",password))).first();
        return doc;
    }

    //function for signUp in the app (insert new user in the db)
    public void signUp(User us){
        Document user = us.toDocument();
        try {
            InsertOneResult result = users.insertOne(user);
            System.out.println("Success! Inserted document id: " + result.getInsertedId());
        } catch (MongoException me) {
            System.err.println("Unable to insert due to an error: " + me);
        }
    }

    public void showAvgAgeLvl(String threshold){
        Bson match = match(eq("trainer", "no"));
        Bson group = group("$level", Accumulators.avg("Avg", eq("$toInt", "$year_of_birth")));
        System.out.format("%20s %20s", "LEVEL", "AVERAGE AGE");
        System.out.println("\n---------------------------------------------------------------------");
        users.aggregate(Arrays.asList(match, group)).forEach(document -> {
            System.out.format("%20s %20d", document.getString("_id"),
                    (LocalDate.now().getYear() - Math.round(document.getDouble("Avg"))));
            System.out.println();
        });


        match = match(and(eq("trainer","no"),gte("year_of_birth", threshold)));
        group = group("$level", sum("count", 1));
        Bson sort = sort(descending("count"));
        Bson limit = limit(1);

        Document youngers = users.aggregate(Arrays.asList(match, group, sort,limit)).first();


        match = match(and(eq("trainer","no"),lt("year_of_birth", threshold)));
        Document olders = users.aggregate(Arrays.asList(match, group, sort,limit)).first();

        System.out.println("\nThese are the level with most users older/younger than the given age:\n");
        System.out.format("%30s %30s", "YOUNGERS", "OLDERS");
        System.out.println("\n---------------------------------------------------------------------");
        System.out.format("%20s %10s %20s %10s", "LEVEL", "COUNT", "LEVEL", "COUNT");
        System.out.println();
        if(youngers != null) {
            System.out.format("%20s %10d ", youngers.getString("_id"), youngers.getInteger("count"));
        }
        if(olders != null) {
            System.out.format("%20s %10d", olders.getString("_id"), olders.getInteger("count"));
        }
        System.out.println();
    }

    public String showComments(String routine){
        ArrayList<Document> comments = (ArrayList<Document>) workout.find(eq("_id", new ObjectId(routine))).first().get("comments");
        System.out.println("-------------------------------------------------------------------------------------------------------------------------------");
        if(comments.size()==0){
            System.out.println("The routine has not any comment yet\nPress any key to continue..");
            Scanner sc = new Scanner(System.in);
            sc.next();
            return null;
        }
        for(int i=0; i<comments.size(); i++){
            Document c = comments.get(i);
            System.out.println((i+1)+")");
            System.out.println("User: "+ c.getString("user"));
            System.out.println("Time: "+ c.getString("Time"));
            System.out.println("Comment: "+ c.getString("Comment"));
            System.out.println("-------------------------------------------------------------------------------------------------------------------------------");
        }

        String input;
        while (true) {
            System.out.println("If you want to delete your comment press the number of the comment\n" +
                    "or press 0 to return to the main menu");

            Scanner sc = new Scanner(System.in);
            input = sc.next();
            if(input.equals("0"))
                return null;
            else if (!input.matches("[0-9.]+"))
                System.out.println("Please select an existing option!\n");
            else if ((Integer.parseInt(input)) > comments.size())
                System.out.println("Please select an existing option!\n");

            int j = Integer.parseInt(input)-1;
            if(!user.getUser_id().equals(comments.get(j).getString("user")))
                System.out.println("You can't delete this comment!\n");
            else
                return comments.get(j).getObjectId("_id").toString();
        }
    }

    //function for print all information of the given exercise
    public Document showExercisesDetails(Document doc){
        Exercise e = new Exercise(doc, false);
        e.printDetails();
        //ex.printDetails();

        System.out.println("\nPress any key to return");
        Scanner sc = new Scanner(System.in);
        sc.next();
        return doc;
    }

    //function for print all information of the given routine
    public String showRoutineDetails(String id){
        Bson match = match(eq("_id",new ObjectId(id)));
        Bson proj = project(fields(excludeId(), exclude("user","comments")));
        Document doc = workout.aggregate(Arrays.asList(match,proj)).first();

        while(true){
            Workout w = new Workout(doc);
            w.printDetails();
            //workout.printDetails();

            System.out.println("\nPress 1 to search an exercise\n"+
                    "Press 2 to comment the routine\n"+
                    "Press 3 to vote the routine\n"+
                    "Press 4 to see routine's comments\n"+
                    "Or press another key to return");
            Scanner sc = new Scanner(System.in);
            String input = sc.next();
            switch (input) {
                case "1": {
                    System.out.println("Insert the exercise name");
                    BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
                    String exercise="";
                    try {
                        exercise = bufferRead.readLine();
                    } catch (IOException e) { e.printStackTrace();}
                    searchExercises(exercise, true,null, null);
                    continue;
                }
                case "2":
                    return "c:"+id;
                case "3":
                    return "v:"+id;
                case "4":
                    String ret = showComments(id);
                    if(ret != null)
                        return "d:"+ret;
                    else
                        return null;
                default: return null;
            }
        }
    }

    //function for print all information of the given user
    public String showUserDetails(String id){
        Document doc = users.find(eq("user_id", id)).first();
        if(doc==null){
            System.out.println("User not found");
            return null;
        }

        while(true){
            User u = new User(doc);
            u.printDetails();

            System.out.println("\nPress 1 to FOLLOW the user\n or press 2 to UNFOLLOW the user\n" +
                    "or press another key to return");
            Scanner sc = new Scanner(System.in);
            String input = sc.next();
            switch (input) {
                case "1":
                    return "f:"+id;
                case "2":
                    return "u:"+id;
                default: return null;
            }
        }

    }
}
