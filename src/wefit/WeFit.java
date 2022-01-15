
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

import java.io.IOException;
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
                    try {
                        if (uM.signUp() == false)
                            return;
                        break;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                default:
                    System.out.println("Please select an existing option!\n");
            }
        }
    }

    public static void signIn() {
        String email, password;
        System.out.println("Please insert your email...");
        Scanner sc = new Scanner(System.in);
        email = sc.next();
        System.out.println("now insert your password...");
        password = sc.next();
        user = mongoDb.signIn(email, password);

        if (user != null && user.getString("trainer").equals("no")) {
            UserManager uM = new UserManager(user, mongoDb);
            if(uM.session()==false) //if session return false the use want to exit
                exit(1);
        }
        else if(user != null && user.getString("trainer").equals("yes")) {
            TrainerManager tM = new TrainerManager(user, mongoDb);
            if(tM.sessionTrainer()==false) //if session return false the use want to exit
                exit(1);
        }
        else{
            System.out.println("Incorrect email or password, please retry!");
        }
    }
}