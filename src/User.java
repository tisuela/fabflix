import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * This User class only has the username field in this example.
 * You can add more attributes such as the user's shopping cart items.
 */
public class User {
    // customer email
    private final String username;

    // customer id
    private final int id;

    // total price of items in cart (price is not stored in this data structure_
    private int totalPrice = 0;

    // Cart is an map {movieId: quantity}
    private HashMap<String, Integer> cart;

    // Saved query parameters
    private Map<String, String[]> savedQueryParameters;

    private boolean savedQuery = false;

    public User(String username) {
        this(username, 0);
    }


    public User(String username, int id){
        this.username = username;
        this.id = id;
        this.cart = new HashMap<String, Integer>();
        this.savedQueryParameters = new HashMap<String, String[]>();
    }


    public User(String username, HashMap<String, Integer> cart){
        this(username);
        this.cart = cart;
    }


    // return customer id
    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }


    // --- cart stuff --- //


    // adds movie to cart and adds quantity
    public void addToCart(String movieId){
        this.cart.putIfAbsent(movieId, 0);
        this.cart.put(movieId, this.cart.get(movieId) + 1);
    }


    // decreases quantity of movie in cart
    public void decreaseQuantity(String movieId){
        this.cart.put(movieId, this.cart.get(movieId) - 1);

        // if the quantity is less than 1
        if (this.cart.get(movieId) < 1){
            this.removeFromCart(movieId);
        }
    }


    // removes movie from cart
    public void removeFromCart(String movieId){
        this.cart.remove(movieId);
    }


    // Empty cart after payment
    public void emptyCart(){
        this.cart.clear();
    }


    // Checks if items are in cart
    public boolean notEmpty(){
        int numberOfItems = 0;
        for(String key: this.cart.keySet()){
            ++numberOfItems;
        }

        return (numberOfItems > 0);
    }

    public HashMap<String, Integer> getCart() {
        return cart;
    }

    public int getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(int totalPrice) {
        this.totalPrice = totalPrice;
    }


    // --- saved Query stuff (might be used later, but we don't need it) --- //


    public Map<String, String[]> getSavedQueryParameters() {
        System.out.println("getting saved query");
        for(String key : this.savedQueryParameters.keySet()){
            System.out.println(key);
        }
        return this.savedQueryParameters;
    }

    public void setSavedQueryParameters(Map<String, String[]> savedQueryParameters) {
        this.savedQuery = true;
        this.clearSavedQueryParameters();

        System.out.println("setting saved query");
        for(String key : savedQueryParameters.keySet()){
            String[] toCopy = savedQueryParameters.get(key);
            String[] values = Arrays.copyOf(toCopy, toCopy.length);
            this.setParameter(key, values);
        }
    }

    public void setParameter(String parameter, String[] values){
        this.savedQueryParameters.put(parameter, values);
    }

    public void clearSavedQueryParameters(){
        this.savedQueryParameters.clear();
    }

    public boolean hasSavedQuery() {
        return this.savedQuery;
    }
}
