package model;

public class User {

  public int id;
  public String firstname;
  public String lastname;
  public String email;
  private String password;
  private long createdTime;
  private String token;

  public User(int id, String firstname, String lastname, String password, String email, long createdTime, String token) {
    this.id = id;
    this.firstname = firstname;
    this.lastname = lastname;
    this.password = password;
    this.email = email;
    this.createdTime = createdTime;
    this.token = token;
  }

  //Get & Set Id
  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  //Get & Set Firstname
  public String getFirstname() {
    return firstname;
  }

  public void setFirstname(String firstname) {
    this.firstname = firstname;
  }

  //Get & Set Lastname
  public String getLastname() {
    return lastname;
  }

  public void setLastname(String lastname) {
    this.lastname = lastname;
  }

  //Get & Set Email
  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  //Get & Set Password
  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  //Get & Set Created time
  public long getCreatedTime() {
    return createdTime;
  }

  public void setCreatedTime(long createdTime) {
    this.createdTime = createdTime;
  }

  //Get & Set token
  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }
}
