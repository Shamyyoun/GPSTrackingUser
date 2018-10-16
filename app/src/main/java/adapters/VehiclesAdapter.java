package adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.Button;
import android.widget.TextView;

import com.daimajia.swipe.SwipeLayout;
import com.mahmoudelshamy.gpstracking.userapp.R;

import java.util.List;

import datamodels.Vehicle;
import views.SlideExpandableListView;

public class VehiclesAdapter extends SlideExpandableListView.AnimatedExpandableListAdapter {

    private Context context;
    private int groupLayoutResourceId;
    private int childLayoutResourceId;
    private List<Vehicle> data;
    private VehicleListener vehicleListener;

    private LayoutInflater inflater;
    private Typeface typefaceLight;
    private Typeface typefaceMedium;

    public VehiclesAdapter(Context context, int groupLayoutResourceId, int childLayoutResourceId,
                           List<Vehicle> data, VehicleListener vehicleListener) {
        this.context = context;
        this.groupLayoutResourceId = groupLayoutResourceId;
        this.childLayoutResourceId = childLayoutResourceId;
        this.data = data;
        this.vehicleListener = vehicleListener;

        inflater = LayoutInflater.from(context);
        typefaceLight = Typeface.createFromAsset(context.getAssets(), "roboto_l.ttf");
        typefaceMedium = Typeface.createFromAsset(context.getAssets(), "roboto_m.ttf");
    }

    @Override
    public Vehicle getChild(int groupPosition, int childPosition) {
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
        Vehicle vehicle = getChild(groupPosition, childPosition);

        if (convertView == null) {
            holder = new ChildHolder();

            convertView = inflater.inflate(childLayoutResourceId, parent, false);
            holder.textPassword = (TextView) convertView.findViewById(R.id.text_password);
            holder.textPurpose = (TextView) convertView.findViewById(R.id.text_purpose);
            holder.textLicenceNumber = (TextView) convertView.findViewById(R.id.text_licenceNumber);
            holder.textNumber = (TextView) convertView.findViewById(R.id.text_number);
            holder.textColor = (TextView) convertView.findViewById(R.id.text_color);
            holder.textYear = (TextView) convertView.findViewById(R.id.text_year);
            holder.textModel = (TextView) convertView.findViewById(R.id.text_model);
            holder.textBrand = (TextView) convertView.findViewById(R.id.text_brand);

            // customize fonts
            holder.textPassword.setTypeface(typefaceLight);
            holder.textPurpose.setTypeface(typefaceLight);
            holder.textLicenceNumber.setTypeface(typefaceLight);
            holder.textNumber.setTypeface(typefaceLight);
            holder.textColor.setTypeface(typefaceLight);
            holder.textYear.setTypeface(typefaceLight);
            holder.textModel.setTypeface(typefaceLight);
            holder.textBrand.setTypeface(typefaceLight);

            convertView.setTag(holder);
        } else {
            holder = (ChildHolder) convertView.getTag();
        }

        // set data
        holder.textPassword.setText(context.getString(R.string.password) + ": " + vehicle.getPassword());

        if (vehicle.getPurpose().isEmpty()) {
            holder.textPurpose.setVisibility(View.GONE);
        } else {
            holder.textPurpose.setText(context.getString(R.string.purpose) + ": " + vehicle.getPurpose());
            holder.textPurpose.setVisibility(View.VISIBLE);
        }

        if (vehicle.getLicenceNumber() == 0) {
            holder.textLicenceNumber.setVisibility(View.GONE);
        } else {
            holder.textLicenceNumber.setText(context.getString(R.string.licence_number) + ": " + vehicle.getLicenceNumber());
            holder.textLicenceNumber.setVisibility(View.VISIBLE);
        }

        if (vehicle.getNumber() == 0) {
            holder.textNumber.setVisibility(View.GONE);
        } else {
            holder.textNumber.setText(context.getString(R.string.number) + ": " + vehicle.getNumber());
            holder.textNumber.setVisibility(View.VISIBLE);
        }

        if (vehicle.getColor().isEmpty()) {
            holder.textColor.setVisibility(View.GONE);
        } else {
            holder.textColor.setText(context.getString(R.string.color) + ": " + vehicle.getColor());
            holder.textColor.setVisibility(View.VISIBLE);
        }

        if (vehicle.getYear() == 0) {
            holder.textYear.setVisibility(View.GONE);
        } else {
            holder.textYear.setText(context.getString(R.string.year) + ": " + vehicle.getYear());
            holder.textYear.setVisibility(View.VISIBLE);
        }

        if (vehicle.getModel().isEmpty()) {
            holder.textModel.setVisibility(View.GONE);
        } else {
            holder.textModel.setText(context.getString(R.string.model) + ": " + vehicle.getModel());
            holder.textModel.setVisibility(View.VISIBLE);
        }

        if (vehicle.getBrand().isEmpty()) {
            holder.textBrand.setVisibility(View.GONE);
        } else {
            holder.textBrand.setText(context.getString(R.string.brand) + ": " + vehicle.getBrand());
            holder.textBrand.setVisibility(View.VISIBLE);
        }

        return convertView;
    }

    @Override
    public int getRealChildrenCount(int groupPosition) {
        return 1;
    }

    @Override
    public Vehicle getGroup(int groupPosition) {
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
        final Vehicle vehicle = getGroup(groupPosition);

        if (convertView == null) {
            holder = new GroupHolder();
            convertView = inflater.inflate(groupLayoutResourceId, parent, false);

            holder.cardView = (CardView) convertView.findViewById(R.id.cardView);
            holder.textId = (TextView) convertView.findViewById(R.id.text_id);
            holder.textName = (TextView) convertView.findViewById(R.id.text_name);
            holder.buttonEdit = (Button) convertView.findViewById(R.id.button_edit);
            holder.buttonDelete = (Button) convertView.findViewById(R.id.button_delete);

            // customize fonts
            holder.textName.setTypeface(typefaceMedium);
            holder.textId.setTypeface(typefaceLight);
            holder.buttonEdit.setTypeface(typefaceLight);
            holder.buttonDelete.setTypeface(typefaceLight);

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
        holder.textId.setText(vehicle.getId());
        holder.textName.setText(vehicle.getName());

        // add listeners
        holder.buttonEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vehicleListener.editVehicle(vehicle, groupPosition);
            }
        });
        holder.buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vehicleListener.deleteVehicle(vehicle, groupPosition);
            }
        });

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
        TextView textId;
        TextView textName;
        Button buttonEdit;
        Button buttonDelete;
    }

    private static class ChildHolder {
        TextView textPassword;
        TextView textPurpose;
        TextView textLicenceNumber;
        TextView textNumber;
        TextView textColor;
        TextView textYear;
        TextView textModel;
        TextView textBrand;
    }

    /**
     * interface used to do vehicle's actions from VehiclesAdapter class in VehiclesActivity class
     */
    public static interface VehicleListener {
        public void editVehicle(Vehicle vehicle, int position);

        public void deleteVehicle(Vehicle vehicle, int position);
    }
}
