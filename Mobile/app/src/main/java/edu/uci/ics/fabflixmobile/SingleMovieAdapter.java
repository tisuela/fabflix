package edu.uci.ics.fabflixmobile;


import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/* Adapter for the recycler view seen in SingleMovieActivity
 * Shows list of Stars -- see Star class
 */
public class SingleMovieAdapter extends RecyclerView.Adapter<SingleMovieAdapter.StarViewHolder> {
    private List<Star> stars;


    /* The view holder "holds" a view, in this case, it will hold
     * the view defined in layout star_row.xml
     * When this class is created, it will take in the layout
     * "star_row.xml" as a parameter.
     */
    public static class StarViewHolder extends RecyclerView.ViewHolder {
        public TextView starName, starBirthYear;
        public View rowView;

        public StarViewHolder(@NonNull View itemView) {
            super(itemView);
            rowView = itemView;
            starName = itemView.findViewById(R.id.star_name);
            starBirthYear = itemView.findViewById(R.id.star_birth_year);
        }
    }

    public SingleMovieAdapter(List<Star> stars) {
        this.stars = stars;
    }



    /* Creates the view holder (defined above)
     */
    @NonNull
    @Override
    public StarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View starRowView = LayoutInflater.from(parent.getContext()).inflate(R.layout.star_row, parent, false);
        StarViewHolder starViewHolder = new StarViewHolder(starRowView);

        return starViewHolder;
    }


    private void setListeners(View view, int position){
        // Lead user to single-movies using this listener
        Star star = stars.get(position);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String message = String.format("Clicked on position: %d star: %s, %s", position, star.getName(), star.getBirthYear());
                Log.d("SingleMovie.clicked", message);

                // open Single Star
                openSingleStar(star);
            }
        });
    }

    // opens single star page
    public void openSingleStar(Star star){
        /* TODO open SingleStarActivity
         * (bundle starId using star.getId(),
         * but u can do that later --  just insert a known starId for now)
         * Intent intent = new Intent(this, SingleStarActivity.class);
         * startActivity(intent);
         */
    }


    /* Fills in the view (defined in star_row.xml) by using the starViewHolder
     * and the stars List
     */
    @Override
    public void onBindViewHolder(@NonNull StarViewHolder holder, int position) {
        holder.starName.setText(stars.get(position).getName());
        holder.starBirthYear.setText(stars.get(position).getBirthYear());
        setListeners(holder.rowView, position);
    }

    // Get size of the array (so the Recycle View knows how many rows to make)
    @Override
    public int getItemCount() {
        return this.stars.size();
    }
}
