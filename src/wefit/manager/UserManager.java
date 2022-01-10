package wefit.manager;

import org.bson.Document;
import org.bson.conversions.Bson;
import wefit.db.MongoDbConnector;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import static com.mongodb.client.model.Aggregates.match;
import static com.mongodb.client.model.Aggregates.project;
import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Projections.exclude;
import static com.mongodb.client.model.Projections.fields;

public class UserManager {

    private Document self;
    static MongoDbConnector mongoDb;

    public UserManager(Document user, MongoDbConnector mongo){
        this.self = user;
        this.mongoDb = mongo;
    }
    public Document signUp(){
        String name, gender, yob, height, weight, training, bg, exp, email, password;
        Scanner sc = new Scanner(System.in);
        System.out.println("Your are signing-up as a new user, please insert your credentials\n" +
                "Insert your name...\n");
        name = sc.next();
        System.out.println("...and your surname");
        name = " " + sc.next();
        System.out.println("Insert your gender...\n");
        gender = sc.next();
        System.out.println("Insert your year of birth...\n");
        yob = sc.next();
        System.out.println("Insert your height...\n");
        height = sc.next();
        System.out.println("Insert your weight...\n");
        weight = sc.next();
        System.out.println("Describe your current training routine...\n");
        training = sc.next();
        System.out.println("Describe your training or sportive background...\n");
        bg = sc.next();
        System.out.println("Describe your athletic experiences...\n");
        exp = sc.next();
        System.out.println("Insert your email...\n");
        email = sc.next();
        System.out.println("Choose a password...\n");
        password = sc.next();
        self = null;
        self.append("name", name).append("gender", gender).append("year_of_birth", yob).append("height", height).append("weight", weight);
        self.append("train", training).append("background", bg).append("experience", exp).append("email", email).append("password", password);
        self.append("level", "Pending").append("trainer", "no");
        System.out.println(self.toString());
        mongoDb.signUp(self);
        //Neo4j.signUp();
        System.out.println("Nice, from now on your are a member of the WeFit community,\n" +
                "soon one of our trainer will contact you to assign a training level\n" +
                "and build a personal routine with you!\n" +
                "We hope your stay here will be a pleasurable one!\n");
        return self;

    }
    public void changeProfile(){
        System.out.println("1) Name: " + self.getString("name"));
        System.out.println("2) Gender: " + self.getString("gender"));
        System.out.println("3) Year of birth: " + self.getString("year_of_birth"));
        System.out.println("4) Height: " + self.getString("height"));
        System.out.println("5) Weight: " + self.getString("weight"));
        System.out.println("6) Training: " + self.getString("train"));
        System.out.println("7) Background: " + self.getString("background"));
        System.out.println("8) Experience: " + self.getString("experience"));
        System.out.println("9) Email: " + self.getString("email"));
        System.out.println("10) Password: " + self.getString("password"));
        System.out.println("0) Save your changes");
        System.out.println("Select an option to change...");
        Scanner sc = new Scanner(System.in);
        String input;
        while(true) {
            BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
            input = sc.next();
            switch (input) {
                case "1": {
                    System.out.println("Insert your full name...");
                    try {
                        input = bufferRead.readLine();
                    } catch (IOException e) {e.printStackTrace();}

                    //System.out.println("...and your surname");
                    //input += " " + sc.next();
                    self.append("name", input);
                    //System.out.println(self.getString("name"));
                    System.out.println("\nSelect another option or press 0 to save your changes");
                    break;
                }
                case "2": {
                    System.out.println("Insert your gender...");
                    input = sc.next();
                    self.append("gender", input);
                    System.out.println("\nSelect another option or press 0 to save your changes");
                    break;
                }
                case "3": {
                    System.out.println("Insert your year of birth...");
                    input = sc.next();
                    self.append("year_of_birth", input);
                    System.out.println("\nSelect another option or press 0 to save your changes");
                    break;
                }
                case "4": {
                    System.out.println("Insert your height...");
                    input = sc.next();
                    self.append("height", input);
                    System.out.println("\nSelect another option or press 0 to save your changes");
                    break;
                }
                case "5": {
                    System.out.println("Insert your weight...");
                    input = sc.next();
                    self.append("weight", input);
                    System.out.println("\nSelect another option or press 0 to save your changes");
                    break;
                }
                case "6": {
                    System.out.println("Insert your training...");
                    try {
                        input = bufferRead.readLine();
                    } catch (IOException e) {e.printStackTrace();}
                    self.append("train", input);
                    System.out.println("\nSelect another option or press 0 to save your changes");
                    break;
                }
                case "7": {
                    System.out.println("Insert your training background...");
                    try {
                        input = bufferRead.readLine();
                    } catch (IOException e) {e.printStackTrace();}
                    self.append("background", input);
                    System.out.println("\nSelect another option or press 0 to save your changes");
                    break;
                }
                case "8": {
                    System.out.println("Insert your experience...");
                    try {
                        input = bufferRead.readLine();
                    } catch (IOException e) {e.printStackTrace();}
                    self.append("experience", input);
                    System.out.println("\nSelect another option or press 0 to save your changes");
                    break;
                }
                case "9": {
                    System.out.println("Insert your new email...");
                    input = sc.next();
                    self.append("email", input);
                    System.out.println("\nSelect another option or press 0 to save your changes");
                    break;
                }
                case "10": {
                    System.out.println("Insert your new password...");
                    input = sc.next();
                    self.append("password", input);
                    System.out.println("\nSelect another option or press 0 to save your changes");
                    break;
                }
                case "0":
                    mongoDb.changeProfile(self);
                    return;
            }
        }
    }
    public void showCurrentRoutine(){   mongoDb.showCurrentRoutine(self.getString("user_id"));}
    public void showPastRoutines(){ mongoDb.showPastRoutines(self.getString("user_id"));}
    public void findRoutine(){
        System.out.println("\nInsert filters for find a routine..");
        System.out.println("1) User");
        System.out.println("2) Trainer");
        System.out.println("3) Level");
        System.out.println("4) Vote");
        System.out.println("5) Data");
        System.out.println("0) Search routine(s)");
        System.out.println("Select an option...");
        Scanner sc = new Scanner(System.in);
        ArrayList<Bson> matches = new ArrayList<>();
        String input;
        while(true) {
            BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
            input = sc.next();
            switch (input) {
                case "1": {
                    System.out.println("Insert the \'user_id\' or the \'name\'");
                    try {
                        input = bufferRead.readLine();
                    } catch (IOException e) { e.printStackTrace();}
                    if(input.matches("[0-9.]+"))
                        matches.add(match(eq("user", input)));
                    else
                        matches.add(match(eq("user", mongoDb.searchUser(input).getString("user_id"))));

                    System.out.println("\nInsert another filter or press 0 to search");
                    break;
                }
                case "2": {
                    System.out.println("Insert the \'user_id\' or the \'name\' of the trainer");
                    try {
                        input = bufferRead.readLine();
                    } catch (IOException e) { e.printStackTrace();}
                    if(input.matches("[0-9.]+"))
                        matches.add(match(eq("trainer", input)));
                    else
                        matches.add(match(eq("trainer", mongoDb.searchUser(input).getString("user_id"))));
                    System.out.println("Insert another filter or press 0 to search");
                    break;
                }
                case "3": {
                    System.out.println("Insert the \'level\'");
                    input = sc.next();
                    matches.add(match(eq("level",input)));
                    System.out.println("Insert another filter or press 0 to search");
                    break;
                }
                case "4": {
                    System.out.println("Insert the \'vote\'");
                    input = sc.next();
                    if(!input.matches("[0-9.]"))
                        System.out.println("Please insert a number between 1 and 5");
                    System.out.println("Press 0 to find lowest votes or 1 to find highest or equal votes");
                    String x = sc.next();
                    switch (x){
                        case "0":
                            matches.add(match(lt("vote",Integer.parseInt(input))));
                            break;
                        case"1":
                            matches.add(match(gte("vote",Integer.parseInt(input))));
                            break;
                        default:
                            System.out.println("Please try again");
                    }
                    System.out.println("Insert another filter or press 0 to search");
                    break;
                }
                case "5": {
                    System.out.println("Insert the starting_day (YYYY-MM-DD)...");
                    input = sc.next();
                    System.out.println("Press 0 to find previous dates or 1 to find equal or later dates");
                    String x = sc.next();
                    switch (x){
                        case "0":
                            matches.add(match(lt("starting_day",input)));
                            break;
                        case"1":
                            matches.add(match(gte("starting_day",input)));
                            break;
                        default:
                            System.out.println("Please try again");
                    }
                    System.out.println("Insert another filter or press 0 to search");
                    break;
                }
                case "0":
                    mongoDb.searchRoutines(matches);
                    return;
                default:
                    System.out.println("Please select an existing option!\n");
            }
        }
    }


}
