package adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Shamyyoun on 4/30/2015.
 */
public class SpinnerAdapter<T> extends BaseAdapter {
    private int layoutRes;
    private LayoutInflater mInflater;
    private List<T> items;

    private Typeface typeface;

    public SpinnerAdapter(Context context, int layoutRes, List<T> items) {
        this.layoutRes = layoutRes;

        mInflater = LayoutInflater.from(context);
        this.items = items;
        typeface = Typeface.createFromAsset(context.getAssets(), "roboto_l.ttf"); // default font
    }

    public SpinnerAdapter(Context context, int layoutRes, T[] items) {
        this(context, layoutRes, Arrays.asList(items));
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        View view = convertView;
        if (view == null) {
            view = mInflater.inflate(layoutRes, null);
            holder = new ViewHolder();

            holder.textView = (TextView) view;
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        holder.textView.setTypeface(typeface);
        holder.textView.setText(items.get(position).toString());

        return view;
    }

    static class ViewHolder {
        TextView textView;
    }

    public void setTypeface(Typeface typeface) {
        this.typeface = typeface;
    }
}
