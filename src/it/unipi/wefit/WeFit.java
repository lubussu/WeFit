package it.unipi.wefit;

import org.apache.commons.codec.digest.DigestUtils;
import it.unipi.wefit.db.*;
import it.unipi.wefit.entities.*;
import it.unipi.wefit.manager.*;

import java.io.IOException;
import java.util.Scanner;

import static java.lang.System.exit;

public class WeFit {
    static MongoDbConnector mongoDb;
    static User user;

    public static void main(String[] args) {
        System.out.println("""
                *******************************************
                Welcome to the WeFit app
                *******************************************""");
        while(true){
            System.out.println("Press 1 to SIGN-IN\nOr press 2 to SIGN-UP");
            Scanner sc = new Scanner(System.in);
            String input = sc.next();
            switch (input) {
                case "1": {
                    mongoDb = new MongoDbConnector("mongodb+srv://wefit:WEFIT2022@wefit2022.kxfyu.mongodb.net/wefit?authSource=admin&replicaSet=atlas-vt45gi-shard-0&w=1&readPreference=primaryPreferred&appname=MongoDB%20Compass&ssl=true",
                            "wefit");
                    logIn();
                    break;
                }
                case "2": {
                    mongoDb = new MongoDbConnector("mongodb+srv://wefit:WEFIT2022@wefit2022.kxfyu.mongodb.net/wefit?authSource=admin&replicaSet=atlas-vt45gi-shard-0&w=1&readPreference=primaryPreferred&appname=MongoDB%20Compass&ssl=true",
                            "wefit");
                    UserManager uM = new UserManager(null, mongoDb);
                    try {
                        if (!uM.signUp())
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

    public static void logIn() {
        String email, password;
        System.out.println("Please insert your email...");
        Scanner sc = new Scanner(System.in);
        email = sc.next();
        System.out.println("now insert your password...");
        password = new DigestUtils("SHA3-256").digestAsHex(sc.next());

        try {
            user = new User(mongoDb.signIn(email, password));
        }catch(NullPointerException npe){
            System.out.println("Incorrect email or password, please retry!");
            System.out.println();
            return;
        }

        if (user != null && user.getTrainer().equals("no")) {
            UserManager uM = new UserManager(user, mongoDb);
            try {
                if(!uM.session()) //if session return false the use want to exit
                    exit(1);
            } catch (IOException e) {e.printStackTrace();}
        }
        else if(user != null && user.getTrainer().equals("yes")) {
            TrainerManager tM = new TrainerManager(user, mongoDb);
            try {
                if(!tM.sessionTrainer()) //if session return false the use want to exit
                    exit(1);
            } catch (IOException e) {e.printStackTrace();}
        }
    }

}