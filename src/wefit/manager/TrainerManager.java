package wefit.manager;

import org.bson.Document;
import wefit.db.MongoDbConnector;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Scanner;

public class TrainerManager {

    private Document self;
    private String[] Muscles = {"Shoulders", "Traps", "Biceps", "Neck", "Lower Back", "Adductors", "Forearms", "Hamstrings", "Lats", "Middle Back", "Glutes", "Chest", "Abdominals", "Quadriceps", "Abductors", "Calves", "Triceps"};
    private MongoDbConnector mongoDb;

    public TrainerManager(Document trainer, MongoDbConnector mongo){
        this.self = trainer;
        this.mongoDb = mongo;
    }

    // Create a routine for a user
    public void createRoutine(){

        // insert name and surname and query the db for the user
        System.out.println("Insert the name of the athlete...");
        String search_name;
        Scanner sc = new Scanner(System.in);
        search_name = sc.next();
        System.out.println("...and the surname");
        search_name += sc.next();
        Document user = mongoDb.searchUser("search_name");
        System.out.println(user.toString());

        // create variables for building the document
        ArrayList<Document> exercises = new ArrayList<Document>();
        ArrayList<Document> warmup = new ArrayList<Document>();
        ArrayList<Document> stretching = new ArrayList<Document>();
        int running = 0;
        int n = 0;

        // cycle all the 17 muscle groups to insert one exercise each
        while(n<17){
            Document exercise = new Document();
            System.out.println("Insert exercise's name,equipment,type and weight...");
            String fetch = sc.next();
            exercise.append("name", fetch);
            exercise.append("muscle_targeted", Muscles[n]);
            fetch = sc.next();
            exercise.append("equipment", fetch);
            fetch = sc.next();
            exercise.append("type", fetch);
            fetch = sc.next();
            exercise.append("weight", fetch);
            exercises.add(exercise);
            n++;
        }

        // two distinct cycles merged to build the warmup and stretching routines
        while(running<2){
            Document exercise = new Document();
            if(running == 0) System.out.println("Insert warmup exercise's name,muscle,equipment and type...");
            if(running == 1) System.out.println("Insert stretching exercise's name,muscle,equipment and type...");
            String fetch = sc.next();
            exercise.append("name", fetch);
            fetch = sc.next();
            exercise.append("muscle_targeted", fetch);
            fetch = sc.next();
            exercise.append("equipment", fetch);
            fetch = sc.next();
            exercise.append("type", fetch);
            if(running == 0) warmup.add(exercise);
            if(running == 1) stretching.add(exercise);
            System.out.println("Do you want to add another exercise (yes/no)");
            fetch = sc.next();
            if(fetch.equals("no")) running++;
        }

        // create and append to the document all the data
        Document new_routine = new Document();
        new_routine.append("end_day", LocalDate.now().plusMonths(1).toString());
        new_routine.append("exercises", exercises);
        System.out.println("Insert the level of the routine...");
        String fecth = sc.next();
        new_routine.append("level", fecth);
        System.out.println("Insert the repetitions of the routine...");
        fecth = sc.next();
        new_routine.append("repeat", fecth);
        System.out.println("Insert the rest time...");
        fecth = sc.next();
        new_routine.append("rest_time(sec)", fecth);
        new_routine.append("starting_day", LocalDate.now().toString());
        new_routine.append("stretching", stretching);
        new_routine.append("trainer", self.getString("user_id"));
        new_routine.append("user", user.getString("user_id"));
        new_routine.append("warm_up", warmup);
        System.out.println("Insert the repetition time...");
        fecth = sc.next();
        new_routine.append("work_time(sec)", fecth);
        mongoDb.insertRoutine(new_routine);
    }

}
