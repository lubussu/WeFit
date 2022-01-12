
package wefit;
import com.mongodb.ConnectionString;
import com.mongodb.client.*;
import org.bson.Document;
import org.neo4j.driver.*;
import org.neo4j.driver.types.Node;
import org.neo4j.driver.types.Path;
import wefit.db.MongoDbConnector;
import wefit.manager.TrainerManager;
import wefit.manager.UserManager;

import java.util.Scanner;

import static java.lang.System.exit;
import static org.neo4j.driver.Values.parameters;

public class WeFit {
    static MongoDbConnector mongoDb;
    static Document user;

    public static void main(String[] args) {
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
                    UserManager uM = new UserManager(null, mongoDb);
                    if(uM.signUp()==false)
                        return;
                    break;
                }
                default:
                    System.out.println("Please select an existing option!\n");
            }
        }
    }

    public static void signIn() {
        String email, password;/*
        System.out.println("Please insert your email...");
        Scanner sc = new Scanner(System.in);
        email = sc.next();
        System.out.println("now insert your password...");
        password = sc.next();*/
        email = "Carla_Kidd1040527379@nanoff.biz";
        password = "VSVCdKNy";
        user = mongoDb.signIn(email, password);

        if (user != null && user.getString("trainer").equals("no")) {
            UserManager uM = new UserManager(user, mongoDb);
            if(uM.session()==false)
                exit(1);
        }
        else if(user.getString("trainer").equals("yes")) {
            TrainerManager tM = new TrainerManager(user, mongoDb);
            if(tM.sessionTrainer()==false)
                exit(1);
        }
        else{
            System.out.println("Incorrect email or password, please retry!");
        }
    }
/*
    public static void session(UserManager uM){
        System.out.println("WELCOME " + user.getString("name"));
        boolean running = true;
        while(running) {
            System.out.println("\nWhat do you need?\n" +
                    "1) See your current routine\n" +
                    "2) See your past routines\n" +
                    "3) See your followed list\n" +
                    "4) See routines you commented\n" +
                    "5) Find a routine by parameter\n" +
                    "6) Find a user by parameter\n" +
                    "7) Modify your profile\n" +
                    "8) Log out");
            Scanner sc = new Scanner(System.in);
            String input = sc.next();
            switch (input) {
                case "1":
                    uM.showCurrentRoutine();
                    break;
                case "2":
                    uM.showPastRoutines();
                    break;
                case "3":
                    uM.showFollowedUsers();
                    break;
                case "4":
                    System.out.println("You have not yet commented any routine...\n");
                    break;
                case "5":
                    uM.findRoutine();
                    break;
                case "6":
                    uM.findUser();
                    break;
                case "7":
                    uM.changeProfile();
                    break;
                case "8":
                    user = null;
                    return;
                default:
                    System.out.println("Please select an existing option!\n");
                    break;
            }
        }
    }
    public static void sessionTrainer(TrainerManager tM){
        System.out.println("WELCOME " + user.getString("name"));
        boolean running = true;
        while(running) {
            System.out.println("\nWhat do you need?\n" +
                    "1) See your routines\n" +
                    "2) Add a new routine\n" +
                    "3) Add a new exercise\n" +
                    "4) Log out\n" +
                    "5) Exit the app\n");
            Scanner sc = new Scanner(System.in);
            String input = sc.next();
            switch (input) {
                case "1":
                    //tM.findRoutines();
                    break;
                case "2":
                    tM.createRoutine();
                    break;
                case "3":
                    //tM.addExercise();
                    break;
                case "4":
                    System.out.println("Bye bye (￣(ｴ)￣)ﾉ");
                    user = null;
                    return;
                case "5":
                    System.out.println("Bye bye (￣(ｴ)￣)ﾉ");
                    running = false;
                    return;
                default:
                    System.out.println("Please select an existing option!\n");
                    break;
            }
        }
    }*/

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