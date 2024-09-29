package za.co.varsitycollege.st10204772.opsc7312_poe

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class GenreAdapter(private val genres: List<String>) : RecyclerView.Adapter<GenreAdapter.GenreViewHolder>() {
    class GenreViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val genreName: TextView = itemView.findViewById(R.id.genreName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GenreViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_genre, parent, false)
        return GenreViewHolder(view)
    }

    override fun onBindViewHolder(holder: GenreViewHolder, position: Int) {
        holder.genreName.text = (position+1).toString() + ". " + genres[position]
    }

    override fun getItemCount() = genres.size
}
