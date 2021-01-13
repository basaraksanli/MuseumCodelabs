package com.hms.codelabs.museum.ui.museumsearch

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.hms.codelabs.museum.R
import com.hms.codelabs.museum.models.Constant
import com.hms.codelabs.museum.utils.SearchUtils
import com.huawei.hms.site.api.model.Site
import java.text.DecimalFormat
import java.util.*

/**
 * Museum list adapter
 */
class MuseumListAdapter(context: Context?, list: ArrayList<Site>, private val searchUtils: SearchUtils) : RecyclerView.Adapter<MuseumListAdapter.ViewHolder>() {
    private val layoutInflater: LayoutInflater = LayoutInflater.from(context)
    private val museumList: ArrayList<Site>
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = layoutInflater.inflate(R.layout.museum_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.museumName.text = museumList[position].name
        holder.museumDescription.text = museumList[position].getFormatAddress()
        holder.distance.text = getDistance(museumList[position])
        holder.button.setOnClickListener { searchUtils.addBarrierToAwarenessKit(museumList[position], Constant.AWARENESS_BARRIER_RADIUS, Constant.AWARENESS_BARRIER_DURATION) }
    }

    override fun getItemCount(): Int {
        return museumList.size
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var museumName: TextView = view.findViewById<View>(R.id.museumName_Row) as TextView
        var museumDescription: TextView = view.findViewById<View>(R.id.museumDescription_Row) as TextView
        var distance: TextView = view.findViewById<View>(R.id.distance_Row) as TextView
        var button: ImageView = view.findViewById<View>(R.id.navigate_button) as ImageView
    }

    private fun getDistance(data: Site): String {
        val distance = data.getDistance()
        return if (distance > 1000) df2.format(distance / 1000) + " km" else df2.format(distance) + " m"
    }

    companion object {
        private val df2 = DecimalFormat("#.##")
    }

    init {
        list.sortBy { it.distance }
        museumList = list
    }
}