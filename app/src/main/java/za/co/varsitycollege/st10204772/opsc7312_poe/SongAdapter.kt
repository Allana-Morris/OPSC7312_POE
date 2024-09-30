package za.co.varsitycollege.st10204772.opsc7312_poe

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SongAdapter(private val songs: SpotifyData) : RecyclerView.Adapter<SongAdapter.SongViewHolder>() {

    class SongViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val songName: TextView = itemView.findViewById(R.id.trackName)
        // Add other views if necessary
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_track, parent, false)
        return SongViewHolder(view)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        var songartist = songs.artistName.zip(songs.songartistID.zip(songs.artistID))
            .filter { (_, ids) -> ids.first == ids.second }
            .map { (name, _) -> name }
        holder.songName.text = (position+1).toString() + ". " + songs.songName[position] + "By " + "$songartist"
        // Bind other data if necessary
    }

    override fun getItemCount() = songs.songName.size
}

