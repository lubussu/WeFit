package wefit.manager;

import org.bson.Document;
import org.bson.conversions.Bson;
import wefit.db.MongoDbConnector;

import javax.print.Doc;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Scanner;

import static com.mongodb.client.model.Aggregates.match;
import static com.mongodb.client.model.Aggregates.unwind;
import static com.mongodb.client.model.Filters.regex;

public class TrainerManager extends UserManager{

    private String[] Muscles = {"Shoulders", "Traps", "Biceps", "Neck", "Lower Back", "Adductors", "Forearms", "Hamstrings", "Lats", "Middle Back", "Glutes", "Chest", "Abdominals", "Quadriceps", "Abductors", "Calves", "Triceps"};

    public TrainerManager(Document trainer, MongoDbConnector mongo){
        super(trainer, mongo);
    }

    public void addTrainer(){
        System.out.println("Insert the name or the user_id of the user you want to promote or press r to return..");
        BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));

        String search_name = null;
        try {
            search_name = bufferRead.readLine();
        } catch (IOException e) {e.printStackTrace();}

        if(search_name == null)
            return;
        Document user = new Document();
        if(!search_name.matches("[0-9.]+"))
            user = mongoDb.getUser(search_name);

        user.remove("trainer");
        user.append("trainer", "yes");

        mongoDb.changeProfile(user);
        neo4j.changeProfile(user);
    }

    // Create a routine for a user
    public void createRoutine(){
        BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
        Scanner sc = new Scanner(System.in);

        // insert name and query the db for the user
        System.out.println("\nInsert the name of the user or the user_id...");
        String search_name = null;
        try {
            search_name = bufferRead.readLine();
        } catch (IOException e) {e.printStackTrace();}

        String user = search_name;
        if(!search_name.matches("[0-9.]+"))
            user = mongoDb.getUser(search_name).getString("user_id");

        // create variables for building the document
        ArrayList<Document> exercises = new ArrayList<Document>();
        ArrayList<Document> warmup = new ArrayList<Document>();
        ArrayList<Document> stretching = new ArrayList<Document>();
        int running = 0;
        int n = 0;

        // cycle all the 17 muscle groups to insert one exercise each
        while(n<1){
            exercises.add(insertExercise(Muscles[n],null));
            n++;
        }

        // two distinct cycles merged to build the warmup and stretching routines
        while(running<2){
            Document exercise = new Document();
            String fetch;
            if(running == 0){
                System.out.print("\nAdding warm_um exercises..");
                exercise = insertExercise(null, "Cardio");
            }
            if(running == 1) {
                System.out.print("\nAdding stretching exercises...");
                exercise = insertExercise(null, "Stretching");
            }

            if(running == 0) warmup.add(exercise);
            if(running == 1) stretching.add(exercise);
            System.out.println("Do you want to add another exercise (yes/no)");
            fetch = sc.next();
            if(fetch.equals("no")) running++;
        }

        // create and append to the document all the data
        Document new_routine = new Document();
        new_routine.append("user", user);
        new_routine.append("trainer", self.getString("user_id"));

        System.out.println("Insert the level of the routine...");
        String fetch = sc.next();
        fetch = fetch.replace(fetch.substring(0,1), fetch.substring(0,1).toUpperCase());
        new_routine.append("level", fetch);

        System.out.println("Insert the work time (sec)...");
        fetch = sc.next();
        new_routine.append("work_time(sec)", Integer.parseInt(fetch));
        System.out.println("Insert the rest time (sec)...");
        fetch = sc.next();
        new_routine.append("rest_time(sec)", Integer.parseInt(fetch));
        System.out.println("Insert the number of repetitions of the routine...");
        fetch = sc.next();
        new_routine.append("repeat", Integer.parseInt(fetch));

        new_routine.append("warm_up", warmup);
        new_routine.append("exercises", exercises);
        new_routine.append("stretching", stretching);

        new_routine.append("starting_day", LocalDate.now().toString());
        new_routine.append("end_day", LocalDate.now().plusMonths(1).toString());
        new_routine.append("num_votes", 0);
        new_routine.append("vote", 0);

        mongoDb.insertRoutine(new_routine);
        neo4j.insertRoutine(new_routine);
    }

    public Document insertExercise(String muscle, String type){
        BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
        Scanner sc = new Scanner(System.in);
        Document exercise = new Document();
        String fetch = null;
        Document ex;
        while(true) {
            if(muscle == null)  System.out.println("\nInsert an exercise's name..");
            else System.out.println("\nInsert an exercise's name per muscle " + muscle + "...");

            try {
                fetch = bufferRead.readLine();
            } catch (IOException e) {e.printStackTrace();}

            ex = mongoDb.showExercises(fetch, false, muscle, type);
            if(ex!=null)
                break;
        }
        System.out.println(ex.getString("name")+ " added\n");
        exercise.append("name", ex.getString("name"));
        exercise.append("muscle_targeted", ex.getString("muscle_targeted"));
        exercise.append("equipment", ex.getString("equipment"));
        exercise.append("type", ex.getString("type"));

        System.out.println("Insert exercise's weight or \'n\' if weight is not present...");
        fetch = sc.next();
        if(!fetch.equals("n"))
            exercise.append("weight", Integer.parseInt(fetch));

        return exercise;
    }

    public boolean sessionTrainer(){
        System.out.println("WELCOME " + self.getString("name"));
        boolean running = true;
        while(running) {
            System.out.println("\nWhat do you need?\n" +
                    "1) See your routines\n" +
                    "2) Add a new routine\n" +
                    "3) Add a new exercise\n" +
                    "4) Add a new trainer\n" +
                    "5) See normal user menu\n" +
                    "6) Log out\n" +
                    "7) Exit the app");
            Scanner sc = new Scanner(System.in);
            String input = sc.next();
            switch (input) {
                case "1":
                    //findRoutines();
                    break;
                case "2":
                    createRoutine();
                    break;
                case "3":
                    //addExercise();
                    break;
                case "4":
                    addTrainer();
                    break;
                case "5":
                    session();
                    break;
                case "6":
                    running = false;
                    System.out.println("Bye bye (￣(ｴ)￣)ﾉ");
                    break;
                case "7":
                    System.out.println("Bye bye (￣(ｴ)￣)ﾉ");
                    return false;
                default:
                    System.out.println("Please select an existing option!\n");
                    break;
            }
        }
        return true;
    }

}
