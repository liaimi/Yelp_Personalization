package mainAlg;
import java.util.HashSet;
import java.util.List;


public class Business {
    final double stars;
    double newstars; //change this instead of real stars
    double prevstars;
    int review_count;
    String id;
    String name;
    String address;
    List<String> categories;
    HashSet<User> users = new HashSet<User>();
    
    public Business(String id, String name, String address, List<String> categories, int review_count, double stars){
    	this.id = id;
    	this.name = name;
    	this.categories = categories;
    	this.review_count = review_count;
    	this.stars = stars;
    	this.newstars = stars;
    	this.prevstars = stars;
    	this.address = address;
    }
    
}
