package wefit.manager;

import org.bson.Document;
import org.bson.conversions.Bson;
import wefit.db.MongoDbConnector;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

import static com.mongodb.client.model.Aggregates.match;
import static com.mongodb.client.model.Aggregates.project;
import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Projections.exclude;
import static com.mongodb.client.model.Projections.fields;

public class UserManager {

    private Document self;
    static MongoDbConnector mongoDb;

    public UserManager(Document user, MongoDbConnector mongo){
        this.self = user;
        this.mongoDb = mongo;
    }

    public void findRoutine(){

    }


}
