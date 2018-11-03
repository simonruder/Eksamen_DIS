package cache;

import java.util.ArrayList;
import controllers.UserController;
import model.User;
import utils.Config;

//TODO: Build this cache and use it.:FIXED
public class UserCache {

    //SIMON - Laver en arrayList over de cachede brugere
    private ArrayList<User> users;

    //SIMON - Tiden over hvor længe cachen lever
    //Denne hentes i config.json klassen
    private long ttl;

    //SIMON - denne bliver sat når cachen bliver aktiveret
    private long created;

    public UserCache(){this.ttl = Config.getUserTtl();}//Værdier er pt. 3600

    public ArrayList<User> getUsers(Boolean forceUpdate){

        if (forceUpdate|| ((this.created + this.ttl)<= (System.currentTimeMillis()/1000L))
        || this.users==null){
            ArrayList<User>users = UserController.getUsers();

            this.users=users;
            this.created = System.currentTimeMillis()/1000L;
        }
        return this.users;

    }

}
