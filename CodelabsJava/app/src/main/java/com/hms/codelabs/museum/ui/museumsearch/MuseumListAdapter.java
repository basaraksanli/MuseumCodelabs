package com.hms.codelabs.museum.ui.museumsearch;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hms.codelabs.museum.models.Constant;
import com.hms.codelabs.museum.utils.SearchUtils;
import com.huawei.hms.site.api.model.Site;

import java.text.DecimalFormat;
import java.util.ArrayList;
import com.hms.codelabs.museum.R;

/**
 * Museum list adapter
 */
public class MuseumListAdapter extends RecyclerView.Adapter<MuseumListAdapter.ViewHolder> {

    private final LayoutInflater layoutInflater;
    private final ArrayList<Site> museumList;
    private final SearchUtils searchUtils;

    private static final DecimalFormat df2 = new DecimalFormat("#.##");

    public MuseumListAdapter(Context context , ArrayList<Site> list, SearchUtils searchUtils){
        this.layoutInflater =  LayoutInflater.from(context);
        this.searchUtils = searchUtils;

        list.sort((site1, site2) -> (int) (site1.getDistance() - site2.getDistance()));
        museumList = list;
    }
    @NonNull
    @Override
    public MuseumListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.museum_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.museum_name.setText(museumList.get(position).name);
        holder.museum_description.setText(museumList.get(position).getFormatAddress());
        holder.distance.setText(getDistance(museumList.get(position)));

        holder.button.setOnClickListener(v -> searchUtils.addBarrierToAwarenessKit(museumList.get(position), Constant.AWARENESS_BARRIER_RADIUS, Constant.AWARENESS_BARRIER_DURATION));
    }
    @Override
    public int getItemCount() {
        return museumList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView museum_name;
        TextView museum_description;
        TextView distance;
        ImageView button;

        public ViewHolder(View view) {
            super(view);

            museum_name = (TextView)view.findViewById(R.id.museumName_Row);
            museum_description  = (TextView)view.findViewById(R.id.museumDescription_Row);
            distance = (TextView)view.findViewById(R.id.distance_Row);
            button = (ImageView) view.findViewById(R.id.navigate_button);
        }
    }
    public String getDistance(Site data) {
        double distance = data.getDistance();
        if (distance > 1000)
            return (df2.format(distance / 1000) + " km");
        else
            return (df2.format(distance) + " m");
    }
}
