package it.unipi.wefit.entities;

import lombok.*;
import org.bson.Document;

@Getter
@Setter
public class User {

    private String name;
    private String gender;
    private String year_of_birth;
    private String height;
    private String weight;
    private String train;
    private String background;
    private String experience;
    private String email;
    private String password;
    private String level;
    private String trainer;
    private String user_id;

    public User(Document doc){
        fromDocument(doc);
    }

    public User(String name, String gender, String year_of_birth, String height, String weight, String train, String background,
                String experience, String email, String password, String level, String trainer, String user_id){
        this.name = name;
        this.gender = gender;
        this.year_of_birth = year_of_birth;
        this.height = height;
        this.weight = weight;
        this.train = train;
        this.background = background;
        this.experience = experience;
        this.email = email;
        this.password = password;
        this.level = level;
        this.trainer = trainer;
        this.user_id = user_id;
    }

    public Document toDocument(){
        Document doc = new Document();
        doc.append("name", name);
        doc.append("gender", gender);
        doc.append("year_of_birth", year_of_birth);
        doc.append("height", height);
        doc.append("weight", weight);
        doc.append("train", train);
        doc.append("background", background);
        doc.append("experience", experience);
        doc.append("email", email);
        doc.append("password", password);
        doc.append("level", level);
        doc.append("trainer", trainer);
        doc.append("user_id", user_id);
        return doc;
    }

    public void fromDocument(Document doc){
        name = doc.getString("name");
        gender = doc.getString("gender");
        year_of_birth = doc.getString("year_of_birth");
        height = doc.getString("height");
        weight = doc.getString("weight");
        train = doc.getString("train");
        background = doc.getString("background");
        experience = doc.getString("experience");
        email = doc.getString("email");
        password = doc.getString("password");
        level = doc.getString("level");
        trainer = doc.getString("trainer");
        user_id = doc.getString("user_id");
    }

    public void printDetails(){
        System.out.println("USER DETAILS:\n");

        System.out.printf("%10s %20s %10s %15s %15s %10s %10s %10s", "User_Id", "Name", "Gender", "Year of birth", "Level","Trainer", "Height", "Weight\n");
        System.out.println("-------------------------------------------------------------------------------------------------------------");

        System.out.printf("%10s %20s %10s %15s %15s %10s %10s %10s", user_id,name,gender,
                year_of_birth, level, trainer, height,weight);
        System.out.println("\n");

        if(train != null)      System.out.println("Train:\n" + train+"\n");
        if(background!= null)  System.out.println("Background:\n" + background+"\n");
        if(experience!= null)  System.out.println("Experience:\n" + background+"\n");
    }

    public void print(){
        System.out.printf("%10s %20s %10s %15s %15s %10s", user_id,name,gender,
                year_of_birth,level,trainer);
        System.out.println("\n");
    }
}
