package it.unipi.wefit;

import it.unipi.wefit.manager.*;

import java.io.IOException;
import java.util.Scanner;

import static java.lang.System.exit;

public class WeFit {

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
                    UserManager uM = new UserManager(null);
                    String ret = uM.signIn();
                    if (ret != null && ret.equals("yes")){
                        TrainerManager tM = new TrainerManager(uM.getSelf());
                        try {
                            if(!tM.session()) //if session return false the user want to exit
                                exit(1);
                        } catch (IOException e) {   e.printStackTrace(); }
                    }
                    else if(ret != null && ret.equals("no")) {
                        try {
                            if(!uM.session()) //if session return false the user want to exit
                                exit(1);
                        } catch (IOException e) {   e.printStackTrace(); }
                    }
                    break;
                }
                case "2": {
                    UserManager uM = new UserManager(null);
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
}