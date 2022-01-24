package it.unipi.wefit.entities;

import org.bson.Document;
import lombok.*;

import java.util.ArrayList;

@Getter
@Setter
public class Exercise {

    private String name;
    private String type;
    private String muscle_targeted;
    private String equipment;
    private String level;
    private String image1;
    private String image2;
    private String details;
    private int weight;
    private boolean routine;

    public Exercise(Document doc, boolean routine){
        fromDocument(doc, routine);
    }

    public Exercise(String name, String type, String muscle_targeted, String equipment, String level, String image1, String image2, String details){
        this.name = name;
        this.type = type;
        this.muscle_targeted = muscle_targeted;
        this.equipment = equipment;
        this.level = level;
        this.image1 = image1;
        this.image2 = image2;
        this.details = details;
        weight = 0;
        routine = false;
    }

    public Exercise(String name, String type, String muscle_targeted, String equipment, int weight){
        this.name = name;
        this.type = type;
        this.muscle_targeted = muscle_targeted;
        this.equipment = equipment;
        this.level = null;
        this.image1 = null;
        this.image2 = null;
        this.details = null;
        this.weight = weight;
        routine = true;
    }

    public Document toDocument(){
        Document doc = new Document();
        doc.append("name", name);
        doc.append("type", type);
        doc.append("muscle_targeted", muscle_targeted);
        doc.append("equipment", equipment);
        if(routine){ if(weight > 0){ doc.append("weight", weight); } return doc;}
        doc.append("level", level);
        ArrayList<Document> images = new ArrayList<>();
        images.add(new Document("image",image1));
        images.add(new Document("image",image2));
        doc.append("images", images);
        if(details != null)
            doc.append("details", details);
        return doc;
    }

    public void fromDocument(Document doc, boolean routine){
        name = doc.getString("name");
        type = doc.getString("type");
        muscle_targeted = doc.getString("muscle_targeted");
        equipment = doc.getString("equipment");
        this.routine = routine;
        if(routine){ if(doc.getInteger("weight") != null){ weight = doc.getInteger("weight"); } return;}
        level = doc.getString("level");
        ArrayList<Document> images = (ArrayList<Document>) doc.get("images");
        image1 = images.get(0).getString("image");
        image2 = images.get(1).getString("image");
        details = doc.getString("details");
    }

    public void printDetails(){
        System.out.println("--------------------------------------------------------------------------------------------------------");
        System.out.print("EXERCISE:\t"+name+"\n");
        System.out.println("--------------------------------------------------------------------------------------------------------");

        System.out.print("Type: " + type+"\t");
        System.out.print("Level: " + level+"\n");
        System.out.print("Muscle targeted: " + muscle_targeted+"\t");
        System.out.print("Equipment: " + equipment+"\n");
        System.out.print("Images:\n");
        System.out.print(image1+"\n");
        System.out.print(image2+"\n");

        if(details!=null)
            System.out.println("Details:\n" + details+"\n");
    }

    public void print(){
        System.out.printf("%50s %20s %15s %15s", name,muscle_targeted,
                equipment,type);
        System.out.println();
    }
}
