package wefit.db;
import com.mongodb.BasicDBObject;
import com.mongodb.ConnectionString;
import com.mongodb.DBObject;
import com.mongodb.MongoException;
import com.mongodb.client.*;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertOneResult;
import org.bson.Document;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Updates.*;
import static com.mongodb.client.model.Aggregates.*;
import static com.mongodb.client.model.Accumulators.sum;


import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import static com.mongodb.client.model.Projections.*;
import java.io.*;
import java.util.regex.Pattern;

import javax.print.Doc;

public class MongoDbConnector {
    static ConnectionString uri;
    static MongoClient myClient;
    static MongoDatabase db;

    static MongoCollection<Document> workout;
    static MongoCollection<Document> users;

    public MongoDbConnector(String conn, String db_name){
        uri = new ConnectionString(conn);
        myClient = MongoClients.create(uri);
        db = myClient.getDatabase(db_name);

        workout = db.getCollection("workout");
        users = db.getCollection("users");
    }

    public void changeProfile(Document user){
        try {
            DeleteResult result = users.deleteOne(eq("user_id", user.getString("user_id")));
        } catch (MongoException me) {
            System.err.println("Unable to delete due to an error: " + me);
        }
        try {
            InsertOneResult result = users.insertOne(user);
            System.out.println("Success! Your profile has been updated.");
        } catch (MongoException me) {
            System.err.println("Unable to insert due to an error: " + me);
        }
    }

    public Document getExercise(String name, String muscle, String type){
        ArrayList<Document> docs = new ArrayList<>();
        ArrayList<Bson> filters = new ArrayList<>();
        filters.add(match(regex("name", ".*"+name+".*", "i")));

        if(muscle != null)
            filters.add(match(eq("type", muscle)));
        if(type != null)
            filters.add(match(eq("muscle_targeted", type)));
        workout.aggregate(filters).into(docs);

        if(docs.size() == 0) {
            System.out.println("Exercise not found");
            return null;
        }

        for(int i=0; i<docs.size(); i++) {
            Document d = docs.get(i);
            System.out.printf("%3s %20s %15s", (i+1)+") ", d.getString("name"),d.getString("level"));
            System.out.println("\n");
        }

        String input;
        while (true) {
            System.out.println("Select the number of the exercise or 0 to return back");
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
                Document ex = docs.get(Integer.parseInt(input)-1);
                return ex;
        }

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

    public String getUser(String name){ //restituisce l'id dell'utente cercato, se più di uno stampa gli id e fa scegliere
        Bson name_filter = regex("name", ".*"+name+".*", "i");

        Document user = new Document();
        ArrayList<Document> docs = new ArrayList<>();
        users.find(name_filter).into(docs);
        if(docs.size()==0)
            return null;
        if(docs.size()==1)
            return docs.get(0).getString("user_id");
        System.out.println("\nList of users with the insert name");
        for(Document document :  docs){
            System.out.println(document.getString("user_id"));
        }
        System.out.println("\nSelect a user_id");
        Scanner sc = new Scanner(System.in);
        String id = sc.next();
        Bson id_condition = new Document("$eq", id);
        Bson id_filter = new Document("user_id", id_condition);
        for(Document document :  users.find(id_filter)){
            user = document;
        }
        return user.getString("user_id");
    }

    public void insertRoutine(Document routine){
        try {
            InsertOneResult result = workout.insertOne(routine);
            System.out.println("Success! Your routine has been inserted.");
        } catch (MongoException me) {
            System.err.println("Unable to insert due to an error: " + me);
        }
    }

    public void printEx(Document doc, String type){
        System.out.printf("%40s %20s %15s %15s %10s", "Name", "Muscle Targeted", "Equipment", "Type", "Weight\n");
        System.out.println("--------------------------------------------------------------------------------------------------------");
        for(Document d: (ArrayList<Document>)doc.get(type)) {
            System.out.printf("%40s %20s %15s %15s %10d", d.getString("name"),d.getString("muscle_targeted"),
                    d.getString("equipment"),d.getString("type"),d.getInteger("weight"));
            System.out.println();
        }
    }

    private void printRoutines(ArrayList<Document> docs) {
        System.out.printf("%3s %10s %15s %15s %15s", "   ", "Trainer", "Level", "Starting day", "End day\n");
        System.out.println("--------------------------------------------------------------------------------------------------------");
        for(int i=0; i<docs.size(); i++) {
            Document d = docs.get(i);
            System.out.printf("%3s %10s %15s %15s %15s", (i+1)+") ", d.getString("trainer"),d.getString("level"),
                    d.getString("starting_day"),d.getString("end_day"));
            System.out.println("\n");
        }
    }

    public void printUsers(ArrayList<Document> docs) {
        System.out.printf("%3s %10s %20s %10s %15s %15s %10s", "   ", "User_Id", "Name", "Gender", "Year of birth", "Level","Trainer\n");
        System.out.println("--------------------------------------------------------------------------------------------------------");
        for(int i=0; i<docs.size(); i++) {
            Document d = docs.get(i);
            System.out.printf("%3s %10s %20s %10s %15s %15s %10s", (i+1)+") ", d.getString("user_id"),d.getString("name"),
                    d.getString("gender"),d.getString("year_of_birth"),d.getString("level"),d.getString("trainer"));
            System.out.println("\n");
        }
    }

    public void searchRoutines(List<Bson> filters){
        ArrayList<Document> docs = new ArrayList<>();
        filters.add(match(ne("user",null)));
        workout.aggregate(filters).into(docs);
        if(docs.size()==0)
            System.out.println("Results not found");
        else {
            printRoutines(docs);
            selectRoutine(docs);
        }
    }

    public void searchUsers(List<Bson> filters){
        ArrayList<Document> docs = new ArrayList<>();
        users.aggregate(filters).into(docs);
        if(docs.size()==0)
            System.out.println("Results not found");
        else {
            printUsers(docs);
            selectUser(docs);
        }
    }

    public void selectRoutine(ArrayList<Document> docs){
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
                return;
            default:
                String id = docs.get(Integer.parseInt(input)-1).getObjectId("_id").toString();
                showRoutineDetails(id);
        }
    }

    public void selectUser(ArrayList<Document> docs){
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
        switch (input) {
            case "0":
                return;
            default:
                String id = docs.get(Integer.parseInt(input)-1).getString("user_id");
                showUserDetails(id);
        }
    }

    public Document signIn(String username, String password) {
        Document doc = users.find(and(eq("email",username),eq("password",password))).first();
        return doc;
    }

    public void signUp(Document user){
        try {
            InsertOneResult result = users.insertOne(user);
            System.out.println("Success! Inserted document id: " + result.getInsertedId());
        } catch (MongoException me) {
            System.err.println("Unable to insert due to an error: " + me);
        }
    }

    public void showCurrentRoutine(String user){
        String c_day = LocalDate.now().toString();
        Bson match = match(and(eq("user",user),gt("end_day",c_day)));
        Bson proj = project(fields(exclude("user","warm_up","exercises","stretching")));

        Document r = workout.aggregate(Arrays.asList(match,proj)).first();

        if(r==null)
            System.out.println("you don't have current routines");
        else {
            ArrayList<Document> docs = new ArrayList<>();
            docs.add(r);
            printRoutines(docs);
            //vedi dettagli

            System.out.println("Press 1 to select the routine\n" +
                    "or press any key to return to the main menu");

            Scanner sc = new Scanner(System.in);
            String input = sc.next();
            switch (input) {
                case "1":
                    String id = r.getObjectId("_id").toString();
                    showRoutineDetails(id);
                    return;
                default:
                    return;
            }
        }

    }

    public void showExercise(String ex){
        Document doc = workout.find(and(eq("name",ex),ne("type",null))).first();
        System.out.print("Exercise:\t"+ex+"\n");
        System.out.print("type: " + doc.getString("type")+"\t");
        System.out.print("level: " + doc.getString("level")+"\n");
        System.out.print("muscle_targeted: " + doc.getString("muscle_targeted")+"\t");
        System.out.print("equipment: " + doc.getString("equipment")+"\n");
        System.out.print("images:\n");
        for(Document d: (ArrayList<Document>)doc.get("images")) {
            System.out.print(d.getString("image")+"\n");
        }
        if(doc.getString("details")!=null)
            System.out.print("details:\n" + doc.getString("details")+"\n");

        System.out.println("Press any key to return");
        Scanner sc = new Scanner(System.in);
        String input = sc.next();
    }

    public void showPastRoutines(String user){
        String c_day = LocalDate.now().toString();
        Bson match = match(and(eq("user",user),lt("end_day",c_day)));
        Bson proj = project(exclude("user","warm_up","exercises","stretching"));

        ArrayList<Document> docs = new ArrayList<>();
        workout.aggregate(Arrays.asList(match,proj)).into(docs);
        if(docs.size()==0)
            System.out.println("you don't have past routines");
        else {
            printRoutines(docs);
            selectRoutine(docs);
        }
    }

    public void showRoutineDetails(String id){
        Bson match = match(eq("_id",new ObjectId(id)));
        Bson proj = project(fields(excludeId(), exclude("user","comments")));
        Document doc = workout.aggregate(Arrays.asList(match,proj)).first();

        while(true){
            System.out.println("--------------------------------------------------------------------------------------------------------");
            System.out.println("ROUTINE DETAILS");
            System.out.println("--------------------------------------------------------------------------------------------------------");

            System.out.print("trainer: " + doc.getString("trainer")+"\t");
            System.out.print("level: " + doc.getString("level")+"\n");
            System.out.print("starting_day: " + doc.getString("starting_day")+"\t");
            System.out.print("end_day: " + doc.getString("end_day")+"\n");
            System.out.print("work_time(sec): " + doc.getInteger("work_time(sec)")+"\t");
            System.out.print("rest_time(sec): " + doc.getInteger("rest_time(sec)")+"\n\n");

            System.out.print("WARM UP:\n");
            printEx(doc, "warm_up");
            System.out.println();

            System.out.print("EXERCISES:\tRepeat the sequence "+doc.getInteger("repeat")+" times\n");
            printEx(doc, "exercises");
            System.out.println();

            System.out.print("STRETCHING:\n");
            printEx(doc, "stretching");

            System.out.println("\nPress 1 to search an exercise\n"+
                                "Press 2 to comment the routine\n"+
                                "Press 3 to vote the routine\n"+
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
                    showExercise(exercise);
                    continue;
                }
                case "2":
                    //comment();
                    break;
                case "3":
                    //vote();
                    break;
                default: return;
            }
        }

    }

    public String showUserDetails(String id){
        Document doc = users.find(eq("user_id", id)).first();
        if(doc==null){
            System.out.println("User not found");
            return null;
        }

        while(true){
            System.out.println("USER DETAILS:\n");

            System.out.printf("%10s %20s %10s %15s %15s %10s %10s %10s", "User_Id", "Name", "Gender", "Year of birth", "Level","Trainer", "Height", "Weight\n");
            System.out.println("--------------------------------------------------------------------------------------------------------");

            System.out.printf("%10s %20s %10s %15s %15s %10s %10s %10s", doc.getString("user_id"),doc.getString("name"),
                    doc.getString("gender"),doc.getString("year_of_birth"), doc.getString("level"),
                    doc.getString("trainer"), doc.getString("height"),doc.getString("weight"));
            System.out.println("\n");

            if(doc.getString("train") != null)      System.out.println("Train:\n" + doc.getString("train")+"\n");
            if(doc.getString("background")!= null)  System.out.println("Background:\n" + doc.getString("background")+"\n");
            if(doc.getString("experience")!= null)  System.out.println("Experience:\n" + doc.getString("experience")+"\n");

            System.out.println("\nPress 1 to FOLLOW the user\n or press 2 to UNFOLLOW the user\n" +
                                "or press another key to return");
            Scanner sc = new Scanner(System.in);
            String input = sc.next();
            switch (input) {
                case "1":
                    return "follow";
                case "2":
                    return "unfollow;";
                default: return null;
            }
        }

    }



}

//Judith_Eyres718285046@liret.org
//3eYZcK8f