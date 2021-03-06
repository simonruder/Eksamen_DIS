package cache;

import controllers.OrderController;
import java.util.ArrayList;
import model.Order;
import utils.Config;

//TODO: Build this cache and use it.:FIXED


//SIMON TODO:Implementér den i koden:FIXED
//SIMON - Indspireret af ProductCache
public class OrderCache {

    //Laver en liste over orderer
    private ArrayList<Order> orders;

    //Tiden hvorlænge cachen lever
    private long ttl;

    //Bliver sat når cachen bliver aktiveret
    private long created;

    public OrderCache(){
        this.ttl = Config.getOrderTtl();//Genbruger ttl = 3600 sekunder
    }

    public ArrayList<Order> getOrders(Boolean forceUpdate) {

        if (forceUpdate || ((this.created + this.ttl)<= (System.currentTimeMillis()/1000L ))//SIMON - Vi skal kun ind i dette loop, hvis tiden er gået eller gvis vi har lavet en ny order via endpointet
            || this.orders==null){

            //Hvis listen er tom, så henter vi ordrene fra OrderControlleren
            ArrayList<Order>orders = OrderController.getOrders();

            this.orders = orders;
            this.created = System.currentTimeMillis() / 1000L;
        }
        return this.orders;
    }
}
