import com.mongodb.ConnectionString;
import com.mongodb.client.*;
import org.bson.Document;

import java.util.*;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Sorts.descending;

public class WorkoutRoutineCreator {
    static ArrayList<Document> beginner_ex = new ArrayList<Document>();
    static ArrayList<Document> intermediate_ex = new ArrayList<Document>();
    static ArrayList<Document> expert_ex = new ArrayList<Document>();
    static ArrayList<Document> trainers = new ArrayList<Document>();


    private static void beginnerRoutine(Document user){
        Document doc = new Document("user", user.getString("athlete_id"));

        doc
                .append("trainer", 1);
    }

    private static void createRoutine(MongoDatabase db){
        MongoCollection<Document> exercises = db.getCollection("exercises");
        MongoCollection<Document> routines = db.getCollection("routines");
        MongoCollection<Document> users = db.getCollection("users");

        try (MongoCursor<Document> cursor = users.find(eq("trainer","no")).iterator()) {
            while (cursor.hasNext()) {
                Document user = cursor.next();
                System.out.println(user.toJson());
                switch(user.getString("level")){
                    case "beginner": {
                        System.out.println(("ciao"));
                        //createBeginnerRoutine();
                        break;
                    }
                    case "intermediate": {
                        //createIntermediateRoutine();
                        break;
                    }
                    case "expert": {
                        //createExpertRoutine();
                        break;
                    }

                }
            }
        }

        Document doc = new Document("name", "MongoDB")
                .append("count", 1)
                .append("versions", Arrays.asList("v3.2", "v3.0", "v2.6"))
                .append("info", new Document("x", 203).append("y", 102));

    }

    public static void main(String[] args) {
        ConnectionString uri = new ConnectionString("mongodb://localhost:27017");
        MongoClient myClient = MongoClients.create(uri);
        MongoDatabase database = myClient.getDatabase("exer");

        MongoCollection<Document> exercises = database.getCollection("exercises");
        try (MongoCursor<Document> cursor = exercises.find(eq("level","Beginner")).iterator()) {
            while (cursor.hasNext()) {
                System.out.println("BEGINNER");
                Document exercise = cursor.next();
                beginner_ex.add(exercise);
            }
        }
        try (MongoCursor<Document> cursor = exercises.find(eq("level","Intermediate")).iterator()) {
            while (cursor.hasNext()) {
                Document exercise = cursor.next();
                intermediate_ex.add(exercise);
            }
        }
        try (MongoCursor<Document> cursor = exercises.find(eq("level","Expert")).iterator()) {
            while (cursor.hasNext()) {
                Document exercise = cursor.next();
                expert_ex.add(exercise);
            }
        }
        MongoCollection<Document> users = database.getCollection("users");
        try (MongoCursor<Document> cursor = users.find(eq("trainer","yes")).iterator()) {
            while (cursor.hasNext()) {
                Document trainer = cursor.next();
                trainers.add(trainer);
            }
        }

        /*
        System.out.println("BEGINNER");
        for(Document d: beginner_ex)
            System.out.println(d.toJson());
        System.out.println("INTERMEDIATE");
        for(Document d: intermediate_ex)
            System.out.println(d.toJson());
        System.out.println("EXPERT");
        for(Document d: expert_ex)
            System.out.println(d.toJson());
         */

        //createRoutine(database);
        myClient.close();
    }
}
