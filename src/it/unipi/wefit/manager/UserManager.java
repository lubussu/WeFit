package it.unipi.wefit.manager;

import it.unipi.wefit.entities.Exercise;
import it.unipi.wefit.entities.Workout;
import org.apache.commons.codec.digest.DigestUtils;
import org.bson.Document;
import org.bson.conversions.Bson;
import it.unipi.wefit.db.MongoDbConnector;
import it.unipi.wefit.db.Neo4jConnector;
import it.unipi.wefit.entities.Comment;
import it.unipi.wefit.entities.User;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Scanner;

public class UserManager {

    protected User self;
    protected MongoDbConnector mongoDb;
    protected Neo4jConnector neo4j;

    Scanner sc = new Scanner(System.in);
    BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));

    protected String log_mongo = "./mongodb_log.txt";
    protected String log_neo4j = "./neo4j_log.txt";

    public UserManager(User user){
        this.self = user;
        this.mongoDb =
                new MongoDbConnector("mongodb+srv://wefit:WEFIT2022@wefit2022.kxfyu.mongodb.net/wefit?authSource=admin&replicaSet=atlas-vt45gi-shard-0&w=1&readPreference=primaryPreferred&appname=MongoDB%20Compass&ssl=true",
                "wefit");
        this.neo4j = new Neo4jConnector("bolt://localhost:7687", "neo4j", "wefit" );
    }

    //function for comment a routine
    private void addComment(String routine_id) throws IOException {
        String input;
        System.out.println("Insert the comment you want to add or press r to return...");
        input = bufferRead.readLine();
        if(input.equals("r"))
            return;
        Comment c = new Comment(input, null);
        c.setUser(self.getUser_id());

        //management of the consistency among db
        if(mongoDb.insertComment(c, routine_id)) {  //mongo db correctly inserted
            if(!neo4j.insertComment(c.getUser(), routine_id)){ //neo4j not inserted
                String message = "ERROR COMMENT: Unable to insert [" + c.getUser()+ "," + routine_id+"]\n";
                saveError(log_neo4j, message);
            }
            System.out.println("Success! Your comment has been inserted\n");
            System.out.println("Press any key to continue...");
            sc.next();
        }
        else { //mongodb not inserted
            System.err.println("Unable to insert the comment!\n");
            System.out.println("Press any key to continue...");
            sc.next();
        }
    }

    //function for vote a routine
    private void addVote(String routine_id){
        System.out.println("Please insert your vote (1-5) or press r to return...");
        String vote_string = insertNumber();
        int vote = Integer.parseInt(vote_string);

        //management of the consistency among db
        if(neo4j.insertVote(self.getUser_id(), routine_id, vote)) { //neo4j correctly inserted
            if(!mongoDb.insertVote(routine_id, vote)){ //mongodb not inserted
                String message = "ERROR VOTE: Unable to insert [" + self.getUser_id()+","+routine_id+ "," + vote + "]\n";
                saveError(log_mongo, message);
            }
            System.out.println("Success! Your vote has been inserted\n");
            System.out.println("Press any key to continue...");
            sc.next();
        }
        else { //neo4j not inserted
            System.err.println("Unable to insert the vote!\n");
            System.out.println("Press any key to continue...");
            sc.next();
        }
    }

    //function for change profile's properties
    private void changeProfile() throws IOException {
        System.out.println("USER_ID: " + self.getUser_id());
        System.out.println("1)  Name: " + self.getName());
        System.out.println("2)  Gender: " + self.getGender());
        System.out.println("3)  Year of birth: " + self.getYear_of_birth());
        System.out.println("4)  Height: " + self.getHeight());
        System.out.println("5)  Weight: " + self.getWeight());
        System.out.println("6)  Training: " + self.getTrain());
        System.out.println("7)  Background: " + self.getBackground());
        System.out.println("8)  Experience: " + self.getExperience());
        System.out.println("9)  Email: " + self.getEmail());
        System.out.println("10) Password: *******");
        System.out.println("0) Save your changes\n");
        System.out.println("Select an option or press 'r' to return...");
        String input;
        User new_user = new User(self.toDocument());
        while(true) {
            input = sc.next();
            switch (input) {
                case "1": {
                    System.out.println("Insert your full name...");
                    input = bufferRead.readLine();
                    new_user.setName(input);
                    System.out.println("\nSelect another option or press 0 to save your changes\n(or press r to return)");
                    break;
                }
                case "2": {
                    System.out.println("Insert your gender...");
                    input = sc.next();
                    new_user.setGender(input);
                    System.out.println("\nSelect another option or press 0 to save your changes\n(or press r to return)");
                    break;
                }
                case "3": {
                    System.out.println("Insert your year of birth...");
                    input = insertNumber();
                    new_user.setYear_of_birth(input);
                    System.out.println("\nSelect another option or press 0 to save your changes\n(or press r to return)");
                    break;
                }
                case "4": {
                    System.out.println("Insert your height...");
                    input = insertNumber();
                    new_user.setHeight(input);
                    System.out.println("\nSelect another option or press 0 to save your changes\n(or press r to return)");
                    break;
                }
                case "5": {
                    System.out.println("Insert your weight...");
                    input = insertNumber();
                    new_user.setWeight(input);
                    System.out.println("\nSelect another option or press 0 to save your changes\n(or press r to return)");
                    break;
                }
                case "6": {
                    System.out.println("Insert your training...");
                    input = bufferRead.readLine();
                    new_user.setTrain(input);
                    System.out.println("\nSelect another option or press 0 to save your changes\n(or press r to return)");
                    break;
                }
                case "7": {
                    System.out.println("Insert your training background...");
                    input = bufferRead.readLine();
                    new_user.setBackground(input);
                    System.out.println("\nSelect another option or press 0 to save your changes\n(or press r to return)");
                    break;
                }
                case "8": {
                    System.out.println("Insert your experience...");
                    input = bufferRead.readLine();
                    new_user.setExperience(input);
                    System.out.println("\nSelect another option or press 0 to save your changes\n(or press r to return)");
                    break;
                }
                case "9": {
                    System.out.println("Insert your new email...");
                    input = sc.next();
                    new_user.setEmail(input);
                    System.out.println("\nSelect another option or press 0 to save your changes\n(or press r to return)");
                    break;
                }
                case "10": {
                    System.out.println("Insert your new password...");
                    input = new DigestUtils("SHA3-256").digestAsHex(sc.next());
                    new_user.setPassword(input);
                    System.out.println("\nSelect another option or press 0 to save your changes\n(or press r to return)");
                    break;
                }
                case "0": {
                    //management of the consistency among db
                    if (mongoDb.changeProfile(new_user)) { //mongodb correctly inserted
                        self = new_user;
                        if(!neo4j.changeProfile(new_user)){ //neo4j not inserted
                            String message = "ERROR USER: Unable to modify ["+self.getUser_id()+","+ self.getName()+","+
                                    self.getGender()+","+ self.getYear_of_birth()+","+
                                    self.getLevel()+","+self.getTrainer()+"]\n";
                            saveError(log_neo4j, message);
                        }
                        System.out.println("Success! The profile has been updated\n");

                    } else //mongodb not inserted
                        System.err.println("Unable to change the profile!\n");
                    return;
                }
                case "r":
                    return;
                default:
                    System.out.println("Please select an existing option!");
            }
        }
    }

    //return the User object of the UserManager
    public User getSelf(){
        return self;
    }

    //function for delete a comment
    private void deleteComment(String comment, String routine) {
        if(mongoDb.deleteComment(comment, routine)) {
            if(!neo4j.deleteComment(self.getUser_id(), routine)){
                String message = "ERROR COMMENT: Unable to delete [" + self.getUser_id()+ "," + routine+"]\n";
                saveError(log_neo4j, message);
            }
            System.out.println("Comment correctly deleted!\n");
            System.out.println("Press any key to continue...");
            sc.next();
        }
        else {
            System.err.println("Unable to delete the comment!\n");
            System.out.println("Press any key to continue...");
            sc.next();
        }
    }

    //function for insert a number (it asks until read is correct)
    protected String insertNumber(){
        String input;
        while(true) {
            input = sc.next();
            if (input.matches("[0-9.]+"))
                break;
            System.out.println("Please insert a correct number..");
        }
        return input;
    }

    //function for set filters for search routine(s)
    private void findRoutine() throws IOException {
        System.out.println("\nInsert filters for find a routine..");
        System.out.println("1) User");
        System.out.println("2) Trainer");
        System.out.println("3) Level");
        System.out.println("4) Vote");
        System.out.println("5) Date");
        System.out.println("0) Search routine(s)\n");
        System.out.println("Select an option or press 'r' to return...");
        ArrayList<Bson> filters = new ArrayList<>();
        String input;
        while(true) {
            input = sc.next();
            switch (input) {
                case "1": {
                    System.out.println("Insert the 'user_id' or the 'name'");
                    input = bufferRead.readLine();
                    if(input.matches("[0-9.]+"))
                        filters.add(mongoDb.getFilter("user", input, "eq"));
                    else {
                        String id;
                        Document user = mongoDb.getUser(input);
                        if(user == null){
                            System.out.println("User not found");
                            break;
                        }
                        id = user.getString("user_id");
                        filters.add(mongoDb.getFilter("user", id, "eq"));
                    }

                    System.out.println("\nInsert another filter or press 0 to search\n(or press r to return)");
                    break;
                }
                case "2": {
                    System.out.println("Insert the 'user_id' or the 'name' of the trainer");
                    input = bufferRead.readLine();
                    if(input.matches("[0-9.]+"))
                        filters.add(mongoDb.getFilter("trainer", input, "eq"));
                    else {
                        String id;
                        Document trainer = mongoDb.getUser(input);
                        if(trainer == null){
                            System.out.println("Trainer not found");
                            break;
                        }
                        id = trainer.getString("user_id");
                        filters.add(mongoDb.getFilter("user", id, "eq"));
                    }
                    System.out.println("\nInsert another filter or press 0 to search\n(or press r to return)");
                    break;
                }
                case "3": {
                    System.out.println("Insert the 'level'");
                    input = sc.next();
                    filters.add(mongoDb.getFilter("level", input, "eq"));
                    System.out.println("\nInsert another filter or press 0 to search\n(or press r to return)");
                    break;
                }
                case "4": {
                    System.out.println("Insert the 'vote'");
                    input = insertNumber();
                    System.out.println("Press 0 to find lowest votes or 1 to find highest or equal votes");
                    String x = sc.next();
                    switch (x) {
                        case "0" -> filters.add(mongoDb.getFilter("vote", Integer.parseInt(input), "lt"));
                        case "1" -> filters.add(mongoDb.getFilter("vote", Integer.parseInt(input), "gte"));
                        default -> System.out.println("Please try again");
                    }
                    System.out.println("\nInsert another filter or press 0 to search\n(or press r to return)");
                    break;
                }
                case "5": {
                    System.out.println("Insert the starting_day (YYYY-MM-DD)...");
                    input = sc.next();
                    System.out.println("Press 0 to find previous dates or 1 to find equal or later dates");
                    String x = sc.next();
                    switch (x) {
                        case "0" -> filters.add(mongoDb.getFilter("starting_day", input, "lt"));
                        case "1" -> filters.add(mongoDb.getFilter("starting_day", input, "gte"));
                        default -> System.out.println("Please try again");
                    }
                    System.out.println("\nInsert another filter or press 0 to search\n(or press r to return)");
                    break;
                }
                case "0": {
                    ArrayList<Workout> works = mongoDb.searchRoutines(filters);
                    optionsRoutines(works, true);
                }
                case "r":
                    return;
                default:
                    System.out.println("Please select an existing option!\n");
            }
        }
    }

    //function for set filters for search user(s)
    private void findUser() throws IOException {
        System.out.println("\nInsert filters for find a user or press 8 to see reccomended users..");
        System.out.println("1) User_id");
        System.out.println("2) Name");
        System.out.println("3) Gender");
        System.out.println("4) Year of birth");
        System.out.println("5) Trainer");
        System.out.println("6) Height");
        System.out.println("7) Weight");
        System.out.println("8) See reccomended users");
        System.out.println("0) Search user(s)\n");
        System.out.println("Select an option or press 'r' to return...");
        ArrayList<Bson> filters = new ArrayList<>();
        String input;
        while(true) {
            input = sc.next();
            switch (input) {
                case "1": {
                    System.out.println("Insert the 'user_id'");
                    input = insertNumber();
                    filters.add(mongoDb.getFilter("user_id", input, "eq"));

                    System.out.println("\nInsert another filter or press 0 to search\n(or press r to return)");
                    break;
                }
                case "2": {
                    System.out.println("Insert the 'name' of the user");
                    input = bufferRead.readLine();
                    filters.add(mongoDb.getFilter("name", input, "like"));

                    System.out.println("\nInsert another filter or press 0 to search\n(or press r to return)");
                    break;
                }
                case "3": {
                    System.out.println("Press 'f' to find a female user or 'm' to find a male user");
                    input= sc.next();
                    switch (input) {
                        case "f" -> filters.add(mongoDb.getFilter("gender", "Female", "eq"));
                        case "m" -> filters.add(mongoDb.getFilter("gender", "Male", "eq"));
                        default -> System.out.println("Please try again");
                    }
                    System.out.println("\nInsert another filter or press 0 to search\n(or press r to return)");
                    break;

                }
                case "4": {
                    System.out.println("Insert the 'Year of birth' of the user");
                    input = insertNumber();
                    System.out.println("Press 0 to find younger users or 1 to find oldest or peer users");
                    String x = sc.next();
                    switch (x) {
                        case "0" -> filters.add(mongoDb.getFilter("year_of_birth", input, "lt"));
                        case "1" -> filters.add(mongoDb.getFilter("year_of_birth", input, "gte"));
                        default -> System.out.println("Please try again");
                    }
                    System.out.println("\nInsert another filter or press 0 to search\n(or press r to return)");
                    break;
                }
                case "5": {
                    System.out.println("Press y to find a trainer or n to find a simple user");
                    input = sc.next();
                    switch (input) {
                        case "y" -> filters.add(mongoDb.getFilter("trainer", "yes", "eq"));
                        case "n" -> filters.add(mongoDb.getFilter("trainer", "no", "eq"));
                        default -> System.out.println("Please try again");
                    }
                    System.out.println("\nInsert another filter or press 0 to search\n(or press r to return)");
                    break;
                }
                case "6": {
                    System.out.println("Insert the 'height' of the user");
                    input = insertNumber();
                    System.out.println("Press 0 to find shorter users or 1 to find higher or equal users");
                    String x = sc.next();
                    switch (x) {
                        case "0" -> filters.add(mongoDb.getFilter("height", input, "lt"));
                        case "1" -> filters.add(mongoDb.getFilter("height", input, "gte"));
                        default -> System.out.println("Please try again");
                    }
                    System.out.println("\nInsert another filter or press 0 to search\n(or press r to return)");
                    break;
                }
                case "7": {
                    System.out.println("Insert the 'weight' of the user");
                    input = insertNumber();
                    System.out.println("Press 0 to find lighter1" +
                            " users or 1 to find heavier or equal users");
                    String x = sc.next();
                    switch (x) {
                        case "0" -> filters.add(mongoDb.getFilter("weight", input, "lt"));
                        case "1" -> filters.add(mongoDb.getFilter("weight", input, "gte"));
                        default -> System.out.println("Please try again");
                    }
                    System.out.println("\nInsert another filter or press 0 to search\n(or press r to return)");
                    break;
                }
                case "8": {
                    optionsUsers(neo4j.showRecommended(self.getUser_id()), true, true);
                    return;
                }
                case "0": {
                    optionsUsers(mongoDb.searchUsers(filters), false, true);
                    return;
                }
                case "r":
                    return;
                default:
                    System.out.println("Please select an existing option!\n");
            }
        }
    }

    //function to see top n followed users
    private void mostFollowedUsers() throws IOException {
        String input;
        System.out.println("Insert the value of n or press r to return");
        while(true){
            input = sc.next();
            if(input.equals("r"))
                return;
            if(!input.matches("[0-9.]+"))
                System.out.println("Please insert a numeric value");
            else
                break;
        }
        optionsUsers(neo4j.mostFollowedUsers(Integer.parseInt(input)), true, false);
    }

    //function to see top n rated trainers
    private void mostRatedTrainers() throws IOException {
        String input;
        System.out.println("Insert the value of n or press r to return");
        while(true){
            input = sc.next();
            if(input.equals("r"))
                return;
            if(!input.matches("[0-9.]+"))
                System.out.println("Please insert a numeric value");
            else
                break;
        }
        optionsUsers(neo4j.mostRatedTrainers(Integer.parseInt(input)), true, false);
    }

    //function to see top n rated trainers
    private void mostCommentedRoutines() throws IOException {
        String input;
        System.out.println("Insert the value of n or press r to return");
        while(true){
            input = sc.next();
            if(input.equals("r"))
                return;
            if(!input.matches("[0-9.]+"))
                System.out.println("Please insert a numeric value");
            else
                break;
        }
        optionsRoutines(neo4j.mostCommentedRoutines(Integer.parseInt(input)), false);
    }

    //function for all the possible operations on a routine
    protected String optionsRoutine(Workout w) throws IOException {
        System.out.println("""
                
                Press 0 to return to main menu
                Press 1 to search an exercise
                Press 2 to comment the routine
                Press 3 to vote the routine
                Press 4 to see routine's comments
                Or press another key to continue""");
        String input = sc.next();
        switch (input) {
            case "0":
                return "0";
            case "1": {
                System.out.println("Insert the exercise name");
                String exercise="";
                try {
                    exercise = bufferRead.readLine();
                } catch (IOException e) { e.printStackTrace();}
                selectExercise(mongoDb.searchExercises(exercise,null, null), true);
                return "";
            }
            case "2":
                addComment(w.getId());
                return "";
            case "3":
                addVote(w.getId());
                return "";
            case "4":
                ArrayList<Comment> comms = mongoDb.showComments(w.getId());
                if(comms.size()==0)
                    return "";
                PrintManager.printComments(comms,10);
                Comment c = selectComment(comms);
                if(c==null)
                    return "";
                else
                    deleteComment(c.getId(), w.getId());
                return "";
            default: return "c";
        }
    }

    //function to manage the operation on a list of routines (select one....)
    protected void optionsRoutines(ArrayList<Workout> works, boolean print) throws IOException {
        while (true) {
            if(print)
                PrintManager.printRoutines(works, 10);
            Workout w = selectRoutine(works);
            if (w == null)
                return;
            while (true) {
                w.printDetails();
                String ret = optionsRoutine(w);
                switch (ret) {
                    case "0": //return to the main menu
                        return;
                    case "c": //return to the list of routines
                        break;
                    default: //continue..
                        w = new Workout(mongoDb.getRoutine(w.getId())); //search again the workout (the vote can be changed)
                        continue;
                }
                break;
            }
        }
    }

    //function for all the possible operations on a user
    protected String optionsUser(User u, boolean b) throws IOException {
        String id = u.getUser_id();
        String trainer = u.getTrainer();
        System.out.println("""
                Press 0 to return to main menu
                Press 1 to see user's details
                press 2 to see user's routines
                press 3 to follow the user
                press 4 to unfollow the user""");

        String input = sc.next();
        switch (input){
            case "0":
                return "0"; //return to the main menu
            case "1":
                while(true) {
                    if(b)
                        u = new User(mongoDb.getUser(u.getUser_id()));
                    u.printDetails();
                    System.out.println("""
                            
                            Press 0 to return to main menu
                            Press 1 to FOLLOW the user
                            Press 2 to UNFOLLOW the user
                            Or press another key to continue""");

                    input = sc.next();
                    switch (input) {
                        case "0":
                            return "0"; //return to the main menu
                        case "1": //follow the user
                            if(neo4j.followUser(self.getUser_id(), id.replace("\"","")))  //follow user
                                System.out.println(u.getName() + " successfully follow");
                            else
                                System.err.println("Unable to follow "+ u.getName());
                            System.out.println("Press any key to continue...");
                            sc.next();
                            break;
                        case "2": //unfollow the user
                            if(neo4j.unfollowUser(self.getUser_id(), id.replace("\"",""))) //unfollow user
                                System.out.println(u.getName() + " successfully unfollow");
                            else
                                System.err.println("Unable to unfollow "+ u.getName());
                            System.out.println("Press any key to continue...");
                            sc.next();
                            break;
                        default: return "c";
                    }
                }
            case "2": {
                if (trainer.equals("yes")){ //the user is a trainer
                    System.out.println("Press 1 to see own routines\n"+
                            "or press 2 to see routine's created");

                    input = sc.next();
                    switch (input) {
                        case "1" -> //se own routines
                                optionsRoutines(neo4j.showRoutines(id.replace("\"", ""), "all"), true);
                        case "2" -> //see created routines
                                optionsRoutines(neo4j.showCreatedRoutines(id.replace("\"", "")), true);
                    }
                    break;
                }
                else { // the user is not a trainer, see his routines
                    optionsRoutines(neo4j.showRoutines(id.replace("\"", ""), "all"), true);
                    break;
                }
            }
            case "3": //follow the user (without see details)
                if(neo4j.followUser(self.getUser_id(), id.replace("\"","")))  //follow user
                    System.out.println(u.getName() + " successfully follow");
                else
                    System.err.println("Unable to follow "+ u.getName());
                System.out.println("Press any key to continue...");
                sc.next();
                break;
            case "4": //unfollow the user (without see details)
                if(neo4j.unfollowUser(self.getUser_id(), id.replace("\"",""))) //unfollow user
                    System.out.println(u.getName() + " successfully unfollow");
                else
                    System.err.println("Unable to unfollow "+ u.getName());
                System.out.println("Press any key to continue...");
                sc.next();
                break;
            default:
                return "c";
        }
        return "c";
    }

    //function to manage the operation on a list of routines (select one....)
    protected void optionsUsers(ArrayList<User> users, boolean b, boolean print) throws IOException {
        while (true) {
            if(print)
                PrintManager.printUsers(users, 10);
            User u = selectUser(users);
            if (u == null)
                return;
            while (true) {
                String ret = optionsUser(u, b); //menu for a single user
                switch (ret) {
                    case "0": //return to the main menu
                        return;
                    case "c": //return to the list of users
                        break;
                    default: //continue..
                        continue;
                }
                break;
            }
        }
    }

    //function to save write operations errors on a log file
    protected void saveError(String file, String message){
        try{
            Files.write(Paths.get(file), message.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.out.println("Unable to save error on file\n");
        }
    }

    //function to select a routine from given routines
    protected Comment selectComment(ArrayList<Comment> comms){
        String input;
        while (true) {
            System.out.println("If you want to delete your comment press the number of the comment\n" +
                    "Press 0 to continue");

            input = sc.next();
            if (!input.matches("[0-9.]+"))
                System.out.println("Please select an existing option!");
            else if ((Integer.parseInt(input)) > comms.size())
                System.out.println("Please select an existing option!\n");
            else
                break;
        }
        if ("0".equals(input)) {
            return null;
        }
        else if (!self.getUser_id().equals(comms.get(Integer.parseInt(input) - 1).getUser())) {
            System.out.println("You can't delete this comment!\n");
            System.out.println("Press any key to continue..");
            sc.next();
            return null;
        } else
            return comms.get(Integer.parseInt(input) - 1);
    }

    //function to select an exercise from given exercises
    protected Exercise selectExercise(ArrayList<Exercise> exs, boolean print){
        PrintManager.printExercises(exs, 10);
        String input;
        while (true) {
            System.out.println("""
                    
                    Press the number of the exercise you want to select
                    or press 0 to return""");

            input = sc.next();
            if (!input.matches("[0-9.]+"))
                System.out.println("Please select an existing option!");
            else if ((Integer.parseInt(input)) > exs.size())
                System.out.println("Please select an existing option!\n");
            else
                break;
        }
        if ("0".equals(input)) {
            return null;
        }
        if (print) {
            exs.get(Integer.parseInt(input) - 1).printDetails();

            System.out.println("\nPress any key to return");
            sc.next();
        }
        return exs.get(Integer.parseInt(input) - 1);
    }

    //function to select a routine from given routines
    protected Workout selectRoutine(ArrayList<Workout> works){
        String input;
        while (true) {
            System.out.println("Press the number of the routine you want to select\n" +
                    "or press 0 to return");

            input = sc.next();
            if (!input.matches("[0-9.]+"))
                System.out.println("Please select an existing option!");
            else if ((Integer.parseInt(input)) > works.size())
                System.out.println("Please select an existing option!\n");
            else
                break;
        }
        if ("0".equals(input)) {
            return null;
        }
        return new Workout(mongoDb.getRoutine(works.get(Integer.parseInt(input) - 1).getId()));
    }

    //function to select a user from given users
    protected User selectUser(ArrayList<User> us) {
        String input;
        while (true) {
            System.out.println("Press the number of the user you want to select\n" +
                    "or press 0 to return to the main menu");

            input = sc.next();
            if(input.equals("r"))
                return null;
            if (!input.matches("[0-9.]+"))
                System.out.println("Please select an existing option!");
            else if ((Integer.parseInt(input)) > us.size())
                System.out.println("Please select an existing option!\n");
            else
                break;
        }
        if ("0".equals(input)) {
            return null;
        }
        return us.get(Integer.parseInt(input) - 1);
    }

    //session function (user menu)
    public boolean session() throws IOException {
        boolean running = true;
        while(running) {
            System.out.println("""
                    
                    What do you need?
                    1)  See your current routine
                    2)  See your past routines
                    3)  See your followed list
                    4)  See your followers list
                    5)  See routines you commented
                    6)  Find a routine by parameter
                    7)  Find a user by parameter
                    8)  Find n-most rated personal trainer
                    9)  Find n-most followed users
                    10) Find n-most commented routines
                    11) Modify your profile
                    12) Log out
                    0)  Exit""");
            String input = sc.next();
            switch (input) {
                case "1" -> showCurrentRoutine();
                case "2" -> showPastRoutines();
                case "3" -> showFollowedUsers();
                case "4" -> showFollowers();
                case "5" -> showCommentedRoutines();
                case "6" -> findRoutine();
                case "7" -> findUser();
                case "8" -> mostRatedTrainers();
                case "9" -> mostFollowedUsers();
                case "10" -> mostCommentedRoutines();
                case "11" -> changeProfile();
                case "12" -> {
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

    //function for signin the app
    public String signIn(){
        String email, password;
        System.out.println("Please insert your email...");
        Scanner sc = new Scanner(System.in);
        email = sc.next();
        System.out.println("now insert your password...");
        password = new DigestUtils("SHA3-256").digestAsHex(sc.next());

        try {
            self = new User(mongoDb.signIn(email, password));
        }catch(NullPointerException npe){
            System.out.println("Incorrect email or password, please retry!");
            System.out.println();
            return null;
        }
        System.out.println("\nWELCOME " + self.getName());
        return self.getTrainer();
    }

    //function for signup to the app
    public boolean signUp() throws IOException {
        String name, gender, yob, height, weight, training, bg, exp, email, password, level;
        System.out.println("""
                Your are signing-up as a new user, please insert your credentials
                If you want to return press r
                """);

        System.out.println("Insert your name...");
        name = bufferRead.readLine();
        if(name.equals("r"))
            return true;
        System.out.println("Insert your gender...");
        gender = sc.next();
        if(gender.equals("r"))
            return true;
        gender = gender.replace(gender.substring(0,1), gender.substring(0,1).toUpperCase());
        System.out.println("Insert your year of birth...");
        yob = insertNumber();
        System.out.println("Insert your height...");
        height = insertNumber();
        System.out.println("Insert your weight...");
        weight = insertNumber();
        System.out.println("Insert your level (Beginner/Intermediate/Expert)...");
        while(true) {
            level = sc.next();
            if (level.equals("r"))
                return true;
            level = level.replace(level.substring(0, 1), level.substring(0, 1).toUpperCase());
            if(!level.equals("Beginner") && !level.equals("Intermediate") && !level.equals("Expert"))
                System.out.println("Please try again");
            else
                break;
        }

        System.out.println("Describe your current training routine...");
        training =bufferRead.readLine();
        if(training.equals("r"))
            return true;
        System.out.println("Describe your training or sportive background...");
        bg = bufferRead.readLine();
        if(bg.equals("r"))
            return true;
        System.out.println("Describe your athletic experiences...");
        exp = bufferRead.readLine();
        if(exp.equals("r"))
            return true;
        System.out.println("Insert your email...");
        email = sc.next();
        if(email.equals("r"))
            return true;
        System.out.println("Choose a password...");
        password = sc.next();
        if(password.equals("r"))
            return true;
        password = new DigestUtils("SHA3-256").digestAsHex(password);
        int user = mongoDb.lastUser();
        if (user == -1){
            System.err.println("Error! Unable to signUp\n");
            return true;
        }

        self = new User(name, gender, yob, height, weight, training, bg, exp, email, password, level, "no", Integer.toString(user));

        //management of the consistency among db
        if(mongoDb.insertUser(self)){ //mongodb correctly inserted
            if(neo4j.insertUser(self)) //neo4j correctly inserted
                System.out.println("""

                        Nice, from now on your are a member of the WeFit community,
                        soon one of our trainer will contact you to assign a training level
                        and build a personal routine with you!
                        We hope your stay here will be a pleasurable one!
                        """);
            else { //neo4j not inserted
                if(!mongoDb.deleteUser(self)){
                    String message = "ERROR USER: Unable to create ["+self.getUser_id()+","+ self.getName()+","+
                            self.getGender()+","+ self.getYear_of_birth()+","+
                            self.getLevel()+","+self.getTrainer()+"]\n";
                    saveError(log_neo4j, message);
                }
                else {
                    System.err.println("Error! Unable to signUp\n");
                    return true;
                }
            }
        }
        else{
            System.err.println("Error! Unable to signUp\n");
            return true;
        }

        return session(); //exit the application
    }

    //show routines commented by the logged user
    private void showCommentedRoutines() throws IOException {
        optionsRoutines(neo4j.showCommentedRoutines(self.getUser_id()), true);
    }

    //function to see the current routine
    private void showCurrentRoutine() throws IOException {
        optionsRoutines(neo4j.showRoutines(self.getUser_id(), "current"), true);
    }

    //function to list of followed users
    private void showFollowedUsers() throws IOException {
        optionsUsers(neo4j.showFollowUsers(self.getUser_id(), "followed"), true, true);
    }

    //function to see list of followers
    private void showFollowers() throws IOException {
        optionsUsers(neo4j.showFollowUsers(self.getUser_id(), "followers"), true, true);
    }

    //function to see past routines
    private void showPastRoutines() throws IOException {
        optionsRoutines(neo4j.showRoutines(self.getUser_id(), "past"), true);
    }

}
