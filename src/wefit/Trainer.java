package wefit;

import org.bson.Document;
import wefit.db.MongoDbConnector;

import java.util.Scanner;

public class Trainer {

    static Document self;
    static String[] Muscles = {"Shoulders", "Traps", "Biceps", "Neck", "Lower Back", "Adductors", "Forearms", "Hamstrings", "Lats", "Middle Back", "Glutes", "Chest", "Abdominals", "Quadriceps", "Abductors", "Calves", "Triceps"};
    static MongoDbConnector mongoDb;

    public Trainer(Document trainer, MongoDbConnector mongo){
        this.self = trainer;
        this.mongoDb = mongo;
    }

    public void createRoutine(){
        System.out.println("Insert the name of the athlete...");
        String search_name;
        Scanner sc = new Scanner(System.in);
        search_name = sc.next();
        System.out.println("...and the surname");
        search_name += sc.next();
        Document user = mongoDb.searchUser("search_name");
        System.out.println(user.toString());
    }

}
