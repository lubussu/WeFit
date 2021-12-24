
import com.mongodb.ConnectionString;
import com.mongodb.client.*;
import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.bson.json.JsonWriterSettings;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Updates.*;
import static com.mongodb.client.model.Aggregates.*;
import static com.mongodb.client.model.Projections.*;
import static com.mongodb.client.model.Accumulators.*;
import static com.mongodb.client.model.Accumulators.addToSet;
import static com.mongodb.client.model.Accumulators.sum;
import static com.mongodb.client.model.Sorts.descending;


import org.bson.conversions.Bson;

import javax.print.Doc;

public class WorkoutRoutineCreator {
    static ConnectionString uri;
    static MongoClient myClient;
    static MongoDatabase db;

    static MongoCollection<Document> exercises;
    static MongoCollection<Document> routines;
    static MongoCollection<Document> users;
    static ArrayList<String> muscles = new ArrayList<String>();


    private static Consumer<Document> printDocuments() {
        return doc -> System.out.println(doc.toJson());
    }

    private static void createEx(Document doc, ArrayList<Document> docs, boolean x){
        Document ex = new Document();
        ex.append("name", doc.getString("name"))
                .append("muscle_targeted", doc.getString("muscle_targeted"))
                .append("equipment", doc.getString("equipment"))
                .append("type", doc.getString("type"));
        if(x){
            int weight = createWeight(doc.getString("muscle_targeted"), doc.getString("equipment"),"Beginner");
            if(weight>0)
                ex.append("weight",weight);
        }
        docs.add(ex);
    }

    private static int weightMuscle(String muscle, String level){
        ThreadLocalRandom tlr = ThreadLocalRandom.current();
        switch (muscle) {
            case "Chest": {
                switch(level){
                    case "Beginner": return tlr.nextInt(20, 30 + 1);
                    case "Intermediate": return tlr.nextInt(30, 40 + 1);
                    case "Expert": return tlr.nextInt(40, 50 + 1);
                }
            }
            case "Forearms":
            case "Triceps": {
                switch(level){
                    case "Beginner": return tlr.nextInt(5, 10 + 1);
                    case "Intermediate": return tlr.nextInt(10, 15 + 1);
                    case "Expert": return tlr.nextInt(15, 20 + 1);
                }
            }
            case "Lats":
            case "Adductors":
            case "Abductors":
            case "Biceps":
            case "Glutes":
            case "Abdominals":
            case "Traps":
            case "Calves":
            case "Hamstrings":
            case "Middle Back":
            case "Lower Back":
            case "Quadriceps":{
                switch(level){
                    case "Beginner": return tlr.nextInt(10, 15 + 1);
                    case "Intermediate": return tlr.nextInt(15, 20 + 1);
                    case "Expert": return tlr.nextInt(20, 25 + 1);
                }
            }
            case "Shoulders": {
                switch(level){
                    case "Beginner": return tlr.nextInt(15, 20 + 1);
                    case "Intermediate": return tlr.nextInt(20, 30 + 1);
                    case "Expert": return tlr.nextInt(30, 50 + 1);
                }
            }
            default: return -1;
        }
    }
    private static int createWeight(String muscle, String eq, String level){
        switch (eq){
            case "Kettlebells": {
                switch (level){
                    case "Beginner": return 4;
                    case "Intermediate": return 8;
                    case "Expert": return 12;
                }
            }
            case "Medicine Ball": {
                switch (level){
                    case "Beginner": return 6;
                    case "Intermediate": return 10;
                    case "Expert": return 14;
                }
            }
            case "Barbell":
            case "Machine":
            case "Cable": {
                return weightMuscle(muscle, level);
            }
            case "Dumbbell":{
                return weightMuscle(muscle, level)/2;
            }
            default: return -1;
        }
    }

    private static void createRoutine(Document user, String level){
        String s;
        Document routine = new Document("user", user.getString("athlete_id"));

        Bson match = match(eq("trainer","no")); //change to yes
        Bson sample = sample(1);
        s = users.aggregate(Arrays.asList(match, sample)).first().getString("athlete_id");
        routine.append("trainer", s)
                .append("level", level)
                .append("work_time(sec)",30)
                .append("rest_time(sec", 10)
                .append("repeat", 3);

        ArrayList<Document> warm_up = new ArrayList<>();
        match = match(and(eq("type","Cardio"),eq("level",level)));
        sample = sample(3);
        AggregateIterable<Document> output = exercises.aggregate(Arrays.asList(match,sample));
        for(Document d: output)
            createEx(d, warm_up, false);
        routine.append("warm_up", warm_up);

        ArrayList<Document> exs = new ArrayList<>();
        for(String m: muscles){
            match = match(and(
                    eq("muscle_targeted",m),eq("level",level),
                            ne("type","Stretching")));
            sample = sample(1);
            output = exercises.aggregate(Arrays.asList(match,sample));
            for(Document d:output)
                createEx(d, exs, true);
        }
        routine.append("exercises",exs);

        ArrayList<Document> stretch = new ArrayList<>();
        match = match(and(eq("type","Stretching"),eq("level",level)));
        sample = sample(3);
        output = exercises.aggregate(Arrays.asList(match,sample));
        for(Document d:output)
            createEx(d, stretch, true);
        routine.append("stretching", stretch);

        System.out.println("----------------------------------------------------------------------");
        System.out.println(routine.toJson());
    }

    public static void main(String[] args) {
        uri = new ConnectionString("mongodb://localhost:27017");
        myClient = MongoClients.create(uri);
        db = myClient.getDatabase("wefit");

        exercises = db.getCollection("exercises");
        routines = db.getCollection("routines");
        users = db.getCollection("users");

        Bson group = group("$muscle_targeted", sum("count", 1));
        exercises.aggregate(Arrays.asList(group)).forEach(doc -> muscles.add(doc.getString("_id")));

        try (MongoCursor<Document> cursor = users.find(eq("trainer","no")).iterator()) {
            while (cursor.hasNext()) {
                Document user = cursor.next();
                String level = user.getString("level");
                if(level==null)
                    continue;
                createRoutine(user, level);
                break;
            }
        }
        myClient.close();
    }
}
