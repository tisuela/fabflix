import java.util.ArrayList;

/**
 * This User class only has the username field in this example.
 * You can add more attributes such as the user's shopping cart items.
 */
public class User {

    private final String username;

    // Cart is an arraylist of movieIds
    private ArrayList<String> cart;

    public User(String username) {
        this.username = username;
        this.cart = new ArrayList<String>();
    }


    public User(String username, ArrayList<String> cart){
        this(username);
        this.cart = cart;
    }


    public void addToCart(String movieId){
        cart.add(movieId);
    }


    public String getUsername() {
        return username;
    }

    public ArrayList<String> getCart() {
        return cart;
    }
}
