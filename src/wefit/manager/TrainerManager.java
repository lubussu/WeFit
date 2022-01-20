package wefit.manager;

import org.bson.Document;
import org.bson.conversions.Bson;
import wefit.db.MongoDbConnector;

import javax.print.Doc;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

import static com.mongodb.client.model.Aggregates.match;
import static com.mongodb.client.model.Aggregates.unwind;
import static com.mongodb.client.model.Filters.regex;

public class TrainerManager extends UserManager{

    private String[] Muscles = {"Shoulders", "Traps", "Biceps", "Neck", "Lower Back", "Adductors", "Forearms", "Hamstrings", "Lats", "Middle Back", "Glutes", "Chest", "Abdominals", "Quadriceps", "Abductors", "Calves", "Triceps"};
    private String[] Levels = {"Beginner", "Intermediate", "Expert"};

    public TrainerManager(Document trainer, MongoDbConnector mongo){
        super(trainer, mongo);
    }

    public void addExercise(){
        BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
        Scanner sc = new Scanner(System.in);
        int r = 0;
        String exerciseName = null;
        String exerciseType = null;
        String exerciseMuscle = null;
        String exerciseEquipment = null;
        String exerciseLevel = null;
        String exerciseImage1 = null;
        String exerciseImage2 = null;
        String exerciseDetails = null;

        while(r<8) {
            switch(r) {
                case 0:
                    System.out.println("\nInsert the name of the new exercise or press r to return...");
                    String search_ex = null;
                    try {
                        search_ex = bufferRead.readLine();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if(search_ex.equals("r"))
                        return;
                    exerciseName = search_ex;
                    r++;
                    break;
                case 1:
                    System.out.println("\nInsert the type of the new exercise or press r to return...");
                    String search_type = null;
                    try {
                        search_type = bufferRead.readLine();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if(search_type.equals("r"))
                        return;

                    exerciseType = search_type;
                    r++;
                    break;
                case 2:
                    System.out.println("\nSelect the number of the muscle target of the new exercise or press r to return...");

                    for (int i = 0; i < 17; i++) {
                        System.out.println(i + " - " + Muscles[i]);
                    }
                    String search_muscle = null;
                    try {
                        search_muscle = bufferRead.readLine();
                    } catch (IOException e) { e.printStackTrace(); }
                    if(search_muscle.equals("r"))
                        return;

                    if (Integer.parseInt(search_muscle) > 17) {
                        System.out.println("\nMuscle target not found... please select again");
                        r = 2;
                    } else {
                        for (int j = 0; j < 17; j++) {
                            if (Integer.parseInt(search_muscle) == j) {
                                exerciseMuscle = Muscles[j];
                                r++;
                            }
                        }
                    }
                    break;
                case 3:
                    System.out.println("\nInsert the equipment of the new exercise or press r to return...");
                    String search_equipment = null;
                    try {
                        search_equipment = bufferRead.readLine();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if(search_equipment.equals("r"))
                        return;

                    exerciseEquipment = search_equipment;
                    r++;
                    break;
                case 4:
                    System.out.println("\nSelect the level of the new exercise or press r to return...");
                    for (int i = 0; i < 3; i++) {
                        System.out.println(i + " - " + Levels[i]);
                    }
                    String search_level = null;
                    try {
                        search_level = bufferRead.readLine();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if(search_level.equals("r"))
                        return;

                    if (Integer.parseInt(search_level) > 3) {
                        System.out.println("\nLevel not valid... please select again");
                        r = 4;
                    } else {
                        for (int j = 0; j < 3; j++) {
                            if (Integer.parseInt(search_level) == j) {
                                exerciseLevel = Levels[j];
                                r++;
                            }
                        }
                    }
                    break;
                case 5:
                    System.out.println("\nInsert the first image's link of the new exercise or press r to return...");
                    String search_image1 = null;
                    try {
                        search_image1 = bufferRead.readLine();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if(search_image1.equals("r"))
                        return;

                    exerciseImage1 = search_image1;
                    r++;
                    break;
                case 6:
                    System.out.println("\nInsert the second image's link of the new exercise or press r to return...");
                    String search_image2 = null;
                    try {
                        search_image2 = bufferRead.readLine();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if(search_image2.equals("r"))
                        return;

                    exerciseImage2 = search_image2;
                    r++;
                    break;
                case 7:
                    System.out.println("\nWrite an explanation of the new exercise or press r to return...");
                    String search_details = null;
                    try {
                        search_details = bufferRead.readLine();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if(search_details.equals("r"))
                        return;

                    exerciseDetails = search_details;
                    r++;
                    break;
            }
        }
        System.out.println("Press any key to insert or press r to return..");
        if(sc.next().equals("r"))
            return;

        List<Document> images = new ArrayList<Document>();
        images.add(new Document("image", exerciseImage1));
        images.add(new Document("image", exerciseImage2));

        Document newExercise = new Document();
        newExercise.append("name", exerciseName);
        newExercise.append("type", exerciseType);
        newExercise.append("muscle_targeted", exerciseMuscle);
        newExercise.append("equipment", exerciseEquipment);
        newExercise.append("level", exerciseLevel);
        newExercise.append("images", images);
        newExercise.append("details", exerciseDetails);

        mongoDb.insertNewExercise(newExercise);
    }

    //change a user from normal user to trainer
    public void addTrainer(){
        System.out.println("Insert the name or the user_id of the user you want to promote or press r to return..");
        BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));

        String search_name = null;
        try {
            search_name = bufferRead.readLine();
        } catch (IOException e) {e.printStackTrace();}

        if(search_name.equals("r"))
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
        System.out.println("\nInsert the name of the user or the user_id or press r to return...");
        String search_name = null;
        try {
            search_name = bufferRead.readLine();
        } catch (IOException e) {e.printStackTrace();}
        if(search_name.equals("r"))
            return;
        String user = search_name;
        if(!search_name.matches("[0-9.]+")) {
            Document d = mongoDb.getUser(search_name);
            if(d != null)
                user = d.getString("user_id");
            else {
                System.out.println("User not found");
                return;
            }
        }

        // create variables for building the document
        ArrayList<Document> exercises = new ArrayList<Document>();
        ArrayList<Document> warmup = new ArrayList<Document>();
        ArrayList<Document> stretching = new ArrayList<Document>();
        int running = 0;
        int n = 0;

        // cycle all the 17 muscle groups to insert one exercise each
        while(n<1){
            Document d = insertExercise(Muscles[n],null);
            if(d==null)
                return;
            exercises.add(d);
            n++;
        }

        // two distinct cycles merged to build the warmup and stretching routines
        while(running<2){
            Document exercise = null;
            String fetch;
            if(running == 0){
                System.out.print("\nAdding warm_um exercises..");
                exercise = insertExercise(null, "Cardio");
                if(exercise==null)
                    return;
            }
            if(running == 1) {
                System.out.print("\nAdding stretching exercises...");
                exercise = insertExercise(null, "Stretching");
                if(exercise==null)
                    return;
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

        System.out.println("Insert the level of the routine or press r to return...");
        String fetch = sc.next();
        if(fetch.equals("r"))
            return;
        fetch = fetch.replace(fetch.substring(0,1), fetch.substring(0,1).toUpperCase());
        new_routine.append("level", fetch);

        System.out.println("Insert the work time (sec) or press r to return...");
        fetch = sc.next();
        if(fetch.equals("r"))
            return;
        new_routine.append("work_time(sec)", Integer.parseInt(fetch));
        System.out.println("Insert the rest time (sec) or press r to return...");
        fetch = sc.next();
        if(fetch.equals("r"))
            return;
        new_routine.append("rest_time(sec)", Integer.parseInt(fetch));
        System.out.println("Insert the number of repetitions of the routine or press r to return...");
        fetch = sc.next();
        if(fetch.equals("r"))
            return;
        new_routine.append("repeat", Integer.parseInt(fetch));

        new_routine.append("warm_up", warmup);
        new_routine.append("exercises", exercises);
        new_routine.append("stretching", stretching);

        new_routine.append("starting_day", LocalDate.now().toString());
        new_routine.append("end_day", LocalDate.now().plusMonths(1).toString());
        new_routine.append("num_votes", 0);
        new_routine.append("vote", 0);

        System.out.println("Press any key to insert or press r to return..");
        fetch = sc.next();
        if(fetch.equals("r"))
            return;
        mongoDb.insertRoutine(new_routine);
        neo4j.insertRoutine(new_routine);
    }

    //function for insert an exercise in a new routine
    public Document insertExercise(String muscle, String type){
        BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
        Scanner sc = new Scanner(System.in);
        Document exercise = new Document();
        String fetch = null;
        Document ex;
        while(true) {
            if(muscle == null)  System.out.println("\nInsert an exercise's name or press r to return..");
            else System.out.println("\nInsert an exercise's name per muscle " + muscle + " or press r to return...");

            try {
                fetch = bufferRead.readLine();
            } catch (IOException e) {e.printStackTrace();}
            if(fetch.equals("r"))
                return null;
            ex = mongoDb.searchExercises(fetch, false, muscle, type);
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

    public void mostUsedEquipment(){
        System.out.printf("%15s %15s %10s", "MUSCLE", "EQUIPMENT", "COUNT");
        System.out.println("\n----------------------------------------------");
        mongoDb.mostUsedEquipment(null);
        for(String s: Muscles)
            mongoDb.mostUsedEquipment(s);
    }

    public void mostVotedPresentExercises(){
        Scanner sc = new Scanner(System.in);
        String s;

        System.out.println("Insert the number of highest voted routine you want to consider and the number of exercises you want to show" +
                "or press r to return...");
        s = sc.next();
        if(s.equals("r")) return;
        int max_vote = Integer.parseInt(s);
        s= sc.next();
        if(s.equals("r")) return;
        int max_ex = Integer.parseInt(s);

        mongoDb.mostVotedPresentExercises(max_vote, max_ex);
    }

    public boolean sessionTrainer(){
        System.out.println("WELCOME " + self.getString("name"));
        boolean running = true;
        while(running) {
            System.out.println("\nWhat do you need?\n" +
                    "1)  See your routines\n" +
                    "2)  Add a new routine\n" +
                    "3)  Add a new exercise\n" +
                    "4)  Add a new trainer\n" +
                    "5)  See normal user menu\n" +
                    "----------ANALYTICS----------\n" +
                    "6)  See average age per level\n" +
                    "7)  Find most fidelity users\n" +
                    "8)  Find most used equipments\n" +
                    "9)  Find most common exercises in most rated routines\n" +
                    "10) Show level up\n" +
                    "-----------------------------\n" +
                    "11) Log out\n" +
                    "0)  Exit the app");
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
                    addExercise();
                    break;
                case "4":
                    addTrainer();
                    break;
                case "5":
                    session();
                    break;
                case "6":
                    showAvgAgeLvl();
                    break;
                case "7":
                    showMostFidelityUsers();
                    break;
                case "8":
                    mostUsedEquipment();
                    break;
                case "9":
                    mostVotedPresentExercises();
                    break;
                case "10":
                    showLvlUp();
                    break;
                case "11":
                    running = false;
                    System.out.println("Bye bye (￣(ｴ)￣)ﾉ");
                    break;
                case "0":
                    System.out.println("Bye bye (￣(ｴ)￣)ﾉ");
                    return false;
                default:
                    System.out.println("Please select an existing option!\n");
                    break;
            }
        }
        return true;
    }

    public void showAvgAgeLvl(){
        String threshold;

        Scanner sc = new Scanner(System.in);
        System.out.println("Insert threshold year for the age or press r to return...");
        while(true) {
            threshold = sc.next();
            if (threshold.equals("r"))
                return;
            else if(!threshold.matches("[0-9.]+"))
                System.out.println("Please insert a correct number");
            else
                break;
        }

        mongoDb.showAvgAgeLvl(threshold);
    }

    public void showLvlUp(){
        String start, end;
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;

        Scanner sc = new Scanner(System.in);
        System.out.println("Insert the starting date...");
        start = sc.next();
        System.out.println("Insert the ending date...");
        end =sc.next();


        System.out.println("The number of users that leveled up from " + start + " to " + end +":");
        System.out.printf("%22s %25s %25s", "Beginner->Intermediate", "Intermediate->Expert", "Beginner->Expert");
        System.out.println();
        neo4j.showLvlUpBI(start, end);
        neo4j.showLvlUpIE(start, end);
        neo4j.showLvlUpBE(start, end);
        System.out.println();
        System.out.println();
    }

    public void showMostFidelityUsers(){
        int num;
        System.out.println("Insert the limit of the most fidelity user you want to see or press r to return...");

        while(true) {
            Scanner sc = new Scanner(System.in);
            String input = sc.next();
            if (input.equals("r"))
                return;
            else if(!input.matches("[0-9.]+"))
                System.out.println("Please insert a number");
            else {
                num = Integer.parseInt(input);
                break;
            }
        }
        String ret = neo4j.showMostFidelityUsers(num);
        optionsUser(ret);
    }

}
