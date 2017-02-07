package bm.wordclock;

import android.support.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by mrks on 04.02.17.
 */

public class WCProtocol extends WCCommunication {

    private final WCCallbacks mCallbacks;

    public WCProtocol(@NonNull String hostName, @NonNull WCCallbacks callbacks) {
        super(hostName);
        mCallbacks = callbacks;
    }

    public void getConfiguration() throws IOException {
        try {
            JSONObject reqConfig = createMessage();
            reqConfig.put("GET_CONFIG", 0);
            writeObject(reqConfig);
        } catch (JSONException e) {
            /* should never happen */
        }
    }

    public void setActivePlugin(int index) throws IOException {
        try {
            JSONObject msg = createMessage();
            msg.put("SET_ACTIVE_PLUGIN", index);
            writeObject(msg);
        } catch (JSONException e) {
            /* should never happen */
        }
    }

    public void readAndProcess() throws IOException, ProtocolException {
        JSONObject json = readObject();

        JSONArray plugins = json.optJSONArray("PLUGINS");
        if (plugins != null) {
            List<Plugin> list = new LinkedList<>();
            try {
                for (int i = 0; i < plugins.length(); ++i) {
                    String name = plugins.getString(i);
                    list.add(new Plugin(name));
                }
            } catch (JSONException e) {
                throw new ProtocolException("Malformed plugin list");
            }
            mCallbacks.setPlugins(list);
        }

        int activePlugin = json.optInt("ACTIVE_PLUGIN", -1);
        if (activePlugin != -1) {
            mCallbacks.setActivePlugin(activePlugin);
        }
    }

}
