package bm.wordclock;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by mrks on 04.02.17.
 */

public class PluginListAdapter extends ArrayAdapter<Plugin> {
    private final LayoutInflater mInflater;
    private int mCurrentSelected;

    public PluginListAdapter(Context context, Collection<Plugin> plugins) {
        super(context, R.layout.plugin_list_item, new ArrayList<>(plugins));

        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        if (convertView != null) {
            view = convertView;
        } else {
            view = mInflater.inflate(R.layout.plugin_list_item, parent, false);
        }
        TextView name = (TextView) view.findViewById(R.id.plugin_list_item_name);
        TextView desc = (TextView) view.findViewById(R.id.plugin_list_item_description);
        RadioButton btn = (RadioButton) view.findViewById(R.id.plugin_list_item_btn);

        Plugin app = getItem(position);

        name.setText(app.getName());
        if (app.hasDescription()) {
            desc.setVisibility(View.VISIBLE);
            desc.setText(app.getDescription());
        } else {
            desc.setVisibility(View.INVISIBLE);
        }
        btn.setChecked(position == mCurrentSelected);

        return view;
    }

    public void setSelectedIndex(int index) {
        mCurrentSelected = index;
        notifyDataSetChanged();
    }
}
