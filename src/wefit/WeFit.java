
package wefit;
import com.mongodb.ConnectionString;
import com.mongodb.client.*;
import org.bson.Document;
import org.neo4j.driver.*;
import org.neo4j.driver.types.Node;
import org.neo4j.driver.types.Path;
import wefit.db.MongoDbConnector;

import java.util.Scanner;

import static java.lang.System.exit;
import static org.neo4j.driver.Values.parameters;

public class WeFit {
    static MongoDbConnector mongoDb;
    static Document user;

    public static void main(String[] args) {
        /*ConnectionString uri = new ConnectionString("mongodb://localhost:27017");
        MongoClient myClient = MongoClients.create(uri);
        MongoDatabase database = myClient.getDatabase("wefit");
        MongoCollection<Document> collection = database.getCollection("users");
        Document doc = Document.parse("{name:\"Gionatan\", surname:\"Gallo\"}");
        collection.insertOne(doc);*/
        //addPerson("stefano");

        System.out.println("*******************************************\n" +
                "Welcome to the WeFit app\n" +
                "*******************************************");
        while(true){
            System.out.println("Press 1 to SIGN-IN\nOr press 2 to SIGN-UP");
            Scanner sc = new Scanner(System.in);
            String input = sc.next();
            switch (input) {
                case "1": {
                    mongoDb = new MongoDbConnector("mongodb://localhost:27017","wefit");
                    signIn();
                    break;
                }
                case "2": {
                    mongoDb = new MongoDbConnector("mongodb://localhost:27017","wefit");
                    signUp();
                    break;
                }
            }
        }
    }

    public static void signUp(){
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
        user = null;
        user.append("name", name).append("gender", gender).append("year_of_birth", yob).append("height", height).append("weight", weight);
        user.append("train", training).append("background", bg).append("experience", exp).append("email", email).append("password", password);
        user.append("level", "Pending").append("trainer", "no");
        System.out.println(user.toString());
        mongoDb.signUp(user);
        System.out.println("Nice, from now on your are a member of the WeFit community,\n" +
                "soon one of our trainer will contact you to assign a training level\n" +
                "and build a personal routine with you!\n" +
                "We hope your stay here will be a pleasurable one!\n");

    }

    public static void signIn() {
        String email, password;/*
        System.out.println("Please insert your email...");
        Scanner sc = new Scanner(System.in);
        email = sc.next();
        System.out.println("now insert your password...");
        password = sc.next();
        System.out.println(email + " " + password);*/
        email = "Judith_Eyres718285046@liret.org";
        password = "3eYZcK8f";
        user = mongoDb.signIn(email, password);
        /*query neo4j database*/
        if (user != null) {
            session(email);
        }else{
            System.out.println("Incorrect email or password, please retry!");
        }
    }

    public static void session(String email){
        System.out.println("WELCOME " + user.getString("name"));
        boolean running = true;
        while(running) {
            System.out.println("\nWhat do you need?\n" +
                    "1) See your current routine\n" +
                    "2) See your past routines\n" +
                    "3) See your followed list\n" +
                    "4) See routines you commented\n" +
                    "5) Modify your profile\n" +
                    "6) Log out\n");
            Scanner sc = new Scanner(System.in);
            String input = sc.next();
            switch (input) {
                case "1":
                    mongoDb.showCurrentRoutine(user.getString("user_id"));
                    break;
                case "2":
                    mongoDb.showPastRoutines(user.getString("user_id"));
                    break;
                case "3":
                    uM.showFollowedUsers();
                    break;
                case "4":
                    System.out.println("You have not yet commented any routine...\n");
                    break;
                case "5":
                    changeProfile();
                    break;
                case "6":
                    user = null;
                    return;
                default:
                    System.out.println("Please select an existing option!\n");
                    break;
            }
        }
    }

    public  static void changeProfile(){
        System.out.println("1) Name: " + user.getString("name"));
        System.out.println("2) Gender: " + user.getString("gender"));
        System.out.println("3) Year of birth: " + user.getString("year_of_birth"));
        System.out.println("4) Height: " + user.getString("height"));
        System.out.println("5) Weight: " + user.getString("weight"));
        System.out.println("6) Training: " + user.getString("train"));
        System.out.println("7) Background: " + user.getString("background"));
        System.out.println("8) Experience: " + user.getString("experience"));
        System.out.println("9) Email: " + user.getString("email"));
        System.out.println("10) Password: " + user.getString("password"));
        System.out.println("0) Save your changes");
        System.out.println("Select an option to change...");
        Scanner sc = new Scanner(System.in);
        String input;
        while(true) {
            input = sc.next();
            switch (input) {
                case "1": {
                    System.out.println("Insert your name...");
                    input = sc.next();
                    System.out.println("...and your surname");
                    input += " " + sc.next();
                    user.append("name", input);
                    System.out.println(user.getString("name"));
                    break;
                }
                case "2": {
                    System.out.println("Insert your gender...");
                    input = sc.next();
                    user.append("gender", input);
                    break;
                }
                case "3": {
                    System.out.println("Insert your year of birth...");
                    input = sc.next();
                    user.append("year_of_birth", input);
                    break;
                }
                case "4": {
                    System.out.println("Insert your height...");
                    input = sc.next();
                    user.append("height", input);
                    break;
                }
                case "5": {
                    System.out.println("Insert your weight...");
                    input = sc.next();
                    user.append("weight", input);
                    break;
                }
                case "6": {
                    System.out.println("Insert your training...");
                    input = sc.next();
                    user.append("train", input);
                    break;
                }
                case "7": {
                    System.out.println("Insert your training background...");
                    input = sc.next();
                    user.append("background", input);
                    break;
                }
                case "8": {
                    System.out.println("Insert your experience...");
                    input = sc.next();
                    user.append("experience", input);
                    break;
                }
                case "9": {
                    System.out.println("Insert your new email...");
                    input = sc.next();
                    user.append("email", input);
                    break;
                }
                case "10": {
                    System.out.println("Insert your new password...");
                    input = sc.next();
                    user.append("password", input);
                    break;
                }
                case "0":
                    mongoDb.changeProfile(user);
                    return;
            }
        }
    }
    public static void addPerson(String name)
    {
        Driver driver = GraphDatabase.driver( "bolt://localhost:7687", AuthTokens.basic( "neo4j", "wefit" ) );

        try ( Session session = driver.session() )
        {
            session.run("CREATE (a:Person {name: $name})", parameters("name", name));
        };
    }
}

//Judith_Eyres718285046@liret.org
//3eYZcK8f


//Adela_Ogilvy1922996892@eirey.tech
//n0E3vqza