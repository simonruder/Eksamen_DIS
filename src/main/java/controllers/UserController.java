package controllers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;

import model.User;
import utils.Hashing;
import utils.Log;

public class UserController {

  private static Hashing hashing = new Hashing();

  private static DatabaseController dbCon;

  public UserController() {
    dbCon = new DatabaseController();
  }

  public static User getUser(int id) {

    // Check for connection
    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    // Build the query for DB
    String sql = "SELECT * FROM user where id=" + id;

    // Actually do the query
    ResultSet rs = dbCon.query(sql);
    User user = null;

    try {
      // Get first object, since we only have one
      if (rs.next()) {
        user =
            new User(
                rs.getInt("id"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getString("password"),
                rs.getString("email"),
                    rs.getLong("created_at"),
                    rs.getString("token"));


        // return the create object
        return user;
      } else {
        System.out.println("No user found");
      }
    } catch (SQLException ex) {
      System.out.println(ex.getMessage());
    }

    // Return null
    return user;
  }

  /**
   * Get all users in database
   *
   * @return
   */
  public static ArrayList<User> getUsers() {

    // Check for DB connection
    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    // Build SQL
    String sql = "SELECT * FROM user";

    // Do the query and initialyze an empty list for use if we don't get results
    ResultSet rs = dbCon.query(sql);
    ArrayList<User> users = new ArrayList<User>();

    try {
      // Loop through DB Data
      while (rs.next()) {
        User user =
            new User(
                rs.getInt("id"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getString("password"),
                rs.getString("email"),
                rs.getLong("created_at"),
                    rs.getString("token"));

        user.setToken(null);//SIMON - Fjerner token fra udskriftet
        // Add element to list
        users.add(user);
      }
    } catch (SQLException ex) {
      System.out.println(ex.getMessage());
    }

    // Return the list of users
    return users;
  }


  public static User createUser(User user) {

    // Write in log that we've reach this step
    Log.writeLog(UserController.class.getName(), user, "Actually creating a user in DB", 0);



    // Set creation time for user.
    user.setCreatedTime(System.currentTimeMillis() / 1000L);

    // Check for DB Connection
    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    hashing.setSalt(String.valueOf(user.getCreatedTime()));
    // Insert the user in the DB
    // TODO: Hash the user password before saving it. : FIXED
    //SIMON TODO:Tag stilling til hvilken Hash-funktion jeg vil bruge: FIXED - bruger Sha
    int userID = dbCon.insert(
        "INSERT INTO user(first_name, last_name, password, email, created_at) VALUES('"
            + user.getFirstname()
            + "', '"
            + user.getLastname()
            + "', '"
            + hashing.shaWithSalt(user.getPassword())//SIMON - Hashing og salter af Password
            + "', '"
            + user.getEmail()
            + "', "
            + user.getCreatedTime()
            + ")");

    if (userID != 0) {
      //Update the userid of the user before returning
      user.setId(userID);
    } else{
      // Return null if user has not been inserted into database
      return null;
    }

    // Return user
    return user;
  }
    // SIMON - Fjerner en bruger fra MySQL Databasen, og gør det med et prepared Statement
  public static void deleteUser (int id){

      // Write in log that we've reach this step
      Log.writeLog(UserController.class.getName(), id, "UserController: Deleting user with id: "+ id, 0);

      // Check for connection to DB
      if (dbCon == null) {
          dbCon = new DatabaseController();
      }

      String sql = "DELETE FROM user where id=" + id;

      dbCon.voidToDB(sql);
  } //End of delete user

  public static void updateUser (int userIdToUpdate, User userUpdatedData){

    // Write in log that we've reach this step
    Log.writeLog(UserController.class.getName(), userUpdatedData, "Actually updating a user in DB", 0);

    User currentUser = getUser(userIdToUpdate); //Finder den bruger med Id, der skal ændres og opretter et objekt af dennes oplysninger.

    if (userUpdatedData.getFirstname()==null){
      userUpdatedData.setFirstname(currentUser.getFirstname());
    }
    if (userUpdatedData.getLastname()==null){
      userUpdatedData.setLastname(currentUser.getLastname());
    }
    if (userUpdatedData.getEmail()==null){
      userUpdatedData.setEmail(currentUser.getEmail());
    }
    if (userUpdatedData.getPassword()==null){
      userUpdatedData.setPassword(currentUser.getPassword());
    }


   hashing.setSalt(String.valueOf(currentUser.getCreatedTime()));//SIMON - Setter salt ud fra CurrentUsers Created_time

// Check for DB Connection
    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

String sql = "UPDATE user SET first_name = '"+userUpdatedData.getFirstname()+ "'" +
        ", last_name= '"+userUpdatedData.getLastname()+ "'" +
        ", password= '"+hashing.shaWithSalt(userUpdatedData.getPassword())+ "'" +
        ", email= '"+userUpdatedData.getEmail()+ "'" + " where id="+userIdToUpdate;

    dbCon.voidToDB(sql);

  }

  public static String login(User userLogin) {

    ArrayList<User> allUsers = UserController.getUsers();//SIMON - Brug evt. cachen i stedet


    System.out.println("Login:"+System.currentTimeMillis()/1000L);//Giver tiden hvor der bliver logget ind, denne skal bruges til at sætte salt


    for (User user : allUsers) {
      if (user.getEmail().equals(userLogin.getEmail())){

        //SIMON - Sætter saltet
        hashing.setSalt(String.valueOf(user.getCreatedTime()));

        String password = hashing.shaWithSalt(userLogin.getPassword());

        if (password.equals(user.getPassword())){
          //SIMON - Laver nu et token ud fra username, email og med salt Created_time

          String token = user.getFirstname()+user.getLastname()+user.getEmail();
          hashing.setSalt(String.valueOf(System.currentTimeMillis()/100L));//Bruger CurrentTime, så token ikke kan genskabes igen, eller overvåges af hackere

          token = hashing.shaWithSalt(token);

          updateToken(user.id,token);//SIMON - Smider token ind i databasen, så det kan findes på brugeren når denne skal lave ændringer

          return token;

        }
      }
    }
    return null;
  }

  private static void updateToken (int id, String token){

    // Write in log that we've reach this step
    Log.writeLog(UserController.class.getName(), token, "Updating the token i DB", 0);

    // Check for DB Connection
    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    String sql = "UPDATE DisExam.user SET token = " +"'" + token +"'"+ " where id="+id;

    dbCon.voidToDB(sql);

  }
}
