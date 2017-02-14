package com.wordpress.capehart.sbs.fullstackeval;

import java.net.URI;
import java.util.ArrayList;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.bson.Document;
import org.bson.json.JsonMode;
import org.bson.json.JsonWriterSettings;

import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Projections.*;

@Path("employees")
public class EmployeeResource {

	@GET
	@Path("abridged")
	@Produces(MediaType.APPLICATION_JSON)
	public String getEmployeesAbridged() {
		// TODO: Make this get the DB host and port from a config file
    	try(MongoClient mongoClient = new MongoClient()) {
	    	MongoDatabase db = mongoClient.getDatabase("sbs");
	    	MongoCollection<Document> employeesDBCollection = db.getCollection("employees");
	    	
	    	MongoCursor<Document> employeeCursor = employeesDBCollection.find().
	    			projection(fields(include("FirstName", "LastName", "Position", "Active"))).iterator();

	    	return buildEmployeesString(employeeCursor);
    	}
	}
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public String getEmployees() {
		// TODO: Make this get the DB host and port from a config file
    	try(MongoClient mongoClient = new MongoClient()) {
	    	MongoDatabase db = mongoClient.getDatabase("sbs");
	    	MongoCollection<Document> employeesDBCollection = db.getCollection("employees");
	    	
	    	MongoCursor<Document> employeeCursor = employeesDBCollection.find().iterator();
	    	
	    	return buildEmployeesString(employeeCursor);
    	}
	}
	
	private String buildEmployeesString(MongoCursor<Document> employeeCursor) {
		final StringBuilder empsJson = new StringBuilder("{\"employees\": [");
    	
    	if(employeeCursor.hasNext()) {
    		empsJson.append(employeeCursor.next().toJson());
    	}
    	while(employeeCursor.hasNext()) {
    		empsJson.append(", " + employeeCursor.next().toJson());
    	}
    	
    	empsJson.append("]}");
    	
    	return empsJson.toString();
	}
	
    /**
     * Method handling HTTP GET requests. The returned object will be sent
     * to the client as "application/json" media type.
     *
     * @return String that will be returned as a application/json response.
     */
    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public String getIt(@PathParam("id") String id) {
    	// TODO: Make this get the DB host and port from a config file
    	try(MongoClient mongoClient = new MongoClient()) {
	    	MongoDatabase db = mongoClient.getDatabase("sbs");
	    	MongoCollection<Document> employeesDBCollection = db.getCollection("employees");
	    	
	    	return employeesDBCollection.find(eq("First Name", id)).first().
	    			toJson(new JsonWriterSettings(JsonMode.STRICT));
    	}
    }
    
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createEmployee(String arg) {
    	// TODO: Make this get the DB host and port from a config file
    	try(MongoClient mongoClient = new MongoClient()) {
    		MongoDatabase db = mongoClient.getDatabase("sbs");
	    	MongoCollection<Document> employeesDBCollection = db.getCollection("employees");
	    	
	    	Document doc = Document.parse(arg);
	    	employeesDBCollection.insertOne(doc);
    	}
    	
		return Response.created(URI.create("/employees")).build();
    }
    
    // TODO: Validate employee JSON before pushing it to the database
}
