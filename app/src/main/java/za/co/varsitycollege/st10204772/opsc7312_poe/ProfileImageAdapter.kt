package za.co.varsitycollege.st10204772.opsc7312_poe

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView

class ProfileImageAdapter(private val profileImageList: List<Bitmap>) : RecyclerView.Adapter<ProfileImageAdapter.ProfileImageViewHolder>() {

    inner class ProfileImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.profileImageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileImageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.profile_image_item, parent, false)
        return ProfileImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProfileImageViewHolder, position: Int) {
        val profileImage = profileImageList[position]
        holder.imageView.setImageBitmap(profileImage)
    }

    override fun getItemCount(): Int {
        return profileImageList.size
    }
}