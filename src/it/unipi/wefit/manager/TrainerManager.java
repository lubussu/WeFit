package it.unipi.wefit.manager;

import it.unipi.wefit.db.*;
import it.unipi.wefit.entities.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

public class TrainerManager extends UserManager{

    private final String[] Muscles = {"Shoulders", "Traps", "Biceps", "Neck", "Lower Back", "Adductors", "Forearms", "Hamstrings", "Lats", "Middle Back", "Glutes", "Chest", "Abdominals", "Quadriceps", "Abductors", "Calves", "Triceps"};
    private final String[] Levels = {"Beginner", "Intermediate", "Expert"};

    public TrainerManager(User trainer, MongoDbConnector mongo){
        super(trainer, mongo);
    }

    //function to add a new exercise
    public void addExercise() throws IOException {
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
            String input;
            switch (r) {
                case 0 -> {
                    System.out.println("\nInsert the name of the new exercise or press 'r' to return...");
                    input = bufferRead.readLine();
                    if (input.equals("r"))
                        return;
                    exerciseName = input;
                    r++;
                }
                case 1 -> {
                    System.out.println("\nInsert the type of the new exercise or press 'r' to return...");
                    input = bufferRead.readLine();
                    if (input.equals("r"))
                        return;
                    exerciseType = input;
                    r++;
                }
                case 2 -> {
                    System.out.println("\nSelect the number of the muscle target of the new exercise or press 'r' to return...");
                    for (int i = 0; i < Muscles.length; i++) {
                        System.out.println(i + ") " + Muscles[i]);
                    }
                    input = bufferRead.readLine();
                    if (input.equals("r"))
                        return;
                    if (Integer.parseInt(input) > Muscles.length) {
                        System.out.println("\nMuscle target not found... please select again");
                        r = 2;
                    } else {
                        for (int j = 0; j < Muscles.length; j++) {
                            if (Integer.parseInt(input) == j) {
                                exerciseMuscle = Muscles[j];
                                r++;
                            }
                        }
                    }
                }
                case 3 -> {
                    System.out.println("\nInsert the equipment of the new exercise or press 'r' to return...");
                    input = bufferRead.readLine();
                    if (input.equals("r"))
                        return;
                    exerciseEquipment = input;
                    r++;
                }
                case 4 -> {
                    System.out.println("\nSelect the level of the new exercise or press 'r' to return...");
                    for (int i = 0; i < 3; i++) {
                        System.out.println(i + ") " + Levels[i]);
                    }
                    input = bufferRead.readLine();
                    if (input.equals("r"))
                        return;
                    if (Integer.parseInt(input) > 3) {
                        System.out.println("\nLevel not valid... please select again");
                        r = 4;
                    } else {
                        for (int j = 0; j < 3; j++) {
                            if (Integer.parseInt(input) == j) {
                                exerciseLevel = Levels[j];
                                r++;
                            }
                        }
                    }
                }
                case 5 -> {
                    System.out.println("\nInsert the first image's link of the new exercise or press 'r' to return...");
                    input = bufferRead.readLine();
                    if (input.equals("r"))
                        return;
                    exerciseImage1 = input;
                    r++;
                }
                case 6 -> {
                    System.out.println("\nInsert the second image's link of the new exercise or press 'r' to return...");
                    input = bufferRead.readLine();
                    if (input.equals("r"))
                        return;
                    exerciseImage2 = input;
                    r++;
                }
                case 7 -> {
                    System.out.println("\nWrite an explanation of the new exercise or press 'r' to return...");
                    input = bufferRead.readLine();
                    if (input.equals("r"))
                        return;
                    exerciseDetails = input;
                    r++;
                }
            }
        }
        System.out.println("Press any key to insert or press 'r' to return..");
        if(sc.next().equals("r"))
            return;

        Exercise new_ex = new Exercise(exerciseName,exerciseType,exerciseMuscle, exerciseEquipment,
                                exerciseLevel,exerciseImage1,exerciseImage2,exerciseDetails);

        mongoDb.insertNewExercise(new_ex);
    }

    //change a user from normal user to trainer
    public void addTrainer() throws IOException {
        System.out.println("Insert the name or the user_id of the user you want to promote or press 'r' to return..");

        String input = bufferRead.readLine();
        if(input.equals("r"))
            return;

        User user = new User(mongoDb.getUser(input));

        user.setTrainer("yes");
        //management of the consistency
        if(neo4j.changeProfile(user)) { //neo4j correctly modify
            if(!mongoDb.changeProfile(user)){ //mongodb not modify
                String message = "ERROR USER: Unable to promote ["+self.getUser_id()+"]\n";
                saveError(log_mongo, message);
            }
            System.out.println("Success! The profile has been updated\n");
        }
        else
            System.err.println("Unable to change the profile!\n");
    }

    // Create a routine for a user
    public void createRoutine() throws IOException {

        // insert name and query the db for the user
        System.out.println("\nInsert the name of the user or the user_id or press 'r' to return...");
        String search_name;
        search_name = bufferRead.readLine();
        if(search_name.equals("r"))
            return;
        String user = search_name;
        if(!search_name.matches("[0-9.]+")) {
            User u = new User(mongoDb.getUser(search_name));
            if(u != null)
                user = u.getUser_id();
            else {
                System.out.println("User not found");
                return;
            }
        }

        // create variables for building the document
        ArrayList<Exercise> exercises = new ArrayList<>();
        ArrayList<Exercise> warmup = new ArrayList<>();
        ArrayList<Exercise> stretching = new ArrayList<>();
        int running = 0;
        int n = 0;

        // cycle all the 17 muscle groups to insert one exercise each
        while(n<17){
            Exercise e = insertExercise(Muscles[n],null);
            if(e==null)
                return;
            exercises.add(e);
            n++;
        }

        // two distinct cycles merged to build the warmup and stretching routines
        while(running<2){
            Exercise exercise = null;
            String fetch;
            if(running == 0){
                System.out.print("\nAdding warm_up exercises..");
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


        System.out.println("Insert the level of the routine or press 'r' to return...");
        String level = sc.next();
        if(level.equals("r"))
            return;
        level = level.replace(level.substring(0,1), level.substring(0,1).toUpperCase());

        System.out.println("Insert the work time (sec)...");
        String work = insertNumber();

        System.out.println("Insert the rest time (sec)...");
        String rest = insertNumber();

        System.out.println("Insert the number of repetitions of the routine...");
        String rep = insertNumber();

        Workout workout = new Workout(null, user, self.getUser_id(), level,Integer.parseInt(work), Integer.parseInt(rest),
                Integer.parseInt(rep), warmup,exercises,stretching, LocalDate.now().toString(),
                LocalDate.now().plusMonths(1).toString(), null, 0, 0);


        System.out.println("Press any key to insert or press 'r' to return..");

        if(sc.next().equals("r"))
            return;

        //management of consistency between db
        String id = mongoDb.insertRoutine(workout);
        if(id != null) { //mongodb correctly inserted
            if(neo4j.insertRoutine(workout, id)) //neo4j correctly inserted
                System.out.println("Success! The routine has been inserted\n");
            else {
                if(!mongoDb.deleteRoutine(workout)) {
                    String message = "ERROR ROUTINE: Unable to create ["+workout.getId()+","+
                            workout.getUser()+","+workout.getTrainer()+","+workout.getLevel()+","+
                            workout.getStarting_day()+","+workout.getEnd_day()+"]\n";
                    saveError(log_neo4j, message);
                }
                else
                    System.err.println("Error! Unable to insert the routine\n");
            }
        }
        else
            System.err.println("Error! Unable to insert the routine\n");
    }

    //function for insert an exercise in a new routine
    public Exercise insertExercise(String muscle, String type) throws IOException {
        Exercise exercise;
        String fetch;
        while(true) {
            if(muscle == null)  System.out.println("\nInsert an exercise's name or press 'r' to return..");
            else System.out.println("\nInsert an exercise's name per muscle " + muscle + " or press 'r' to return...");

            fetch = bufferRead.readLine();
            if(fetch.equals("r"))
                return null;
            exercise = selectExercise(mongoDb.searchExercises(fetch, muscle, type), false);
            if(exercise==null)
                continue;
            break;
        }
        System.out.println(exercise.getName()+ " added\n");

        System.out.println("Insert exercise's weight or 'n' if weight is not present...");
        fetch = sc.next();
        if(!fetch.equals("n"))
            exercise.setWeight(Integer.parseInt(fetch));

        return exercise;
    }

    //function that find the most used equipment in general and for the selected muscle group
    public void mostUsedEquipment() throws IOException {
        System.out.printf("%15s %15s %10s", "MUSCLE", "EQUIPMENT", "COUNT");
        System.out.println("\n----------------------------------------------");
        mongoDb.mostUsedEquipment(null);
        while(true){
            System.out.println("\nInsert the muscle you want to see or press r to return...");
            for (int i = 0; i < Muscles.length; i++) {
                System.out.println(i + ") " + Muscles[i]);
            }
            String input = bufferRead.readLine();
            if(input.equals("r"))
                return;
            if(!input.matches("[0-9.]+") || Integer.parseInt(input)>= Muscles.length){
                System.out.println("Please select an existing option...");
                continue;
            }
            System.out.printf("%15s %15s %10s", "MUSCLE", "EQUIPMENT", "COUNT");
            System.out.println("\n----------------------------------------------");
            mongoDb.mostUsedEquipment(Muscles[Integer.parseInt(input)]);
        }
    }

    //function for show the most present exercise in the top-n rated routines
    public void mostVotedPresentExercises(){
        String s;

        System.out.println("Insert the number of routine you want to consider...\nand the number of exercises you want to show" +
                "or press 'r' to return...");
        s = sc.next();
        if(s.equals("r")) return;
        int max_vote = Integer.parseInt(s);
        s= sc.next();
        if(s.equals("r")) return;
        int max_ex = Integer.parseInt(s);

        mongoDb.mostVotedPresentExercises(max_vote, max_ex);

        System.out.println("\nPress any key to continue...");
        sc.next();
    }

    public boolean sessionTrainer() throws IOException {
        System.out.println("WELCOME " + self.getName());
        boolean running = true;
        while(running) {
            System.out.println("""
                    What do you need?
                    1)  See your routines
                    2)  Add a new routine
                    3)  Add a new exercise
                    4)  Add a new trainer
                    5)  See normal user menu
                    ----------ANALYTICS----------
                    6)  See average age per level
                    7)  Find most fidelity users
                    8)  Find most used equipments per muscle
                    9)  Find most common exercises in most rated routines
                    10) Show level-ups
                    -----------------------------
                    11) Log out
                    0)  Exit the app""");
            String input = sc.next();
            switch (input) {
                case "1" -> showCreatedRoutines();
                case "2" -> createRoutine();
                case "3" -> addExercise();
                case "4" -> addTrainer();
                case "5" -> session();
                case "6" -> showAvgAgeLvl();
                case "7" -> showMostFidelityUsers();
                case "8" -> mostUsedEquipment();
                case "9" -> mostVotedPresentExercises();
                case "10" -> showLvlUp();
                case "11" -> {
                    running = false;
                    System.out.println("Bye bye\n");
                }
                case "0" -> {
                    System.out.println("Bye bye\n");
                    return false;
                }
                default -> System.out.println("Please select an existing option!\n");
            }
        }
        return true;
    }

    //function that show the average age for each level and the most common level for the selected age
    public void showAvgAgeLvl(){
        String threshold;

        System.out.println("Insert the age you want to consider or press 'r' to return...");
        while(true) {
            threshold = sc.next();
            if (threshold.equals("r"))
                return;
            else if(!threshold.matches("[0-9.]+"))
                System.out.println("Please insert a correct number");
            else
                break;
        }
        mongoDb.showAvgAgeLvl(Integer.toString(LocalDate.now().getYear()-Integer.parseInt(threshold)));
        System.out.println("\nPress any key to continue...");
        sc.next();
    }

    //function to see routines created by the trainer
    public void showCreatedRoutines() throws IOException {
        optionsRoutines(neo4j.showCreatedRoutines(self.getUser_id()));
    }

    //function that show how many users levelled up in the ginven period
    public void showLvlUp(){
        String start, end;

        System.out.println("Insert the starting date (yyyy-mm-dd)...");
        start = sc.next();
        System.out.println("Insert the ending date (yyyy-mm-dd)...");
        end =sc.next();


        System.out.println("\nThe number of users that leveled up from " + start + " to " + end +":\n");
        System.out.printf("%22s %25s %25s", "Beginner->Intermediate", "Intermediate->Expert", "Beginner->Expert");
        System.out.println();
        neo4j.showLvlUpBI(start, end);
        neo4j.showLvlUpIE(start, end);
        neo4j.showLvlUpBE(start, end);
        System.out.println();
        System.out.println("\nPress any key to continue...");
        sc.next();
    }

    //function to show users with the high number of past routines (or with the previous start_day of the first routine)
    public void showMostFidelityUsers() throws IOException {
        int num;
        System.out.println("Insert the number of user you want to see or press r to return...");

        while(true) {
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
        optionsUsers(neo4j.showMostFidelityUsers(num), true, false);
        System.out.println("Press any key to continue...");
        sc.next();
    }

}
