package wefit.db;
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

import static com.mongodb.client.model.Projections.*;



import javax.print.Doc;

public class MongoDbConnector {
    static ConnectionString uri;
    static MongoClient myClient;
    static MongoDatabase db;

    static MongoCollection<Document> exercises;
    static MongoCollection<Document> routines;
    static MongoCollection<Document> users;

    public MongoDbConnector(String conn, String db_name){
        uri = new ConnectionString(conn);
        myClient = MongoClients.create(uri);
        db = myClient.getDatabase(db_name);

        exercises = db.getCollection("exercises");
        routines = db.getCollection("routines");
        users = db.getCollection("users");
    }

    private Consumer<Document> printDocuments() {
        return doc -> System.out.println(doc.toJson());
    }
    private Consumer<Document> printRoutine() {
        return doc -> {
            System.out.println("trainer\t" + doc.getString("trainer"));
            System.out.println("level\t" + doc.getString("level"));
            System.out.println("work\t" + doc.getString("level"));
        };
    }

    public Document signIn(String username, String password) {
        Document doc = users.find(and(eq("email",username),eq("password",password))).first();
        return doc;
    }

    public void showCurrentRoutine(String user){
        String c_day = LocalDate.now().toString();
        Bson match = match(and(eq("user",user),gt("end_day",c_day)));
        Bson proj = project(fields(excludeId(), exclude("user","warm_up","exercises","stretching")));

        Document r = routines.aggregate(Arrays.asList(match,proj)).first();

        if(r==null)
            System.out.println("you don't have current routines");
        else
            System.out.println(r.toJson());

    }
    public void showPastRoutine(String user){
        String c_day = LocalDate.now().toString();
        Bson match = match(and(eq("user",user),lt("end_day",c_day)));
        Bson proj = project(fields(excludeId(), exclude("user","warm_up","exercises","stretching")));

        Document r = routines.aggregate(Arrays.asList(match,proj)).first();

        if(r==null)
            System.out.println("you don't have past routines");
        else
            System.out.println(r.toJson());

    }
}