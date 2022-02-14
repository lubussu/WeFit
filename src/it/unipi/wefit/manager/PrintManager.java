package it.unipi.wefit.manager;

import it.unipi.wefit.entities.Comment;
import it.unipi.wefit.entities.Exercise;
import it.unipi.wefit.entities.User;
import it.unipi.wefit.entities.Workout;

import java.util.ArrayList;
import java.util.Scanner;

public class PrintManager {

    //function for print summary information of the given exercises
    public static void printComments(ArrayList<Comment> comms, int num){
        int cycle = 0;
        for(int i=0; i<comms.size(); i++){
            System.out.println((i+1)+")");
            Comment c = comms.get(i);
            System.out.println("User: "+ c.getUser());
            System.out.println("Time: "+ c.getTimestamp());
            System.out.println("Comment: "+ c.getComment());
            System.out.println("-------------------------------------------------------------------------------------------------------------------------------");

            cycle++;
            if(cycle == num){
                System.out.println("Insert m to see more or another key to continue...");
                Scanner sc = new Scanner(System.in);
                if(sc.next().equals("m")) cycle = 0;
                else return;
            }
        }
    }

    //function for print summary information of the given exercises
    public static void printExercises(ArrayList<Exercise> exs, int num){
        int cycle = 0;
        System.out.printf("%5s %60s %20s %15s %15s", "     ", "Name", "Muscle Targeted", "Equipment", "Type\n");
        System.out.println("---------------------------------------------------------------------------------------------------------------------");
        for(int i=0;i<exs.size();i++) {
            System.out.printf("%5s", (i+1)+") ");
            exs.get(i).print(false);
            cycle++;
            if(cycle == num){
                System.out.println("Insert m to see more or another key to continue...");
                Scanner sc = new Scanner(System.in);
                if(sc.next().equals("m")) cycle = 0;
                else return;
            }
        }
    }

    //function for print summary information of the given routines
    public static void printRoutines(ArrayList<Workout> works, int num) {
        int cycle = 0;
        System.out.printf("%5s %10s %10s %15s %15s %15s %15s", "     ", "User", "Trainer", "Level", "Starting day", "End day","Vote\n");
        System.out.println("------------------------------------------------------------------------------------------------------------");
        for(int i=0; i<works.size(); i++) {
            System.out.printf("%5s", (i+1)+") ");
            works.get(i).print();
            cycle++;
            if(cycle == num){
                System.out.println("Insert m to see more or another key to continue...");
                Scanner sc = new Scanner(System.in);
                if(sc.next().equals("m")) cycle = 0;
                else return;
            }
        }
    }

    //function for print summary information of the given users
    public static void printUsers(ArrayList<User> us, int num) {
        int cycle = 0;
        System.out.printf("%5s %10s %20s %10s %15s %15s %10s", "     ", "User_Id", "Name", "Gender", "Year of birth", "Level","Trainer\n");
        System.out.println("--------------------------------------------------------------------------------------------------------");
        for(int i=0; i<us.size(); i++) {
            System.out.printf("%5s", (i+1)+") ");
            us.get(i).print();
            cycle++;
            if(cycle == num){
                System.out.println("Insert m to see more or another key to continue...");
                Scanner sc = new Scanner(System.in);
                if(sc.next().equals("m")) cycle = 0;
                else return;
            }
        }
    }
}
