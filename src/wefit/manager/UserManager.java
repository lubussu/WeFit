package wefit.manager;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import wefit.db.MongoDbConnector;
import wefit.db.Neo4jConnector;
import wefit.entities.User;

import javax.swing.text.html.HTMLDocument;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

import static com.mongodb.client.model.Aggregates.*;
import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Projections.exclude;
import static com.mongodb.client.model.Projections.fields;

public class UserManager {

    protected User self;
    protected MongoDbConnector mongoDb;
    protected Neo4jConnector neo4j;

    public UserManager(User user, MongoDbConnector mongo){
        this.self = user;
        this.mongoDb = mongo;
        this.neo4j = neo4j = new Neo4jConnector("bolt://localhost:7687", "neo4j", "wefit" );
    }

    //show routines commented by the logged user
    public void showRoutinesCommented(String userID) {
        neo4j.searchRoutinesCommentedByUser(userID);
    }

    //function for comment a routine
    public void addComment(String routine_id) throws IOException {
        Document comment = new Document();
        BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
        String input = null;
        System.out.println("Insert the comment you want to add or press r to return...");
        input = bufferRead.readLine();
        if(input.equals("r"))
            return;
        comment.append("Comment", input).append("Time", LocalDate.now().toString()).append("user", self.getUser_id());
        mongoDb.insertComment(comment, routine_id);
    }

    //function for vote a routine
    public void addVote(String routine_id){
        Scanner sc = new Scanner(System.in);
        System.out.println("Please insert your vote or press r to return...");
        String vote_string = sc.next();
        if(vote_string.equals("r"))
            return;
        int vote = Integer.parseInt(vote_string);
        mongoDb.insertVote(routine_id, vote);
        neo4j.insertVote(self.getUser_id(), routine_id, vote);
    }

    //function for change profile's properties
    public void changeProfile() throws IOException {
        System.out.println("USER_ID: " + self.getUser_id());
        System.out.println("1) Name: " + self.getName());
        System.out.println("2) Gender: " + self.getGender());
        System.out.println("3) Year of birth: " + self.getYear_of_birth());
        System.out.println("4) Height: " + self.getHeight());
        System.out.println("5) Weight: " + self.getWeight());
        System.out.println("6) Training: " + self.getTrain());
        System.out.println("7) Background: " + self.getBackground());
        System.out.println("8) Experience: " + self.getExperience());
        System.out.println("9) Email: " + self.getEmail());
        System.out.println("10) Password: " + self.getPassword());
        System.out.println("0) Save your changes\n");
        System.out.println("Select an option or press \'r\' to return...");
        Scanner sc = new Scanner(System.in);
        String input;
        while(true) {
            BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
            input = sc.next();
            switch (input) {
                case "1": {
                    System.out.println("Insert your full name...");
                    input = bufferRead.readLine();
                    self.setName(input);
                    System.out.println("\nSelect another option or press 0 to save your changes\n(or press r to return)");
                    break;
                }
                case "2": {
                    System.out.println("Insert your gender...");
                    input = sc.next();
                    self.setGender(input);
                    System.out.println("\nSelect another option or press 0 to save your changes\n(or press r to return)");
                    break;
                }
                case "3": {
                    System.out.println("Insert your year of birth...");
                    input = sc.next();
                    self.setYear_of_birth(input);
                    System.out.println("\nSelect another option or press 0 to save your changes\n(or press r to return)");
                    break;
                }
                case "4": {
                    System.out.println("Insert your height...");
                    input = sc.next();
                    self.setHeight(input);
                    System.out.println("\nSelect another option or press 0 to save your changes\n(or press r to return)");
                    break;
                }
                case "5": {
                    System.out.println("Insert your weight...");
                    input = sc.next();
                    self.setWeight(input);
                    System.out.println("\nSelect another option or press 0 to save your changes\n(or press r to return)");
                    break;
                }
                case "6": {
                    System.out.println("Insert your training...");
                    input = bufferRead.readLine();
                    self.setTrain(input);
                    System.out.println("\nSelect another option or press 0 to save your changes\n(or press r to return)");
                    break;
                }
                case "7": {
                    System.out.println("Insert your training background...");
                    input = bufferRead.readLine();
                    self.setBackground(input);
                    System.out.println("\nSelect another option or press 0 to save your changes\n(or press r to return)");
                    break;
                }
                case "8": {
                    System.out.println("Insert your experience...");
                    input = bufferRead.readLine();
                    self.setExperience(input);
                    System.out.println("\nSelect another option or press 0 to save your changes\n(or press r to return)");
                    break;
                }
                case "9": {
                    System.out.println("Insert your new email...");
                    input = sc.next();
                    self.setEmail(input);
                    System.out.println("\nSelect another option or press 0 to save your changes\n(or press r to return)");
                    break;
                }
                case "10": {
                    System.out.println("Insert your new password...");
                    input = sc.next();
                    self.setPassword(input);
                    System.out.println("\nSelect another option or press 0 to save your changes\n(or press r to return)");
                    break;
                }
                case "0":
                    mongoDb.changeProfile(self);
                    neo4j.changeProfile(self);
                    return;
                case "r":
                    return;
            }
        }
    }

    //function for set filters for search routine(s)
    public void findRoutine() throws IOException {
        System.out.println("\nInsert filters for find a routine..");
        System.out.println("1) User");
        System.out.println("2) Trainer");
        System.out.println("3) Level");
        System.out.println("4) Vote");
        System.out.println("5) Data");
        System.out.println("0) Search routine(s)\n");
        System.out.println("Select an option or press \'r\' to return...");
        Scanner sc = new Scanner(System.in);
        ArrayList<Bson> filters = new ArrayList<>();
        String input;
        while(true) {
            BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
            input = sc.next();
            switch (input) {
                case "1": {
                    System.out.println("Insert the \'user_id\' or the \'name\'");
                    input = bufferRead.readLine();
                    if(input.matches("[0-9.]+"))
                        filters.add(mongoDb.getFilter("user", input, "eq"));
                    else {
                        String id=null;
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
                    System.out.println("Insert the \'user_id\' or the \'name\' of the trainer");
                    input = bufferRead.readLine();
                    if(input.matches("[0-9.]+"))
                        filters.add(mongoDb.getFilter("trainer", input, "eq"));
                    else {
                        String id=null;
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
                    System.out.println("Insert the \'level\'");
                    input = sc.next();
                    filters.add(mongoDb.getFilter("level", input, "eq"));
                    System.out.println("\nInsert another filter or press 0 to search\n(or press r to return)");
                    break;
                }
                case "4": {
                    System.out.println("Insert the \'vote\'");
                    input = sc.next();
                    if(!input.matches("[0-5.]"))
                        System.out.println("Please insert a number between 1 and 5");
                    System.out.println("Press 0 to find lowest votes or 1 to find highest or equal votes");
                    String x = sc.next();
                    switch (x){
                        case "0":
                            filters.add(mongoDb.getFilter("vote", Integer.parseInt(input), "lt"));
                            break;
                        case"1":
                            filters.add(mongoDb.getFilter("vote", Integer.parseInt(input), "gte"));
                            break;
                        default:
                            System.out.println("Please try again");
                    }
                    System.out.println("\nInsert another filter or press 0 to search\n(or press r to return)");
                    break;
                }
                case "5": {
                    System.out.println("Insert the starting_day (YYYY-MM-DD)...");
                    input = sc.next();
                    System.out.println("Press 0 to find previous dates or 1 to find equal or later dates");
                    String x = sc.next();
                    switch (x){
                        case "0":
                            filters.add(mongoDb.getFilter("starting_day", input, "lt"));
                            break;
                        case"1":
                            filters.add(mongoDb.getFilter("starting_day", input, "gte"));
                            break;
                        default:
                            System.out.println("Please try again");
                    }
                    System.out.println("\nInsert another filter or press 0 to search\n(or press r to return)");
                    break;
                }
                case "0":
                    String option = mongoDb.searchRoutines(filters);
                    if(option==null)
                        return;
                    else if(option.startsWith("c:"))
                        addComment(option.substring(2));
                    else if(option.startsWith(option.substring(2)))
                        addVote(option.substring(2));
                    return;
                case "r":
                    return;
                default:
                    System.out.println("Please select an existing option!\n");
            }
        }
    }

    //function for set filters for search user(s)
    public void findUser() throws IOException {
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
        System.out.println("Select an option or press \'r\' to return...");
        Scanner sc = new Scanner(System.in);
        ArrayList<Bson> filters = new ArrayList<>();
        String input;
        while(true) {
            BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
            input = sc.next();
            switch (input) {
                case "1": {
                    System.out.println("Insert the \'user_id\'");
                    input = bufferRead.readLine();
                    filters.add(mongoDb.getFilter("user_id", input, "eq"));

                    System.out.println("\nInsert another filter or press 0 to search\n(or press r to return)");
                    break;
                }
                case "2": {
                    System.out.println("Insert the \'name\' of the user");
                    input = bufferRead.readLine();
                    filters.add(mongoDb.getFilter("name", input, "eq"));

                    System.out.println("\nInsert another filter or press 0 to search\n(or press r to return)");
                    break;
                }
                case "3": {
                    System.out.println("Press \'f\' to find a female user or \'m\' to find a male user");
                    input= sc.next();
                    switch (input) {
                        case "f":
                            filters.add(mongoDb.getFilter("gender", "Female", "eq"));
                            break;
                        case "m":
                            filters.add(mongoDb.getFilter("gender", "Male", "eq"));
                            break;
                        default:
                            System.out.println("Please try again");
                    }
                    System.out.println("\nInsert another filter or press 0 to search\n(or press r to return)");
                    break;

                }
                case "4": {
                    System.out.println("Insert the \'Year of birth\' of the user");
                    while(true){
                        input = sc.next();
                        if(!input.matches("[0-9]+")) {
                            System.out.println("Please insert a correct year");
                            continue;
                        }
                        break;
                    }
                    System.out.println("Press 0 to find younger users or 1 to find oldest or peer users");
                    String x = sc.next();
                    switch (x){
                        case "0":
                            filters.add(mongoDb.getFilter("year_of_birth", input, "lt"));
                            break;
                        case"1":
                            filters.add(mongoDb.getFilter("year_of_birth", input, "gte"));
                            break;
                        default:
                            System.out.println("Please try again");
                    }
                    System.out.println("\nInsert another filter or press 0 to search\n(or press r to return)");
                    break;
                }
                case "5": {
                    System.out.println("Press y to find a trainer or n to find a simple user");
                    input = sc.next();
                    switch (input){
                        case "y":
                            filters.add(mongoDb.getFilter("trainer", "yes", "eq"));
                            break;
                        case"n":
                            filters.add(mongoDb.getFilter("trainer", "no", "eq"));
                            break;
                        default:
                            System.out.println("Please try again");
                    }
                    System.out.println("\nInsert another filter or press 0 to search\n(or press r to return)");
                    break;
                }
                case "6": {
                    System.out.println("Insert the \'height\' of the user");
                    while (true) {
                        input = sc.next();
                        if (!input.matches("[0-9]+")) {
                            System.out.println("Please insert a correct option");
                            continue;
                        }
                        break;
                    }
                    System.out.println("Press 0 to find shorter users or 1 to find higher or equal users");
                    String x = sc.next();
                    switch (x) {
                        case "0":
                            filters.add(mongoDb.getFilter("height", input, "lt"));
                            break;
                        case "1":
                            filters.add(mongoDb.getFilter("height", input, "gte"));
                            break;
                        default:
                            System.out.println("Please try again");
                    }
                    System.out.println("\nInsert another filter or press 0 to search\n(or press r to return)");
                    break;
                }
                case "7": {
                    System.out.println("Insert the \'weight\' of the user");
                    while (true) {
                        input = sc.next();
                        if (!input.matches("[0-9]+")) {
                            System.out.println("Please insert a correct option");
                            continue;
                        }
                        break;
                    }
                    System.out.println("Press 0 to find lighter1" +
                            " users or 1 to find heavier or equal users");
                    String x = sc.next();
                    switch (x) {
                        case "0":
                            filters.add(mongoDb.getFilter("weight", input, "lt"));
                            break;
                        case "1":
                            filters.add(mongoDb.getFilter("weight", input, "gte"));
                            break;
                        default:
                            System.out.println("Please try again");
                    }
                    System.out.println("\nInsert another filter or press 0 to search\n(or press r to return)");
                    break;
                }
                case "8":
                    String r = neo4j.showRecommended(self.getUser_id());
                    optionsUser(r);
                    return;
                case "0":
                    String ret = mongoDb.searchUsers(filters);
                    if(ret==null)
                        return;
                    else if(ret.startsWith("u:")) // the user want to see details of one user
                        neo4j.unfollowUser(self.getUser_id(), ret.substring(2));
                    else if(ret.startsWith("f:"))
                        neo4j.followUser(self.getUser_id(), ret.substring(2));
                    return;
                case "r":
                    return;
                default:
                    System.out.println("Please select an existing option!\n");
            }
        }
    }

    public void mostFollowedUsers() throws IOException {
        String input;
        Scanner sc = new Scanner(System.in);
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
        String ret = neo4j.mostFollowedUsers(Integer.parseInt(input));
        optionsUser(ret);
    }

    public void mostRatedTrainers() throws IOException {
        String input;
        Scanner sc = new Scanner(System.in);
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
        String ret = neo4j.mostRatedTrainers(Integer.parseInt(input));
        optionsUser(ret);
    }

    public void optionsUser(String option) throws IOException {
        if(option==null)
            return;
        if(option.startsWith("r:")) { //the user want to see routine details of one of followed users
            String ret = mongoDb.showRoutineDetails(option.substring(2));
            if(ret==null)
                return;
            else if(ret.startsWith("c:"))
                addComment(option.substring(2));
            else if(ret.startsWith("v:"))
                addVote(option.substring(2));
        }

        else if(option.startsWith("u:")){ // the user want to see details of one user
            String ret = mongoDb.showUserDetails(option.substring(2)); //if the return is not null the user want to follow/unfollow antoher user
            if(ret==null)
                return;
            else if(ret.startsWith("u:")) {
                neo4j.unfollowUser(self.getUser_id(), option.substring(2));
            }
            else if(ret.startsWith("f:")) {
                neo4j.followUser(self.getUser_id(), option.substring(2));
            }
        }
    }

    public boolean session() throws IOException {
        System.out.println("WELCOME " + self.getName());
        boolean running = true;
        while(running) {
            System.out.println("\nWhat do you need?\n" +
                    "1)  See your current routine\n" +
                    "2)  See your past routines\n" +
                    "3)  See your followed list\n" +
                    "4)  See your followers list\n" +
                    "5)  See routines you commented\n" +
                    "6)  Find a routine by parameter\n" +
                    "7)  Find a user by parameter\n" +
                    "8)  Find n-most rated personal trainer\n" +
                    "9)  Find n-most followed users\n" +
                    "10) Modify your profile\n" +
                    "11) Log out\n" +
                    "0)  Exit");
            Scanner sc = new Scanner(System.in);
            String input = sc.next();
            switch (input) {
                case "1":
                    showCurrentRoutine();
                    break;
                case "2":
                    showPastRoutines();
                    break;
                case "3":
                    showFollowedUsers();
                    break;
                case "4":
                    showFollowers();
                    break;
                case "5":
                    showRoutinesCommented(self.getUser_id());
                    break;
                case "6":
                    findRoutine();
                    break;
                case "7":
                    findUser();
                    break;
                case "8":
                    mostRatedTrainers();
                    break;
                case "9":
                    mostFollowedUsers();
                    break;
                case "10":
                    changeProfile();
                    break;
                case "11":
                    running = false;
                    break;
                case "0":
                    return false;
                default:
                    System.out.println("Please select an existing option!\n");
                    break;
            }
        }
        return true;
    }

    //function for signup to the app
    public boolean signUp() throws IOException {
        BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
        String name, gender, yob, height, weight, training, bg, exp, email, password, level;
        Scanner sc = new Scanner(System.in);
        System.out.println("Your are signing-up as a new user, please insert your credentials\n" +
                "If you want to return press r\n");

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
        yob = sc.next();
        if(yob.equals("r"))
            return true;
        System.out.println("Insert your height...");
        height = sc.next();
        if(height.equals("r"))
            return true;
        System.out.println("Insert your weight...");
        weight = sc.next();
        if(weight.equals("r"))
            return true;
        System.out.println("Insert your level (Beginner/Intermediate/Expert)...");
        level = sc.next();
        if(level.equals("r"))
            return true;
        level = level.replace(level.substring(0,1), level.substring(0,1).toUpperCase());
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
        int user = mongoDb.lastUser();
        self = new User(name, gender, yob, height, weight, training, bg, exp, email, password, level, "no", Integer.toString(user));

        mongoDb.signUp(self);
        neo4j.insertUser(self);
        System.out.println("\nNice, from now on your are a member of the WeFit community,\n" +
                "soon one of our trainer will contact you to assign a training level\n" +
                "and build a personal routine with you!\n" +
                "We hope your stay here will be a pleasurable one!\n");
        if (session()==false)
            return false; //exit the application
        return true;
    }

    public void showCurrentRoutine() throws IOException {
        String routine = neo4j.showRoutines(self.getUser_id(), "current");
        if(routine!=null) {
            String option = mongoDb.showRoutineDetails(routine);
            if(option==null)
                return;
            else if(option.startsWith("c:"))
                addComment(routine);
            else if(option.startsWith("v:"))
                addVote(routine);
        }
    }

    public void showFollowedUsers() throws IOException {
        String ret = neo4j.showFollowUsers(self.getUser_id(), "followed");
        optionsUser(ret);
    }

    public void showFollowers() throws IOException {
        String ret = neo4j.showFollowUsers(self.getUser_id(), "followers");
        optionsUser(ret);
    }

    public void showPastRoutines() throws IOException {
        //mongoDb.showPastRoutines(self.getString("user_id"));}
        String routine = neo4j.showRoutines(self.getUser_id(), "past");
        if(routine!=null) {
            String option = mongoDb.showRoutineDetails(routine);
            if(option==null)
                return;
            else if(option.startsWith("c:"))
                addComment(routine);
            else if(option.startsWith("v:"))
                addVote(routine);
        }
    }

}
