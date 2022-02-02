package it.unipi.wefit.entities;


import org.bson.Document;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import lombok.*;
import org.bson.types.ObjectId;

@Getter
@Setter
public class Comment {

    private String id;
    private String comment;
    private String timestamp;
    private String user;

    public Comment(Document doc){
        fromDocument(doc);
    }

    public Comment(String comment, String user){
        this.comment = comment;
        this.timestamp = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").format(new Date());
        this.user = user;
    }

    public Document toDocument(){
        Document doc = new Document();
        if(id != null)
            doc.append("_id", new ObjectId(getId()));
        doc.append("Comment", comment);
        doc.append("Time", timestamp);
        doc.append("user", user);
        return doc;
    }

    public void fromDocument(Document doc){
        id = doc.getObjectId("_id").toString();
        comment = doc.getString("Comment");
        timestamp = doc.getString("Time");
        user = doc.getString("user");
    }
}
