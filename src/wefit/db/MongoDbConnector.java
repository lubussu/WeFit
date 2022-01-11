package wefit.db;
import com.mongodb.ConnectionString;
import com.mongodb.MongoException;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
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

    public void insertComment(Document comment, String id){
        Bson filter = eq("_id", id);
        Bson change = push("comments", comment);
        workout.updateOne(filter, change);
    }

    public void insertVote(String id, int vote){
        Bson filter = eq("_id", id);
        int score, nVotes;
        String score_string = null;
        String nVotes_string = null;
        try (MongoCursor<Document> cursor = workout.find().iterator())
        {
            while (cursor.hasNext())
            {
                nVotes_string = cursor.next().getString("num_votes");
                score_string = cursor.next().getString("vote");
            }
        }

        if(score_string == null || nVotes_string == null){
            System.out.println("Couldn't add your vote...");
            return;
        }

        score = Integer.parseInt(score_string);
        nVotes = Integer.parseInt(nVotes_string);
        score = ((score*nVotes)+vote)/(nVotes+1);
        nVotes = nVotes+1;

        workout.updateOne(eq("_id", id), set("vote", score));
        workout.updateOne(eq("_id", id), set("num_votes", nVotes));
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

    public void searchRoutines(List<Bson> filters){
        ArrayList<Document> docs = new ArrayList<>();
        workout.aggregate(filters).into(docs);
        if(docs.size()==0)
            System.out.println("Results not found");
        else {
            printRoutines(docs);/*
            for (int i = 0; i < docs.size(); i++) {
                System.out.print((i + 1) + ") ");
                printRoutine(docs.get(i));
            }*/
            selectRoutine(docs);
        }
    }

    public Document searchUser(String name){
        Bson name_condition = new Document("$eq", name);
        Bson name_filter = new Document("name", name_condition);
        Document user = new Document();
        String id;
        ArrayList<Document> docs = new ArrayList<>();
        users.find(name_filter).into(docs);
        if(docs.size()==1)
            return docs.get(0);
        for(Document document :  users.find(name_filter)){
            System.out.println(document.getString("user_id"));
        }
        Scanner sc = new Scanner(System.in);
        id = sc.next();
        Bson id_condition = new Document("$eq", id);
        Bson id_filter = new Document("user_id", id_condition);
        for(Document document :  users.find(id_filter)){
            user = document;
        }
        return user;
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
            for (int i = 0; i < docs.size(); i++) {
                System.out.print((i + 1) + ") ");
                //printRoutine(docs.get(i));
            }
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

    public boolean showUserDetails(String id){
        Document doc = users.find(eq("user_id", id)).first();
        if(doc==null){
            System.out.println("User not found");
            return false;
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

            System.out.println("\nPress 1 to FOLLOW / UNFOLLOW the user\n" +
                                "or press another key to return");
            Scanner sc = new Scanner(System.in);
            String input = sc.next();
            switch (input) {
                case "1":
                    return true;
                default: return false;
            }
        }

    }



}

//Judith_Eyres718285046@liret.org
//3eYZcK8f