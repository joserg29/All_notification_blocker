package com.projects.allnotificationblocker.blockthemall.Activities.Profiles

import android.view.*
import android.widget.*
import androidx.recyclerview.widget.*
import com.projects.allnotificationblocker.blockthemall.*
import com.projects.allnotificationblocker.blockthemall.data.db.entities.*

/**
 * A custom adapter to use with the RecyclerView widget.
 */
class SelectProfileRecyclerViewAdapter(
    private var modelList: ArrayList<Profile>,
): RecyclerView.Adapter<RecyclerView.ViewHolder?>() {
    private var mItemClickListener: OnItemClickListener? = null

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.list_item_select_profile, viewGroup, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        //Here you can fill your row view

        if (holder is ViewHolder) {
            val profile = getItem(position)
            val genericViewHolder = holder

            genericViewHolder.itemTxtTitle.text = profile.name
            genericViewHolder.itemTxtMessage.text = profile.description
            if (profile.description!!.isEmpty()) genericViewHolder.itemTxtMessage.visibility =
                View.GONE
        }
    }

    override fun getItemCount(): Int {
        return modelList.size
    }

    fun SetOnItemClickListener(mItemClickListener: OnItemClickListener) {
        this.mItemClickListener = mItemClickListener
    }

    private fun getItem(position: Int): Profile {
        return modelList[position]
    }


    interface OnItemClickListener {
        fun onItemClick(view: View?, position: Int, model: Profile?)

        fun deleteProfile(profile: Profile?)

        fun editProfile(profile: Profile?)
    }

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val itemTxtTitle: TextView
        val itemTxtMessage: TextView
        val textViewDeleteProfile: Button
        val textViewApplyProfile: Button
        val textViewEditProfile: Button

        init {
            val imgUser = itemView.findViewById<ImageView?>(R.id.img_user)
            this.itemTxtTitle = itemView.findViewById<TextView>(R.id.item_txt_title)
            this.itemTxtMessage = itemView.findViewById<TextView>(R.id.item_txt_message)
            this.textViewDeleteProfile =
                itemView.findViewById<Button>(R.id.text_view_delete_profile)
            this.textViewApplyProfile = itemView.findViewById<Button>(R.id.text_view_apply_profile)
            this.textViewEditProfile = itemView.findViewById<Button>(R.id.text_view_edit_profile)

            this.textViewApplyProfile.setOnClickListener(object: View.OnClickListener {
                override fun onClick(view: View?) {
                    mItemClickListener!!.onItemClick(
                        itemView,
                        adapterPosition,
                        modelList.get(adapterPosition)
                    )
                }
            })

            this.textViewEditProfile.setOnClickListener(object: View.OnClickListener {
                override fun onClick(view: View?) {
                    mItemClickListener!!.editProfile(modelList.get(adapterPosition))
                }
            })

            textViewDeleteProfile.setOnClickListener(object: View.OnClickListener {
                override fun onClick(view: View?) {
                    mItemClickListener!!.deleteProfile(modelList.get(adapterPosition))
                }
            })
        }
    }
}

