package com.cbsexam;

import cache.UserCache;
import com.google.gson.Gson;
import com.sun.org.apache.xpath.internal.operations.Bool;
import controllers.UserController;

import java.security.PublicKey;
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
import utils.Hashing;
import utils.Log;

@Path("user")
public class UserEndpoints {

    //SIMON - Laver en global instance af UserCache
    private static UserCache userCache = new UserCache();

    private static Hashing hashing = new Hashing();

  /**
   * @param idUser
   * @return Responses
   */
  @GET
  @Path("/{idUser}/{token}")
  public Response getUser(@PathParam("idUser") int idUser, @PathParam("token") String token) {

      try {
          if (idUser==UserController.getUser(idUser).id) {
              // Use the ID to get the user from the controller.
              User user = UserController.getUser(idUser);

              // TODO: Add Encryption to JSON : FIXED
              // Convert the user object to json in order to return the object
              String json = new Gson().toJson(user);

              //Kryptering tilføjet
              json = Encryption.encryptDecryptXOR(json);

              if (token.equals(UserController.getUser(idUser).getToken())){
                  //SIMON - Dekrypterer indholdet, hvis man er logget ind med det rigtige token
                  json=Encryption.encryptDecryptXOR(json);
              }

              // Return the user with the status code 200
              // TODO: What should happen if something breaks down? : FIXED
              //Hvis databasen går ned, så får man ikke en Internal Server fejl, men blot en fejlmeddelelse, hvorpå man kan arbejde videre
              return Response.status(200).type(MediaType.APPLICATION_JSON_TYPE).entity(json).build();
          }
      }
      catch (Exception e){
          return Response.status(400).entity("The user does not exist").build();
      }
      return null;
  }



  /** @return Responses */
  @GET
  @Path("/{token}")
  public Response getUsers(@PathParam("token") String token) {

    // Write to log that we are here
    Log.writeLog(this.getClass().getName(), this, "Getting all users", 0);

    // Get a list of users from cache
    ArrayList<User> users = userCache.getUsers(false);

    // TODO: Add Encryption to JSON : FIXED

      //SIMON - Krypterer indholdet hvis token ikke findes.
      Boolean check = true;

    for (User user :  users){
        if (user.getToken()!= null && user.getToken().equals(token)){
            //SIMON - Sætter check til false, så json-strengen ikke bliver krypteret, hvis token findes.
            check = false;


        }
        //SIMON - Sætter token til nul, så de ikke bliver udskrevet
        user.setToken(null);
    }

    // Transfer users to json in order to return it to the user
    String json = new Gson().toJson(users);
    if (check){
        //SIMON - Tilføjer kryptering
        json = Encryption.encryptDecryptXOR(json);
    }

    // Return the users with the status code 200
    return Response.status(200).type(MediaType.APPLICATION_JSON).entity(json).build();
  }

  @POST
  @Path("/create")
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
  public Response loginUser(String login) {

      User userLogin = new Gson().fromJson(login, User.class);

      String token = UserController.login(userLogin);


if (token!=null){
    return Response.status(200).entity("Hello, here is your session-token:\n"+token +"\n Remember it when entering other endpoints").build();
}else{
    // Return a response with status 200 and JSON as type
    return Response.status(400).entity("User with e-mail: "+ userLogin.getEmail()+ " doesn't exist").build();}
  }

  // TODO: Make the system able to delete users : FIXED
  @POST
  @Path("/delete/{delete}/{token}")
  public Response deleteUser(@PathParam("delete") int idToDelete,@PathParam("token") String token) {

      try {
          if (token.equals(UserController.getUser(idToDelete).getToken())) {

              //SIMON - Kalder deleteUser-metoden i UserControlleren, hvor input er det id, der bliver skrevet i URL'en
              UserController.deleteUser(idToDelete);

              //SIMON - Skriver i loggen, hvilken bruger, der bliver slettet
              Log.writeLog(UserController.class.getName(), idToDelete, "Sletter nu: " + idToDelete, 0);

              if (idToDelete != 0) {
                  return Response.status(200).entity("Your account with id: " + idToDelete + " has now been deleted. \n Thanks for using our service.").build();
              } else {
                  return Response.status(400).entity("Failed to delete user").build();
              }
          } else {
              System.out.println("Der er noget galt");
              return Response.status(400).entity("The token doesn't match our service").build();
          }
      }catch (Exception e){
          System.out.println(e.getMessage());
          return Response.status(400).entity("Your are not allowed to delete other users").build();
      }
  }

  // TODO: Make the system able to update users : FIXED
  @POST
  @Path("update/{update}/{token}")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response updateUser(@PathParam("update") int userIdToUpdate, String UserUpdatedData, @PathParam("token") String token) {

     try {


         // Read the json from body and transfer it to a user class
         User updateUser = new Gson().fromJson(UserUpdatedData, User.class);

         if (token.equals(UserController.getUser(userIdToUpdate).getToken())) {

             if (userIdToUpdate != 0) {
                 UserController.updateUser(userIdToUpdate, updateUser);
             }
         }

         // Get the user back with the added ID and return it to the user
         String json = new Gson().toJson(updateUser);

         if (updateUser != null) {
             //SIMON - Opdaterer cachen når en bruger har opdateret sine oplysninger
             userCache.getUsers(true);
             return Response.status(200).type(MediaType.APPLICATION_JSON_TYPE).entity("You have chosen to update user with id " + userIdToUpdate ).build();
         } else {
             return Response.status(400).entity("Could not update user").build();
         }

     }catch (Exception e){
         System.out.println(e.getMessage());
         return Response.status(400).entity("The user doesn't exist").build();
     }
     }
}
