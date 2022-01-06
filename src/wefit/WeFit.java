
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
                }
            }
        }

    }


    public static void signIn() {
        String username, password;
        System.out.println("Please insert your username...");
        Scanner sc = new Scanner(System.in);
        username = sc.next();
        System.out.println("now insert your password...");
        password = sc.next();
        System.out.println(username + " " + password);
        user = mongoDb.signIn(username, password);
        /*query neo4j database*/

        if (user != null) {
            session(username, password);
        }else{
            System.out.println("Incorrect email or password, please retry!");
        }
    }

    public static void session(String username, String password){
        System.out.println("WELCOME " + user.getString("name\n"));
        boolean running = true;
        while(running) {
            System.out.println("\n What do you need?\n" +
                    "1) See your current routine\n" +
                    "2) See your past routines\n" +
                    "3) See your followed list\n" +
                    "4) See routines you commented\n" +
                    "5) Log out\n" +
                    "6) Exit the app");
            Scanner sc = new Scanner(System.in);
            String input = sc.next();
            switch (input) {
                case "1":
                    mongoDb.showCurrentRoutine(user.getString("athlete_id"));
                    break;
                case "2":
                    mongoDb.showPastRoutine(user.getString("athlete_id"));
                    break;
                case "3":
                    System.out.println("There are no people your are following...\n");
                    break;
                case "4":
                    System.out.println("You have not yet commented any routine...\n");
                    break;
                case "5":
                    user = null;
                    return;
                case "6":
                    System.out.println("Bye bye (￣(ｴ)￣)ﾉ");
                    running = false;
                    break;
                default:
                    System.out.println("Please select an existing option!\n");
                    break;
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