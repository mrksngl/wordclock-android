package bm.wordclock;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;

import java.util.Collection;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link PluginsListFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class PluginsListFragment extends ListFragment {

    private OnFragmentInteractionListener mListener;
    private PluginListAdapter mPluginsAdapter;

    public PluginsListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ListView lv = this.getListView();
        lv.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        lv.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);
    }

    public void setPlugins(Collection<Plugin> plugins) {
        if (plugins == null)
            setListAdapter(null);
        else {
            mPluginsAdapter = new PluginListAdapter(getContext(), plugins);
            setListAdapter(mPluginsAdapter);
        }
    }

    public void setCurrentPlugin(int index) {
        mPluginsAdapter.setSelectedIndex(index);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        mPluginsAdapter.setSelectedIndex(position);
        if (mListener != null)
            mListener.onPluginChange(position);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement AppsFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    public interface OnFragmentInteractionListener {
        void onPluginChange(int index);
    }
}
