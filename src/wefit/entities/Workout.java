package wefit.entities;

import com.sun.source.tree.BinaryTree;
import lombok.*;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.neo4j.driver.internal.shaded.io.netty.util.internal.StringUtil;

import javax.print.Doc;
import java.util.ArrayList;

@Getter
@Setter
public class Workout {

    private String id;
    private String user;
    private String trainer;
    private String level;
    private int work_time;
    private int rest_time;
    private int repeat;
    private ArrayList<Exercise> warm_up;
    private ArrayList<Exercise> exercises;
    private ArrayList<Exercise> stretching;
    private String starting_day;
    private String end_day;
    private ArrayList<Comment> comments;
    private int num_votes;
    private double vote;

    public Workout(Document doc){
        fromDocument(doc);
    }

    public Workout(String user, String trainer, String level, int work_time, int rest_time, int repeat, ArrayList<Exercise> warm_up,
                   ArrayList<Exercise> exercises, ArrayList<Exercise> stretching, String starting_day, String end_day,
                   ArrayList<Comment> comments, int num_votes, double vote){
        this.user = user;
        this.trainer = trainer;
        this.level = level;
        this.work_time = work_time;
        this.rest_time = rest_time;
        this.repeat = repeat;
        this.warm_up = warm_up;
        this.exercises = exercises;
        this.stretching = stretching;
        this.starting_day = starting_day;
        this.end_day = end_day;
        this.comments = comments;
        this.num_votes = num_votes;
        this.vote = vote;
    }

    public Document toDocument(){
        Document doc = new Document();
        if(id != null)
            doc.append("_id", new ObjectId(id));
        doc.append("user", user);
        doc.append("trainer", trainer);
        doc.append("level", level);
        doc.append("work_time", work_time);
        doc.append("rest_time", rest_time);
        doc.append("repeat", repeat);
        ArrayList<Document> routine = new ArrayList<>();
        for(Exercise e : warm_up){
            Document fetch = new Document();
            fetch.append("name", e.getName());
            fetch.append("muscle_targeted", e.getMuscle_targeted());
            fetch.append("equipment", e.getEquipment());
            fetch.append("type", e.getType());
            if(e.getWeight() > 0){ fetch.append("weight", e.getWeight()); }
            routine.add(fetch);
        }
        doc.append("warm_up", routine);
        routine = new ArrayList<>();
        for(Exercise e : exercises) {
            Document fetch = new Document();
            fetch.append("name", e.getName());
            fetch.append("muscle_targeted", e.getMuscle_targeted());
            fetch.append("equipment", e.getEquipment());
            fetch.append("type", e.getType());
            if (e.getWeight() > 0) { fetch.append("weight", e.getWeight()); }
            routine.add(fetch);
        }
        doc.append("exercises", routine);
        routine = new ArrayList<>();
        for(Exercise e : stretching) {
            Document fetch = new Document();
            fetch.append("name", e.getName());
            fetch.append("muscle_targeted", e.getMuscle_targeted());
            fetch.append("equipment", e.getEquipment());
            fetch.append("type", e.getType());
            if (e.getWeight() > 0) { fetch.append("weight", e.getWeight()); }
            routine.add(fetch);
        }
        doc.append("stretching", routine);
        doc.append("starting_day", starting_day);
        doc.append("end_day", end_day);
        routine = new ArrayList<>();
        for(Comment c : comments){
            Document fetch = new Document();
            fetch.append("Comment", c.getComment());
            fetch.append("Time", c.getTimestamp());
            fetch.append("user", c.getUser());
            routine.add(fetch);
        }
        doc.append("comments", routine);
        doc.append("num_votes", num_votes);
        doc.append("vote", vote);
        return doc;
    }

    public void fromDocument(Document doc){
        user = doc.getString("user");
        trainer = doc.getString("trainer");
        level = doc.getString("level");
        warm_up = new ArrayList<>();
        stretching= new ArrayList<>();
        exercises = new ArrayList<>();
        comments = new ArrayList<>();

        ArrayList<Document> routine;
        //for warmup
        routine = (ArrayList<Document>) doc.get("warm_up");
        for(Document ex : routine){
            Exercise fetch = new Exercise(ex, true);
            warm_up.add(fetch);
        }
        //for exercises
        routine = (ArrayList<Document>) doc.get("exercises");
        for(Document ex : routine){
            Exercise fetch = new Exercise(ex, true);
            exercises.add(fetch);
        }
        //for stretching
        routine = (ArrayList<Document>) doc.get("stretching");
        for(Document ex : routine){
            Exercise fetch = new Exercise(ex, true);
            stretching.add(fetch);
        }
        starting_day = doc.getString("starting_day");
        end_day = doc.getString("end_day");
        //for comments
        routine = (ArrayList<Document>) doc.get("comments");
        if(routine != null) {
            for (Document c : routine) {
                Comment fetch = new Comment(c);
                comments.add(fetch);
            }
        }
        num_votes = doc.getInteger("num_votes");
        vote = doc.getDouble("vote");
    }

    public void printDetails(){
        System.out.println("------------------------------------------------------------------------------------------------------------");
        System.out.println("ROUTINE DETAILS");
        System.out.println("------------------------------------------------------------------------------------------------------------");

        System.out.print("trainer: " + trainer+"\t");
        System.out.print("level: " + level+"\n");
        System.out.print("starting_day: " + starting_day+"\t");
        System.out.print("end_day: " + end_day+"\n");
        System.out.print("work_time(sec): " + work_time+"\t");
        System.out.print("rest_time(sec): " + rest_time+"\n\n");

        System.out.print("WARM UP:\n");
        System.out.printf("%50s %20s %15s %15s", "Name", "Muscle Targeted", "Equipment", "Type\n");
        System.out.println("------------------------------------------------------------------------------------------------------------");
        for(Exercise ex : warm_up)
            ex.print();
        System.out.println();

        System.out.print("EXERCISES:\tRepeat the sequence "+repeat+" times\n");
        System.out.printf("%50s %20s %15s %15s", "Name", "Muscle Targeted", "Equipment", "Type\n");
        System.out.println("------------------------------------------------------------------------------------------------------------");
        for(Exercise ex : exercises)
            ex.print();
        System.out.println();

        System.out.print("STRETCHING:\n");
        System.out.printf("%50s %20s %15s %15s", "Name", "Muscle Targeted", "Equipment", "Type\n");
        System.out.println("------------------------------------------------------------------------------------------------------------");
        for(Exercise ex : exercises)
            ex.print();
        System.out.println();
    }

    public void print(){
        System.out.printf("%10s %15s %15s %15s", trainer,level,
                starting_day,end_day);
        System.out.println("\n");
    }

}
