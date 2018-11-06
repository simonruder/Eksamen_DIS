package com.cbsexam;

import cache.UserCache;
import com.google.gson.Gson;
import controllers.UserController;
import java.util.ArrayList;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import model.User;
import utils.Encryption;
import utils.Log;

@Path("user")
public class UserEndpoints {

  /**
   * @param idUser
   * @return Responses
   */
  @GET
  @Path("/{idUser}")
  public Response getUser(@PathParam("idUser") int idUser) {

    // Use the ID to get the user from the controller.
    User user = UserController.getUser(idUser);

    // TODO: Add Encryption to JSON : FIXED
    // Convert the user object to json in order to return the object
    String json = new Gson().toJson(user);

    //Kryptering tilføjet
    json = Encryption.encryptDecryptXOR(json);

    // Return the user with the status code 200
    // TODO: What should happen if something breaks down?
    return Response.status(200).type(MediaType.APPLICATION_JSON_TYPE).entity(json).build();
  }

  //SIMON - Laver en global instance af UserCache
  public static UserCache userCache = new UserCache();

  /** @return Responses */
  @GET
  @Path("/")
  public Response getUsers() {

    // Write to log that we are here
    Log.writeLog(this.getClass().getName(), this, "Getting all users", 0);

    // Get a list of users from cache
    ArrayList<User> users = userCache.getUsers(false);

    // TODO: Add Encryption to JSON : FIXED
    // Transfer users to json in order to return it to the user
    String json = new Gson().toJson(users);

    //Kryptering tilføjet
    json=Encryption.encryptDecryptXOR(json);

    // Return the users with the status code 200
    return Response.status(200).type(MediaType.APPLICATION_JSON).entity(json).build();
  }

  @POST
  @Path("/")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response createUser(String body) {

    // Read the json from body and transfer it to a user class
    User newUser = new Gson().fromJson(body, User.class);

    // Use the controller to add the user
    User createUser = UserController.createUser(newUser);

    //SIMON - Opdaterer cachen når der er blevet oprettet en bruger
    userCache.getUsers(true);


    // Get the user back with the added ID and return it to the user
    String json = new Gson().toJson(createUser);

    // Return the data to the user
    if (createUser != null) {
      // Return a response with status 200 and JSON as type
      return Response.status(200).type(MediaType.APPLICATION_JSON_TYPE).entity(json).build();
    } else {
      return Response.status(400).entity("Could not create user").build();
    }
  }

  // TODO: Make the system able to login users and assign them a token to use throughout the system.
  @POST
  @Path("/login")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response loginUser(String x) {

    // Return a response with status 200 and JSON as type
    return Response.status(400).entity("Endpoint not implemented yet").build();
  }

  // TODO: Make the system able to delete users : FIXED
  @POST
  @Path("/delete/{delete}")
  public Response deleteUser(@PathParam("delete") int idToDelete) {

    //SIMON - Kalder deleteUser-metoden i UserControlleren, hvor input er det id, der bliver skrevet i URL'en
    UserController.deleteUser(idToDelete);

    //SIMON - Skriver i loggen, hvilken bruger, der bliver slettet
    Log.writeLog(UserController.class.getName(), idToDelete, "Sletter nu: "+ idToDelete, 0);

    if (idToDelete!=0) {
      return Response.status(200).entity("User with id: " + idToDelete + " has been deleted").build();
    }else {
    return Response.status(400).entity("Failed to delete user").build();}
  }

  // TODO: Make the system able to update users : FIXED
  @POST
  @Path("update/{update}")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response updateUser(@PathParam("update") int userIdToUpdate, String UserUpdatedData) {

    // Read the json from body and transfer it to a user class
    User updateUser = new Gson().fromJson(UserUpdatedData, User.class);


    if (userIdToUpdate != 0) {
        UserController.updateUser(userIdToUpdate, updateUser);
    }

    // Use the controller to add the user
    // User updatedUser = UserController.updateUser(updateUser);

    // Get the user back with the added ID and return it to the user
    String json = new Gson().toJson(updateUser);

    if (updateUser!=null){
        //SIMON - Opdaterer cachen når en bruger har opdateret sine oplysninger
        userCache.getUsers(true);
      return Response.status(200).type(MediaType.APPLICATION_JSON_TYPE).entity("You have chosen to update user with id " + userIdToUpdate +
              " ").build();
    }else {
      return Response.status(400).entity("Could not update user").build();
    }

  }
}
