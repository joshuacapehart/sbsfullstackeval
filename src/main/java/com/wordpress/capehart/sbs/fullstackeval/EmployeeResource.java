package com.wordpress.capehart.sbs.fullstackeval;

import java.net.URI;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.bson.Document;
import org.bson.json.JsonMode;
import org.bson.json.JsonWriterSettings;
import org.bson.types.ObjectId;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.UpdateResult;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Projections.*;

@Path("")
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
	
    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public String getIEmployee(@PathParam("id") String id) {
    	// TODO: Make this get the DB host and port from a config file
    	try(MongoClient mongoClient = new MongoClient()) {
	    	MongoDatabase db = mongoClient.getDatabase("sbs");
	    	MongoCollection<Document> employeesDBCollection = db.getCollection("employees");
	    	
	    	Document employee = employeesDBCollection.find(eq("_id", new ObjectId(id))).first();
	    	if(employee == null) {
	    		throw new WebApplicationException(Response.Status.NOT_FOUND);
	    	}
	    	return employee.toJson(new JsonWriterSettings(JsonMode.STRICT));
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
	    	
	    	ObjectId newEmployeeID = (ObjectId)doc.get("_id");
			return Response.created(URI.create("/employees/" + newEmployeeID.toHexString())).build();
    	}
    }
    
    @POST
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public void updateEmployee(@PathParam("id") String id, String newEmployee) {
    	// TODO: Make this get the DB host and port from a config file
    	try(MongoClient mongoClient = new MongoClient()) {
    		MongoDatabase db = mongoClient.getDatabase("sbs");
	    	MongoCollection<Document> employeesDBCollection = db.getCollection("employees");

	    	Document newEmployeeDoc = Document.parse(newEmployee);
	    	UpdateResult updateResult =
	    			employeesDBCollection.replaceOne(eq("_id", new ObjectId(id)), newEmployeeDoc);
	    	if(updateResult.getMatchedCount() < 1) {
	    		throw new WebApplicationException(Response.Status.NOT_FOUND);
	    	}
	    	if(updateResult.getModifiedCount() < 1) {
	    		throw new WebApplicationException(Response.Status.NOT_MODIFIED);
	    	}
    	}
    }
    
    @DELETE
    @Path("{id}")
    public void deleteEmployee(@PathParam("id") String id) {
    	// TODO: Make this get the DB host and port from a config file
    	try(MongoClient mongoClient = new MongoClient()) {
    		MongoDatabase db = mongoClient.getDatabase("sbs");
	    	MongoCollection<Document> employeesDBCollection = db.getCollection("employees");
	    	
	    	Document employee = employeesDBCollection.find(eq("_id", new ObjectId(id))).first();
	    	if(employee == null) {
	    		throw new WebApplicationException(Response.Status.NOT_FOUND);
	    	}
	    	
	    	employeesDBCollection.deleteOne(eq("_id", new ObjectId(id)));
    	}
    }
    
    // TODO: Validate employee JSON before pushing it to the database
}
