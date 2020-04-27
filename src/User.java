import java.util.ArrayList;
import java.util.HashMap;

/**
 * This User class only has the username field in this example.
 * You can add more attributes such as the user's shopping cart items.
 */
public class User {

    private final String username;

    // Cart is an map {movieId: quantity}
    private HashMap<String, Integer> cart;

    public User(String username) {
        this.username = username;
        this.cart = new HashMap<String, Integer>();
    }


    public User(String username, HashMap<String, Integer> cart){
        this(username);
        this.cart = cart;
    }

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


    public String getUsername() {
        return username;
    }

    public HashMap<String, Integer> getCart() {
        return cart;
    }
}
