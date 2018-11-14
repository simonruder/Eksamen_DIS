package com.cbsexam;

import cache.ProductCache;
import com.google.gson.Gson;
import controllers.ProductController;
import java.util.ArrayList;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import controllers.UserController;
import model.Product;
import model.User;
import utils.Encryption;
import utils.Log;

@Path("product")
public class ProductEndpoints {

  private static ProductCache productCache = new ProductCache();//SIMON - Vi laver en global instans
  private static ArrayList<User> users = UserController.getUsers();

  /**
   * @param idProduct
   * @return Responses
   */
  @GET
  @Path("/{idProduct}/{token}")
  public Response getProduct(@PathParam("idProduct") int idProduct, @PathParam("token") String token) {

    try{


    if (idProduct==ProductController.getProduct(idProduct).getId()){
      boolean checkForEncryption = true;
    // Call our controller-layer in order to get the order from the DB
    Product product = ProductController.getProduct(idProduct);

    // We convert the java object to json with GSON library imported in Maven
    String json = new Gson().toJson(product);

    for (User user : users){
      if (user.getToken()!=null && user.getToken().equals(token)){
        checkForEncryption = false;
      }
    }

    if (checkForEncryption){
      // TODO: Add Encryption to JSON : FIXED
      //SIMON - Krypering tilføjet
      json = Encryption.encryptDecryptXOR(json);
    }
    // Return a response with status 200 and JSON as type
    return Response.status(200).type(MediaType.TEXT_PLAIN_TYPE).entity(json).build();
    }//End of IF ID=id

    }catch (Exception e) {
      System.out.println(e.getMessage());
      return Response.status(400).entity("Product with "+idProduct+" does not exist").build();
    }
    return null;
  }



  /** @return Responses */
  @GET
  @Path("/{token}")
  public Response getProducts(@PathParam("token") String token) {

    try {
      boolean checkForEncryption = true;


      // Write to log that we are here
      Log.writeLog(this.getClass().getName(), this, "Getting all products", 0);

      // Call our controller-layer in order to get the order from the DB
      ArrayList<Product> products = productCache.getProducts(false);//Den skal kun opdatere, hvis den er tom


      // We convert the java object to json with GSON library imported in Maven
      String json = new Gson().toJson(products);

      for (User user:users) {
        if (user.getToken()!=null && user.getToken().equals(token)){
          checkForEncryption=false;
        }
      }//SIMON - End of forEach

      if (checkForEncryption){
        // TODO: Add Encryption to JSON : FIXED
        //Kryptering tilføjet
        json = Encryption.encryptDecryptXOR(json);
      }

      // Return a response with status 200 and JSON as type
      return Response.status(200).type(MediaType.TEXT_PLAIN_TYPE).entity(json).build();
    }catch (Exception e){
      return Response.status(400).entity("No products available").build();
    }
  }

  @POST
  @Path("/")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response createProduct(String body) {

    //Getting the instance of ProductCache
    ProductCache productCache = new ProductCache();

    // Read the json from body and transfer it to a product class
    Product newProduct = new Gson().fromJson(body, Product.class);

    // Use the controller to add the user
    Product createdProduct = ProductController.createProduct(newProduct);

    //SIMON - ForceUpdate sættes til true, hvilket betyder at kræver en opdatering af cachen
    productCache.getProducts(true);

    // Get the user back with the added ID and return it to the user
    String json = new Gson().toJson(createdProduct);

    // Return the data to the user
    if (createdProduct != null) {
      // Return a response with status 200 and JSON as type
      return Response.status(200).type(MediaType.APPLICATION_JSON_TYPE).entity(json).build();
    } else {
      return Response.status(400).entity("Could not create user").build();
    }
  }
}
