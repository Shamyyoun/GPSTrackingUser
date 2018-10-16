package adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.ImageView;
import android.widget.TextView;

import com.mahmoudelshamy.gpstracking.userapp.FindOnMapActivity;
import com.mahmoudelshamy.gpstracking.userapp.R;

import java.text.SimpleDateFormat;
import java.util.List;

import datamodels.Constants;
import datamodels.Trip;
import views.SlideExpandableListView;

public class TripsAdapter extends SlideExpandableListView.AnimatedExpandableListAdapter {

    private Context context;
    private int groupLayoutResourceId;
    private int childLayoutResourceId;
    private List<Trip> data;

    private LayoutInflater inflater;
    private Typeface typeface;

    public TripsAdapter(Context context, int groupLayoutResourceId, int childLayoutResourceId,
                        List<Trip> data) {
        this.context = context;
        this.groupLayoutResourceId = groupLayoutResourceId;
        this.childLayoutResourceId = childLayoutResourceId;
        this.data = data;

        inflater = LayoutInflater.from(context);
        typeface = Typeface.createFromAsset(context.getAssets(), "roboto_l.ttf");
    }

    @Override
    public Trip getChild(int groupPosition, int childPosition) {
        return data.get(groupPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getRealChildView(int groupPosition, int childPosition,
                                 boolean isLastChild, View convertView, ViewGroup parent) {

        ChildHolder holder;
        Trip trip = getChild(groupPosition, childPosition);

        if (convertView == null) {
            holder = new ChildHolder();

            convertView = inflater.inflate(childLayoutResourceId, parent, false);
            holder.textEndedIn = (TextView) convertView.findViewById(R.id.text_ended_in);
            holder.textEndedAt = (TextView) convertView.findViewById(R.id.text_ended_at);
            holder.textDistance = (TextView) convertView.findViewById(R.id.text_distance);
            holder.textDuration = (TextView) convertView.findViewById(R.id.text_duration);
            holder.textSpeed = (TextView) convertView.findViewById(R.id.text_speed);

            // customize fonts
            holder.textEndedIn.setTypeface(typeface);
            holder.textEndedAt.setTypeface(typeface);
            holder.textDistance.setTypeface(typeface);
            holder.textDuration.setTypeface(typeface);
            holder.textSpeed.setTypeface(typeface);

            convertView.setTag(holder);
        } else {
            holder = (ChildHolder) convertView.getTag();
        }

        // set data
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm a");
        String endDate = sdf.format(trip.getEndDate());
        String distance = trip.getDistance() < 1000 ?
                ("" + trip.getDistance()).substring(0, ("" + trip.getDistance()).indexOf(".") + 2) + " " + context.getString(R.string.m) :
                ("" + (trip.getDistance() / 1000)).substring(0, ("" + (trip.getDistance() / 1000)).indexOf(".") + 2) + " " + context.getString(R.string.km);
        String duration = "";
        if (trip.getDuration()[3] > 0) {
            duration += trip.getDuration()[3] + " " + context.getString(R.string.days) + " - ";
        }
        if (trip.getDuration()[2] > 0) {
            duration += trip.getDuration()[2] + " " + context.getString(R.string.hours) + " - ";
        }
        duration += trip.getDuration()[1] + " " + context.getString(R.string.minutes);
        String speed = ("" + trip.getSpeed()).substring(0, ("" + trip.getSpeed()).indexOf(".") + 2) + " " + context.getString(R.string.km_h);

        holder.textEndedIn.setText(context.getString(R.string.ended_in) + ": " + trip.getEndLocationTitle());
        holder.textEndedAt.setText(context.getString(R.string.ended_at) + ": " + endDate);
        holder.textDistance.setText(context.getString(R.string.distance) + ": " + distance);
        holder.textDuration.setText(context.getString(R.string.duration) + ": " + duration);
        holder.textSpeed.setText(context.getString(R.string.speed) + ": " + speed);

        return convertView;
    }

    @Override
    public int getRealChildrenCount(int groupPosition) {
        return 1;
    }

    @Override
    public Trip getGroup(int groupPosition) {
        return data.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return data.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(final int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {

        final GroupHolder holder;
        final Trip trip = getGroup(groupPosition);

        if (convertView == null) {
            holder = new GroupHolder();
            convertView = inflater.inflate(groupLayoutResourceId, parent, false);

            holder.cardView = (CardView) convertView.findViewById(R.id.cardView);
            holder.textStartedFrom = (TextView) convertView.findViewById(R.id.text_started_from);
            holder.textStartedAt = (TextView) convertView.findViewById(R.id.text_started_at);
            holder.imageFindOnMap = (ImageView) convertView.findViewById(R.id.image_findOnMap);

            // customize fonts
            holder.textStartedAt.setTypeface(typeface);
            holder.textStartedFrom.setTypeface(typeface);;

            convertView.setTag(holder);
        } else {
            holder = (GroupHolder) convertView.getTag();
        }

        // customize margins
        MarginLayoutParams layoutParams = (MarginLayoutParams) holder.cardView.getLayoutParams();
        if (groupPosition == 0) {
            // add margin top to first card view
            layoutParams.topMargin = (int) context.getResources().getDimension(R.dimen.card_view_margin);
        } else {
            // remove top margin from this item
            layoutParams.topMargin = 0;
        }
        holder.cardView.setLayoutParams(layoutParams);

        // set data
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm a");
        String startDate = sdf.format(trip.getStartDate());
        holder.textStartedFrom.setText(context.getString(R.string.started_from) + ": " + trip.getStartLocationTitle());
        holder.textStartedAt.setText(context.getString(R.string.started_at) + ": " + startDate);

        // check if ended
        if (trip.isEnded()) {
            // --ended--
            // show find on map icon
            holder.imageFindOnMap.setVisibility(View.VISIBLE);
            // add listeners
            holder.imageFindOnMap.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // open find on map activity
                    Intent intent = new Intent(context, FindOnMapActivity.class);
                    intent.putExtra(Constants.KEY_TRIP, trip);
                    context.startActivity(intent);
                }
            });
        } else {
            // hide find on map icon
            holder.imageFindOnMap.setVisibility(View.GONE);
        }

        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public boolean isChildSelectable(int arg0, int arg1) {
        return true;
    }

    private static class GroupHolder {
        CardView cardView;
        TextView textStartedFrom;
        TextView textStartedAt;
        ImageView imageFindOnMap;
    }

    private static class ChildHolder {
        TextView textEndedIn;
        TextView textEndedAt;
        TextView textDistance;
        TextView textDuration;
        TextView textSpeed;
    }
}
