package it.unipi.wefit.db;

import com.mongodb.ConnectionString;
import com.mongodb.MongoException;
import com.mongodb.client.*;
import com.mongodb.client.model.Accumulators;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.result.UpdateResult;
import it.unipi.wefit.entities.User;
import org.bson.Document;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Sorts.ascending;
import static com.mongodb.client.model.Sorts.descending;
import static com.mongodb.client.model.Updates.*;
import static com.mongodb.client.model.Aggregates.*;
import static com.mongodb.client.model.Accumulators.sum;


import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import it.unipi.wefit.entities.*;


public class MongoDbConnector {
    private final MongoCollection<Document> workout;
    private final MongoCollection<Document> users;
    private final MongoCollection<Document> exercises;

    public MongoDbConnector(String conn, String db_name){
        ConnectionString uri = new ConnectionString(conn);
        MongoClient myClient = MongoClients.create(uri);
        MongoDatabase db = myClient.getDatabase(db_name);

        workout = db.getCollection("workout");
        users = db.getCollection("users");
        exercises = db.getCollection("exercises");
    }

    //function for change profile's properties in the db
    public boolean changeProfile(User user){
        ArrayList<Bson> updates = new ArrayList<>();
        updates.add(set("name", user.getName()));
        updates.add(set("gender", user.getGender()));
        updates.add(set("year_of_birth", user.getYear_of_birth()));
        updates.add(set("height", user.getHeight()));
        updates.add(set("weight", user.getWeight()));
        updates.add(set("train", user.getTrain()));
        updates.add(set("background", user.getBackground()));
        updates.add(set("experience", user.getExperience()));
        updates.add(set("email", user.getEmail()));
        updates.add(set("password", user.getPassword()));

        UpdateResult result = users.updateOne(eq("user_id", user.getUser_id()),updates);
        return (result.getModifiedCount()==1);
    }

    public boolean deleteComment(String comment_id, String routine){
        Bson filter = eq("_id", new ObjectId(routine));
        Bson change = pull("comments", eq("_id",new ObjectId(comment_id)));
        UpdateResult result = workout.updateOne(filter, change);
        return (result.getModifiedCount()==1);
    }

    public boolean deleteRoutine(Workout w){
        Bson filter = eq("_id", new ObjectId(w.getId()));
        DeleteResult result = workout.deleteOne(filter);
        return result.getDeletedCount()==1;
    }

    public boolean deleteUser(User user){
        Bson filter = eq("user_id", user.getUser_id());
        DeleteResult result = users.deleteOne(filter);

        return result.getDeletedCount()==1;
    }

    public Bson getFilter(String field, String value, String option){

        return switch (option) {
            case "eq" -> (match(regex(field, ".*" + value + ".*", "i")));
            case "lt" -> (match(lt(field, value)));
            case "lte" -> (match(lte(field, value)));
            case "gt" -> (match(gt(field, value)));
            case "gte" -> (match(gte(field, value)));
            default -> null;
        };
    }

    public Bson getFilter(String field, int value, String option){
        return switch (option) {
            case "eq" -> (match(eq(field, value)));
            case "lt" -> (match(lt(field, value)));
            case "lte" -> (match(lte(field, value)));
            case "gt" -> (match(gt(field, value)));
            case "gte" -> (match(gte(field, value)));
            default -> null;
        };
    }

    //function to get a user by the name
    public Document getRoutine(String id){ //restituisce l'id dell'utente cercato, se più di uno stampa gli id e fa scegliere
        Document routine = workout.find(eq("_id", new ObjectId(id))).first();
        if(routine == null) {
            System.out.println("\nRoutine not found");
            return null;
        }
        return routine;
    }

    //function to get a user by the name
    public Document getUser(String search){ //restituisce l'id dell'utente cercato, se più di uno stampa gli id e fa scegliere
        Bson filter;
        if(!search.matches("[0-9.]+"))
            filter = regex("name", ".*"+search+".*", "i");
        else
            filter = eq("user_id",search);

        Document user = new Document();
        ArrayList<Document> docs = new ArrayList<>();
        users.find(filter).into(docs);
        if(docs.size()==0)
            return null;
        if(docs.size()==1)
            return docs.get(0);
        System.out.println("\nList of users with the insert name");
        int counter = 0;
        Scanner next = new Scanner(System.in);
        String go = "";
        for(Document document :  docs){
            System.out.println(document.getString("user_id")+"\t"+document.getString("name"));
            counter++;
            if(counter == 9){
                System.out.println("Insert m to see more or another key to continue...");
                go = next.next();
                if(go.equals("m")) {
                    counter = 0;
                    continue;
                }
                else break;
            }
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
    public boolean insertComment(Comment comment, String id){
        Bson filter = eq("_id", new ObjectId(id));
        Document com = comment.toDocument();
        if(com.getObjectId("id") == null)
            com.append("_id", new ObjectId());
        Bson change = push("comments", com);
        UpdateResult result = workout.updateOne(filter, change);
        return (result.getModifiedCount()==1);
    }

    //function to insert a new exercise in the db
    public void insertNewExercise(Exercise exercise){
        try {
            exercises.insertOne(exercise.toDocument());
            System.out.println("Success! Your new exercise has been inserted.");
        } catch (MongoException me) {
            System.err.println("Unable to insert due to an error: " + me);
        }
    }

    //function to insert a routine in the db
    public String insertRoutine(Workout routine){
        try {
            InsertOneResult result = workout.insertOne(routine.toDocument());
            if(result.getInsertedId() != null)
                return result.getInsertedId().asObjectId().getValue().toString();
        } catch (MongoException me) {
            return null;
        }
        return null;
    }

    //function for signUp in the app (insert new user in the db)
    public boolean insertUser(User us){
        Document user = us.toDocument();
        try {
            InsertOneResult result = users.insertOne(user);
            return (result.getInsertedId() != null);
        } catch (MongoException me) {
            return false;
        }
    }

    //function to insert a vote in the db
    public boolean insertVote(String routine_id, int vote){
        double score;
        int nVotes;
        Document routine = workout.find(eq("_id", new ObjectId(routine_id))).first();
        if(routine==null)
            return false;

        score = routine.getDouble("vote");
        nVotes = routine.getInteger("num_votes");

        BigDecimal bd = BigDecimal.valueOf(((score * nVotes) + vote) / (nVotes + 1)).setScale(2, RoundingMode.HALF_UP);
        nVotes = nVotes+1;
        score = bd.doubleValue();

        ArrayList<Bson> updates = new ArrayList<>();
        updates.add(set("vote", score));
        updates.add(set("num_votes", nVotes));
        UpdateResult result = workout.updateOne(eq("_id", new ObjectId(routine_id)), updates);
        return (result.getModifiedCount()==1);
    }

    //function to get the user_id of the last user (to insert a new user)
    public int lastUser(){
        Document user =  users.find(ne("last_user", null)).first();
        if(user==null)
            return -1;
        int last_user = user.getInteger("last_user");
        last_user ++;
        users.updateOne(ne("last_user", null), set("last_user", last_user));
        return last_user;
    }

    //function that find the most used equipment in general or for the selected muscle group
    public void mostUsedEquipment(String muscle){

        ArrayList<Bson> filters = new ArrayList<>();
        Bson unwind = unwind("$exercises");
        filters.add(unwind);
        if(muscle != null){
            Bson match_muscle = match(eq("exercises.muscle_targeted",muscle));
            filters.add(match_muscle);
        } else { muscle = "ALL"; }

        Bson group = group("$exercises.equipment", sum("count", 1));
        Bson match = match(ne("_id","Body Only"));
        Bson sort = sort(descending("count"));
        Bson limit = limit(1);
        filters.add(group); filters.add(match);
        filters.add(sort);  filters.add(limit);

        Document aggregation = workout.aggregate(filters).first();
        if(aggregation == null) {
            System.out.println("Result not found");
            return;
        }

        System.out.printf("%15s %15s %10s", muscle, aggregation.getString("_id"), aggregation.getInteger("count").toString());
        System.out.println("\n");
    }

    //function for show the most present exercise in the top-n rated routines
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

    //function for search exercise(s) using the given filters
    public ArrayList<Exercise> searchExercises(String ex, String muscle, String type){
        ArrayList<Bson> filters = new ArrayList<>();
        filters.add(match(regex("name", ".*"+ex+".*", "i")));

        if(muscle != null)
            filters.add(match(eq("muscle_targeted", muscle)));

        if(type != null)
            filters.add(match(eq("type", type)));
        else
            filters.add(match(ne("type",null)));

        ArrayList<Document> docs = new ArrayList<>();
        ArrayList<Exercise> exs = new ArrayList<>();
        exercises.aggregate(filters).into(docs);
        if(docs.size()==0){
            System.out.println("Exercise not found\nPress any key to continue..");
            Scanner sc = new Scanner(System.in);
            sc.next();
            return null;
        }
        for(Document d: docs)
            exs.add(new Exercise(d, false));

        return exs;
    }

    //function for search routine(s) using the given filters
    public ArrayList<Workout> searchRoutines(List<Bson> filters){
        ArrayList<Document> docs = new ArrayList<>();
        ArrayList<Workout> works = new ArrayList<>();
        filters.add(match(ne("user",null)));
        filters.add(sort(ascending("_id")));
        workout.aggregate(filters).into(docs);
        if(docs.size()==0)
            System.out.println("Results not found");
        else {
            for(Document d: docs)
                works.add(new Workout(d));
        }
        return works;
    }

    //function for search user(s) using the given filters
    public ArrayList<User> searchUsers(List<Bson> filters){
        ArrayList<Document> docs = new ArrayList<>();
        ArrayList<User> us = new ArrayList<>();
        users.aggregate(filters).into(docs);
        if(docs.size()==0)
            System.out.println("Results not found");
        else {
            for(Document d: docs)
                us.add(new User(d));
        }
        return us;
    }

    //function for signIn in the app (search credentials in the db)
    public Document signIn(String username, String password) {
        return users.find(and(eq("email",username),eq("password",password))).first();
    }

    //function to show the average age per level
    public void showAvgAgeLvl(String threshold){
        Bson match = match(and(eq("trainer","no"),gte("year_of_birth", threshold)));
        Bson group = group("$level", sum("count", 1));
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
        System.out.println("\n\n");

        match = match(eq("trainer", "no"));
        group = group("$level", Accumulators.avg("Avg", eq("$toInt", "$year_of_birth")));
        System.out.format("%20s %20s", "LEVEL", "AVERAGE AGE");
        System.out.println("\n---------------------------------------------------------------------");
        users.aggregate(Arrays.asList(match, group)).forEach(document -> {
            System.out.format("%20s %20d", document.getString("_id"),
                    (LocalDate.now().getYear() - Math.round(document.getDouble("Avg"))));
            System.out.println();
        });
    }

    //functio to return an ArrayList<Comment> of the given routine
    public ArrayList<Comment> showComments(String routine){
        Document r = workout.find(eq("_id", new ObjectId(routine))).first();
        ArrayList<Comment> comms = new ArrayList<>();
        ArrayList<Document> comments;
        if(r == null)
            return comms;
        comments = (ArrayList<Document>) r.get("comments");

        System.out.println("-------------------------------------------------------------------------------------------------------------------------------");
        if(comments == null || comments.size()==0){
            System.out.println("The routine has not any comment yet\nPress any key to continue..");
            Scanner sc = new Scanner(System.in);
            sc.next();
            return comms;
        }
        for(Document d: comments)
            comms.add(new Comment(d));
        return comms;
    }
}
