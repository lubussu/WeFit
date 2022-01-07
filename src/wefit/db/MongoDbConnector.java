package wefit.db;
import com.mongodb.ConnectionString;
import com.mongodb.MongoException;
import com.mongodb.client.*;
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

    private Consumer<Document> printDocuments() {
        return doc -> System.out.println(doc.toJson());
    }
    private Consumer<Document> printRoutine() {
        return doc -> {
            System.out.print("trainer: " + doc.getString("trainer")+"\t");
            System.out.print("level: " + doc.getString("level")+"\n");
            System.out.print("starting_day: " + doc.getString("starting_day")+"\t");
            System.out.print("end_day: " + doc.getString("end_day")+"\n");
            System.out.println("___________________________________________________________________________");
        };
    }
    private void printRoutine(Document doc) {
        System.out.print("trainer: " + doc.getString("trainer")+"\t");
        System.out.print("level: " + doc.getString("level")+"\t");
        System.out.print("work: " + doc.getString("level")+"\t");
        System.out.print("starting_day: " + doc.getString("starting_day")+"\n");
        System.out.print("end_day: " + doc.getString("end_day")+"\n");
        System.out.println("___________________________________________________________________________");
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

        System.out.print("ROUTINE DETAILS\n\n");

        System.out.print("trainer: " + doc.getString("trainer")+"\t");
        System.out.print("level: " + doc.getString("level")+"\n");
        System.out.print("starting_day: " + doc.getString("starting_day")+"\t");
        System.out.print("end_day: " + doc.getString("end_day")+"\n");
        System.out.print("work_time(sec): " + doc.getInteger("work_time(sec)")+"\t");
        System.out.print("rest_time(sec): " + doc.getInteger("rest_time(sec)")+"\n\n");


        System.out.print("WARM UP:\n");
        for(Document d: (ArrayList<Document>)doc.get("warm_up")) {
            System.out.print("name: " + d.getString("name")+"\t");
            System.out.print("muscle_targeted: " + d.getString("muscle_targeted")+"\t");
            System.out.print("equipment: " + d.getString("equipment")+"\t");
            System.out.print("type: " + d.getString("type")+"\n");
        }
        System.out.println();
        System.out.print("EXERCISES:\tRepeat the sequence "+doc.getInteger("repeat")+" times\n");
        for(Document d: (ArrayList<Document>)doc.get("exercises")) {
            System.out.print("name: " + d.getString("name")+"\t");
            System.out.print("muscle_targeted: " + d.getString("muscle_targeted")+"\t");
            System.out.print("equipment: " + d.getString("equipment")+"\t");
            System.out.print("type: " + d.getString("type")+"\t");
            if(d.getInteger("weight")!=null)
                System.out.print("weight: " + d.getInteger("weight"));
            System.out.print("\n");
        }
        System.out.println();
        System.out.print("STRETCHING:\n");
        for(Document d: (ArrayList<Document>)doc.get("stretching")) {
            System.out.print("name: " + d.getString("name")+"\t");
            System.out.print("muscle_targeted: " + d.getString("muscle_targeted")+"\t");
            System.out.print("equipment: " + d.getString("equipment")+"\t");
            System.out.print("type: " + d.getString("type")+"\n");
        }

        System.out.println("\nPress any key to return to the main menu");
        Scanner sc = new Scanner(System.in);
        String input = sc.next();
    }

}

//Judith_Eyres718285046@liret.org
//3eYZcK8f