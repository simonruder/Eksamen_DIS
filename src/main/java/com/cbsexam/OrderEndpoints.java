package com.cbsexam;

import cache.OrderCache;
import com.google.gson.Gson;
import controllers.OrderController;
import java.util.ArrayList;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import controllers.UserController;
import model.Order;
import model.User;
import utils.Encryption;
import utils.Log;

@Path("order")
public class OrderEndpoints {

  private static ArrayList<User> users = UserController.getUsers();
  //Laver en global instance af orderCachen
  private static OrderCache orderCache = new OrderCache();
  /**
   * @param idOrder
   * @return Responses
   */
  @GET
  @Path("/{idOrder}/{token}")
  public Response getOrder(@PathParam("idOrder") int idOrder, @PathParam("token") String token) {

    try {

      //SIMON - vi behøver ikke tjekke for ID, da vi catcher fejlen

      // Call our controller-layer in order to get the order from the DB
      Order order = OrderController.getOrder(idOrder);

        boolean checkForEncryption = true;


        // We convert the java object to json with GSON library imported in Maven
        String json = new Gson().toJson(order);

        for (User user : users){
          if (user.getToken()!= null && user.getToken().equals(token)){
            checkForEncryption = false;
          }
        }

        if (checkForEncryption){
          // TODO: Add Encryption to JSON : FIXED
          //SIMON - Tilføjer kryptering til getOrder
          json = Encryption.encryptDecryptXOR(json);
        }

        // Return a response with status 200 and JSON as type
        return Response.status(200).type(MediaType.APPLICATION_JSON).entity(json).build();

    }catch (Exception e){
      System.out.println(e.getMessage());
      return Response.status(400).entity("The order with: "+idOrder+" does not exist").build();
    }

  }


  /** @return Responses */
  @GET
  @Path("/{token}")
  public Response getOrders(@PathParam("token") String token) {

    try {

      boolean checkForEncryption = true;

      // Write to log that we are here
      Log.writeLog(this.getClass().getName(), this, "Getting all orders", 0);

      // SIMON Call our cache-layer in order to get the orders from the DB
      //SIMON - Implementerer cache
      ArrayList<Order> orders = orderCache.getOrders(false); //SIMON - Den skal kun opdatere hvis den er tom


      // We convert the java object to json with GSON library imported in Maven
      String json = new Gson().toJson(orders);

      for (User user : users){
        if (user.getToken()!= null && user.getToken().equals(token)){
          checkForEncryption = false;
        }
      }

      if (checkForEncryption){
        // TODO: Add Encryption to JSON : FIXED
        //SIMON - Kryptering er tilføjet
        json = Encryption.encryptDecryptXOR(json);
      }

      // Return a response with status 200 and JSON as type
      return Response.status(200).type(MediaType.TEXT_PLAIN_TYPE).entity(json).build();
    }catch (Exception e){
      System.out.println(e.getMessage());
      return Response.status(400).entity("No orders available").build();
    }
  }

  @POST
  @Path("/")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response createOrder(String body) {

    // Read the json from body and transfer it to a order class
    Order newOrder = new Gson().fromJson(body, Order.class);

    // Use the controller to add the user
    Order createdOrder = OrderController.createOrder(newOrder);

    // Get the user back with the added ID and return it to the user
    String json = new Gson().toJson(createdOrder);

    // Return the data to the user
    if (createdOrder != null) {
      // Return a response with status 200 and JSON as type
      return Response.status(200).type(MediaType.APPLICATION_JSON_TYPE).entity(json).build();
    } else {

      // Return a response with status 400 and a message in text
      return Response.status(400).entity("Could not create user").build();
    }
  }
}