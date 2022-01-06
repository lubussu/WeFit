package wefit;
import com.mongodb.ConnectionString;
import com.mongodb.client.*;
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
                    case "Beginner":
                        return tlr.nextInt(20, 30 + 1);
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

    private static void createRoutine(Document user, String level, long start){
        String s;
        Document routine = new Document("user", user.getString("athlete_id"));

        Bson match = match(eq("trainer","no")); //change to yes
        Bson sample = sample(1);
        s = users.aggregate(Arrays.asList(match, sample)).first().getString("athlete_id");
        routine.append("trainer", s)
                .append("level", level)
                .append("work_time(sec)",30)
                .append("rest_time(sec)", 10)
                .append("repeat", 3);

        ArrayList<Document> warm_up = new ArrayList<>();
        switch (level){ //scelgo gli esercizi tra quelli dello stesso livello o inferiori
            case "Beginner":
                match = match(and(eq("type","Cardio"),eq("level","Beginner")));
                break;
            case "Intermediate":
                match = match(and(eq("type","Cardio"),or(eq("level","Intermediate"),eq("level","Beginner"))));
                break;
            case "Expert":
                match = match(and(eq("type","Cardio"),or(eq("level","Intermediate"),eq("level","Beginner"),eq("level","Expert"))));
                break;
        }
        sample = sample(3); //scelgo casualmente 3 esercizi tra quelli trovati
        AggregateIterable<Document> output = exercises.aggregate(Arrays.asList(match,sample));
        for(Document d: output)
            createEx(d, warm_up, false);
        routine.append("warm_up", warm_up); //aggiungo l'esercizio alla routine (solo alcuni attributi)

        ArrayList<Document> exs = new ArrayList<>();
        for(String m: muscles){
            switch (level){ //scelgo gli esercizi tra quelli dello stesso livello o inferiori
                case "Beginner":
                    match = match(and(
                            eq("muscle_targeted",m),eq("level","Beginner"),
                            ne("type","Stretching")));
                    break;
                case "Intermediate":
                    match = match(and(
                            eq("muscle_targeted",m),or(eq("level","Beginner"),eq("level","Intermediate")),
                            ne("type","Stretching")));
                    break;
                case "Expert":
                    match = match(and(
                            eq("muscle_targeted",m),or(eq("level","Beginner"),eq("level","Intermediate"),eq("level","Expert")),
                            ne("type","Stretching")));
                    break;
            }
            sample = sample(1);
            output = exercises.aggregate(Arrays.asList(match,sample));
            for(Document d:output)
                createEx(d, exs, true);
        }
        routine.append("exercises",exs);

        ArrayList<Document> stretch = new ArrayList<>();
        switch (level){ //scelgo gli esercizi tra quelli dello stesso livello o inferiori
            case "Beginner":
                match = match(and(eq("type","Stretching"),eq("level","Beginner")));
                break;
            case "Intermediate":
                match = match(and(eq("type","Stretching"),or(eq("level","Intermediate"),eq("level","Beginner"))));
                break;
            case "Expert":
                match = match(and(eq("type","Stretching"),or(eq("level","Intermediate"),eq("level","Beginner"),eq("level","Expert"))));
                break;
        }
        sample = sample(3);
        output = exercises.aggregate(Arrays.asList(match,sample));
        for(Document d:output)
            createEx(d, stretch, true);
        routine.append("stretching", stretch);
        routine.append("starting_day", LocalDate.ofEpochDay(start).toString());
        routine.append("end_day", LocalDate.ofEpochDay(start).plusDays(30).toString());

        routines.insertOne(routine);
        //System.out.println("----------------------------------------------------------------------");
        //System.out.println(routine.toJson());
    }

    public static void comments(){
        MongoCollection<Document> comments = db.getCollection("comments");
        String utente;
        String routine;

        int count=1;
        try (MongoCursor<Document> cursor = comments.find(or(eq("user",null),eq("routine", null))).iterator()) {
            while (cursor.hasNext()) {
                System.out.println(count);
                Document com = cursor.next();
                Bson sample = sample(1);
                utente = users.aggregate(Arrays.asList(sample)).first().getString("athlete_id");
                routine = routines.aggregate(Arrays.asList(sample)).first().getObjectId("_id").toString();
                comments.updateOne(com, set("user",utente));
                comments.updateOne(com, set("routine",routine));
                count++;
            }
        }

    }
/*
    public static void main(String[] args) {
        uri = new ConnectionString("mongodb://localhost:27017");
        myClient = MongoClients.create(uri);
        db = myClient.getDatabase("wefit");

        exercises = db.getCollection("exercises");
        routines = db.getCollection("routines");
        users = db.getCollection("users");

        // array con tutti i gruppi muscolaru
        Bson group = group("$muscle_targeted", sum("count", 1));
        exercises.aggregate(Arrays.asList(group)).forEach(doc -> muscles.add(doc.getString("_id")));

        Random random = new Random();
        int minDay = (int) LocalDate.of(2015, 1, 1).toEpochDay();
        int maxDay = (int) LocalDate.of(2021, 1, 31).toEpochDay();

        //int count=1;
        Bson match = match(eq("trainer","no"));

        try (MongoCursor<Document> cursor = users.aggregate(Arrays.asList(match)).iterator()) {
            while (cursor.hasNext()) {
                //System.out.println(count);
                Document user = cursor.next();
                String level = user.getString("level");

                long randomDay = minDay + random.nextInt(maxDay - minDay);

                if(level==null)
                    continue;
                //numero di routine per l'utente in corso (random tra 1 e 4)
                ThreadLocalRandom tlr = ThreadLocalRandom.current();
                int routines = tlr.nextInt(1,5);

                while(routines > 0){
                    createRoutine(user, level, randomDay);
                    //incremento la data di inizio di un numero casuale tra 30 e 90 giorni
                    randomDay = LocalDate.ofEpochDay(randomDay).plusDays(random.nextInt(90 - 30)).toEpochDay();
                    int test = tlr.nextInt(1, 9);
                    if(test > 6){ // incremento di livello con una probabilità di 2/8
                        switch (level){
                            case "Beginner": {
                                level = "Intermediate";
                                break;
                            }
                            case "Intermediate": {
                                level = "Expert";
                                break;
                            }
                        }
                        users.updateOne(
                                eq("athlete_id", user.getString("athlete_id")),
                                set("level", level));
                    }
                    routines--;
                }
                //count++;
            }
        }


        //match = match(and(eq("trainer","no"),eq("level","")));
        //Bson sample = sample(16000);
        //users.aggregate(Arrays.asList(match)).forEach(
        //        doc->users.updateOne(doc,set("level","Expert"))
        //);

        myClient.close();
    }*/
}
