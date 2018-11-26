package com.cbsexam;
import com.google.gson.Gson;
import controllers.ReviewController;
import java.util.ArrayList;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import controllers.UserController;
import model.Review;
import model.User;
import utils.Encryption;

@Path("search")
public class ReviewEndpoints {

  /**
   * @param reviewTitle
   * @return Responses
   */
  @GET
  @Path("/title/{title}/{token}")
  public Response search(@PathParam("title") String reviewTitle, @PathParam("token") String token) {

    try{

      boolean checkForEncryption = true;
    //SIMON - Kalder UserControlleren til at lave en liste over alle brugere fra DB
    ArrayList<User> users = UserController.getUsers();
      // Call our controller-layer in order to get the order from the DB
      ArrayList<Review> reviews = ReviewController.searchByTitle(reviewTitle);


      // We convert the java object to json with GSON library imported in Maven
      String json = new Gson().toJson(reviews);

      //SIMON - Itererer gennem listen af brugere for at finde det token i DB, der matcher det indtastede
      for (User user : users) {
        if (user.getToken()!= null && user.getToken().equals(token)){
            checkForEncryption = false;
        }
    }

    if (checkForEncryption) {
      // TODO: Add Encryption to JSON : FIXED
      //SIMON - Kryptering tilføjet
      json = Encryption.encryptDecryptXOR(json);
    }
    
    return Response.status(200).type(MediaType.APPLICATION_JSON).entity(json).build();
    }catch (Exception e){
      System.out.println(e.getMessage());
      return Response.status(400).type("Your token doesn't match our service - please try again").build();
    }
  }


}
