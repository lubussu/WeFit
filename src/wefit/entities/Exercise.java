package wefit.entities;

import org.bson.Document;
import lombok.*;

@Getter
@Setter
public class Exercise {

    private String name;
    private String type;
    private String muscle_targeted;
    private String equipment;
    private String level;
    private String images;
    private String details;
    private int weight;
    private boolean routine;

    public Exercise(Document doc, boolean routine){
        fromDocument(doc, routine);
    }

    public Exercise(String name, String type, String muscle_targeted, String equipment, String level, String images, String details){
        this.name = name;
        this.type = type;
        this.muscle_targeted = muscle_targeted;
        this.equipment = equipment;
        this.level = level;
        this.images = images;
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
        this.images = null;
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
        doc.append("images", images);
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
        images = doc.getString("images");
        details = doc.getString("details");
    }
}
