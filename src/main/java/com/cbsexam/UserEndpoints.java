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
   * @param token
   * @return Responses
   */
  @GET
  @Path("/getuser/{token}")
  public Response getUser(@PathParam("token") String token) {



      try {
          ArrayList<User> users = userCache.getUsers(false);

          String json;

          for (User user : users){
              if (user.getToken()!=null && user.getToken().equals(token)){
                  json = new Gson().toJson(user);

                  return Response.status(200).type(MediaType.APPLICATION_JSON_TYPE).entity(json).build();

              }

          }

          json = new Gson().toJson(users);

          // TODO: Add Encryption to JSON : FIXED
          json = Encryption.encryptDecryptXOR(json);

          return Response.status(400).type(MediaType.APPLICATION_JSON_TYPE).entity(json).build();

              // Return the user with the status code 200
              // TODO: What should happen if something breaks down? : FIXED
              //SIMON - Hvis databasen går ned, så får man ikke en Internal Server fejl, men blot en fejlmeddelelse, hvorpå man kan arbejde videre


      }
      catch (Exception e){
          return Response.status(400).entity("No users available").build();
      }
  }



  /** @return Responses */
  @GET
  @Path("/{token}")
  public Response getUsers(@PathParam("token") String token) {

    // Write to log that we are here
    Log.writeLog(this.getClass().getName(), this, "Getting all users", 0);

    // Get a list of users from cache
    ArrayList<User> users = userCache.getUsers(false);
    ArrayList<User>usersWithSelectedColumns = new ArrayList<>();

    // TODO: Add Encryption to JSON : FIXED

      //SIMON - Krypterer indholdet hvis token ikke findes.
      Boolean check = true;

    for (User user :  users){
        if (user.getToken()!= null && user.getToken().equals(token)){
            //SIMON - Sætter check til false, så json-strengen ikke bliver krypteret, hvis token findes.
            check = false;

        }//SIMON - End of if-statement

        //SIMON - Tilføjer brugere til den nye arrayliste med selecteret indhold
        User userToArray = new User(user.getId(),user.getFirstname(),user.getLastname(),null,user.getEmail(),user.getCreatedTime(),null);
        usersWithSelectedColumns.add(userToArray);

    }

    // Transfer the selceted users to json in order to return it to the user
    String json = new Gson().toJson(usersWithSelectedColumns);


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

      try {
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
      }catch (Exception e){
          System.out.println(e.getMessage());
          return Response.status(400).entity("E-mail address is already in use").build();
      }
  }

  // TODO: Make the system able to login users and assign them a token to use throughout the system : FIXED.
  @POST
  @Path("/login")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response loginUser(String login) {

      User userLogin = new Gson().fromJson(login, User.class);

      String token = UserController.login(userLogin);

      userCache.getUsers(true); //SIMON - Opdaterer Cachen, så vi får token med, da vi verificerer ud fra Cahcen


if (token!=null){
    return Response.status(200).entity("Hello, here is your session-token:\n"+token +"\n Remember it, when entering other endpoints").build();
}else{
    // Return a response with status 200 and JSON as type
    return Response.status(400).entity("User with e-mail: "+ userLogin.getEmail()+ " doesn't exist").build();}
  }

  // TODO: Make the system able to delete users : FIXED
  @POST
  @Path("/delete/{token}")
  public Response deleteUser(@PathParam("token") String token) {


          ArrayList<User> users = userCache.getUsers(false);

          for (User user : users){
              if (user.getToken()!=null && user.getToken().equals(token)){
                  //SIMON - Kalder deleteUser-metoden i UserControlleren, hvor input er det id, der bliver skrevet i URL'en
                  UserController.deleteUser(user.getId());
                  //SIMON - Skriver i loggen, hvilken bruger, der bliver slettet
                  Log.writeLog(UserController.class.getName(), user.getId(), "Sletter nu: " + user.getId(), 0);

                  //SIMON - Updating the cache
                  userCache.getUsers(true);
                  return Response.status(200).entity("Your account with id: " + user.getId() + " has now been deleted. \n Thanks for using our service.").build();
              }
          }
          return Response.status(400).entity("Your token is not valid").build();

  }

  // TODO: Make the system able to update users : FIXED
  @POST
  @Path("update/{token}")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response updateUser(String UserUpdatedData, @PathParam("token") String token) {

      ArrayList<User> users = userCache.getUsers(false);
      // Read the json from body and transfer it to a user class
      User updateUser = new Gson().fromJson(UserUpdatedData, User.class);

        for (User user : users){
            if (user.getToken()!=null && user.getToken().equals(token)){

            UserController.updateUser(user, updateUser);

            //SIMON - Opdaterer cachen når en bruger har opdateret sine oplysninger
            userCache.getUsers(true);
            return Response.status(200).type(MediaType.APPLICATION_JSON_TYPE).entity("You have chosen to update user with id " + user.getId() ).build();

            }
        }

         return Response.status(400).entity("Invalid token").build();
  }


  //SIMON - Tillader brugeren at logge ud, og dermed nulstille sit token
    @POST
    @Path("/logout/{token}")
    public Response logout (@PathParam("token") String token){

      ArrayList<User> users = userCache.getUsers(false);

      for (User user : users){
          if (user.getToken()!= null && user.getToken().equals(token)){
              UserController.logout(user);

              userCache.getUsers(true);

              return Response.status(200).entity("You are now logget out. See Ya").build();
          }
      }

        return Response.status(400).entity("Invalid token").build();

    }







}


