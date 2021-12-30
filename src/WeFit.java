import com.mongodb.ConnectionString;
import com.mongodb.client.*;
import org.bson.Document;
import org.neo4j.driver.*;

import java.util.Scanner;

import static java.lang.System.exit;
import static org.neo4j.driver.Values.parameters;

public class WeFit {
    public static void main(String[] args) {
            /*ConnectionString uri = new ConnectionString("mongodb://localhost:27017");
            MongoClient myClient = MongoClients.create(uri);
            MongoDatabase database = myClient.getDatabase("mydb");
            MongoCollection<Document> collection = database.getCollection("test");
            Document doc = Document.parse("{name:\"Gionatan\", surname:\"Gallo\"}");
            collection.insertOne(doc);*/
            /*Neo4jdb neo = new Neo4jdb("bolt://localhost:7687", "neo4j", "acul");
            neo.addPerson("stefano");*/

        System.out.println("*******************************************\n" +
                "Welcome to the WeFit app\n" +
                "*******************************************");
        System.out.println("Press 1 to SIGN-IN\nOr press 2 to SIGN-UP");
        Scanner sc = new Scanner(System.in);
        String input = sc.next();
        switch (input) {
            case "1":
                signIn();
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
        /*query neo4j database*/
        if (/*found*/true) {
            session(username, password);
        }else{
            System.out.println("Incorrect email or password, please retry!");
        }
    }

    public static void session(String username, String password){
        boolean running = true;
        while(running) {
            System.out.println("WELCOME " + username + "\n What do you need?\n" +
                    "1) See your current routine\n" +
                    "2) See your past routines\n" +
                    "3) See your followed list\n" +
                    "4) See routines you commented\n" +
                    "5) Exit the app");
            Scanner sc = new Scanner(System.in);
            String input = sc.next();
            switch (input) {
                case "1":
                    System.out.println("There is no active routine...\n");
                    break;
                case "2":
                    System.out.println("There are no pasts routines...\n");
                    break;
                case "3":
                    System.out.println("There are no people your are following...\n");
                    break;
                case "4":
                    System.out.println("You have not yet commented any routine...\n");
                    break;
                case "5":
                    System.out.println("Bye bye (￣(ｴ)￣)ﾉ");
                    running = false;
                    break;
                default:
                    System.out.println("Please select an existing option!\n");
                    break;
            }
        }
    }
}