package wefit.db;
import com.mongodb.ConnectionString;
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

    private void printRoutine(Document doc) {
        System.out.print("trainer: " + doc.getString("trainer")+"\t\t");
        System.out.print("level: " + doc.getString("level")+"\n");
        System.out.print("starting_day: " + doc.getString("starting_day")+"\t");
        System.out.print("end_day: " + doc.getString("end_day")+"\n");
        System.out.println("___________________________________________________________________________");
    }

    public void printEx(Document doc, String type){
        System.out.printf("%40s %20s %15s %15s %10s", "Name", "Muscle Targeted", "Equipment", "Type", "Weight\n");
        System.out.println("--------------------------------------------------------------------------------------------------------");
        for(Document d: (ArrayList<Document>)doc.get(type)) {
            System.out.printf("%40s %20s %15s %15s %10d", d.getString("name"),d.getString("muscle_targeted"),
                    d.getString("equipment"),d.getString("type"),d.getInteger("weight"));
            System.out.println();
            /*
            System.out.print("name: " + d.getString("name")+"\t\t");
            System.out.print("muscle_targeted: " + d.getString("muscle_targeted")+"\t\t");
            System.out.print("equipment: " + d.getString("equipment")+"\t\t");
            System.out.print("type: " + d.getString("type\t\t"));
            if(d.getInteger("weight")!=null)
                System.out.printf("%5s", "Weight");
            System.out.print("\n");*/
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

    public void signUp(Document user){
        try {
            InsertOneResult result = users.insertOne(user);
            System.out.println("Success! Inserted document id: " + result.getInsertedId());
        } catch (MongoException me) {
            System.err.println("Unable to insert due to an error: " + me);
        }
    }
    public Document signIn(String username, String password) {
        Document doc = users.find(and(eq("email",username),eq("password",password))).first();
        return doc;
    }

    public void changeProfile(Document user){
        try {
            DeleteResult result = users.deleteOne(eq("athlete_id", user.getString("athlete_id")));
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

    public Document searchUser(String name){
        Bson name_condition = new Document("$eq", name);
        Bson name_filter = new Document("name", name_condition);
        Document user = new Document();
        String id;
        for(Document document :  users.find(name_filter)){
            System.out.println(document.getString("athlete_id"));
        }
        Scanner sc = new Scanner(System.in);
        id = sc.next();
        Bson id_condition = new Document("$eq", id);
        Bson id_filter = new Document("athlete_id", id_condition);
        for(Document document :  users.find(id_filter)){
            user = document;
        }
        return user;
    }

    public void showCurrentRoutine(String user){
        String c_day = LocalDate.now().toString();
        Bson match = match(and(eq("user",user),gt("end_day",c_day)));
        Bson proj = project(fields(exclude("user","warm_up","exercises","stretching")));

        Document r = workout.aggregate(Arrays.asList(match,proj)).first();

        if(r==null)
            System.out.println("you don't have current routines");
        else {
            printRoutine(r);
            //vedi dettagli

            System.out.println("Press 0 to select the routine\n" +
                    "or press any key to return to the main menu");

            Scanner sc = new Scanner(System.in);
            String input = sc.next();
            switch (input) {
                case "0":
                    String id = r.getObjectId("_id").toString();
                    showDetails(id);
                    return;
                default:
                    return;
            }
        }

    }
    public void showPastRoutine(String user){
        String c_day = LocalDate.now().toString();
        Bson match = match(and(eq("user",user),lt("end_day",c_day)));
        Bson proj = project(exclude("user","warm_up","exercises","stretching"));

        ArrayList<Document> docs = new ArrayList<>();
        workout.aggregate(Arrays.asList(match,proj)).into(docs);
        if(docs.size()==0)
            System.out.println("you don't have past routines");
        else {
            for (int i = 0; i < docs.size(); i++) {
                System.out.print((i + 1) + ") ");
                printRoutine(docs.get(i));
            }

            String input;
            while (true) {
                System.out.println("Press the number of the routine you want to select\n" +
                        "or press 0 to return to the main menu");

                Scanner sc = new Scanner(System.in);
                input = sc.next();
                if (!input.matches("[0-9.]+"))
                    System.out.println("Please select an existing option!");
                else if ((Integer.parseInt(input)) > docs.size())
                    System.out.println("Please select an existing option!");
                else
                    break;
            }
            switch (input) {
                case "0":
                    return;
                default:
                    String id = docs.get(Integer.parseInt(input)-1).getObjectId("_id").toString();
                    showDetails(id);
            }
        }
    }
    public void showDetails(String id){
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

            System.out.println("\nPress 1 to search an exercise\nOr any other key to return");
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
                default: return;
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

}

//Judith_Eyres718285046@liret.org
//3eYZcK8f