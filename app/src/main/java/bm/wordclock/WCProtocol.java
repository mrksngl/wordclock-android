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

    protected final WCCallbacks mCallbacks;

    /* must match wordclock */
    private static final int EVENT_LEFT = 0;
    private static final int EVENT_RIGHT = 1;
    private static final int EVENT_RETURN = 2;

    private static final byte [] rawPacketLeft;
    private static final byte [] rawPacketRight;
    private static final byte [] rawPacketReturn;

    private static byte [] makeSimpleRawPkg(String cmd, int param) {
        try {
            JSONObject obj = createMessage();
            obj.put(cmd, param);
            return getRawBuffer(obj);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    static {
        rawPacketLeft = makeSimpleRawPkg("SEND_EVENT", EVENT_LEFT);
        rawPacketRight = makeSimpleRawPkg("SEND_EVENT", EVENT_RIGHT);
        rawPacketReturn = makeSimpleRawPkg("SEND_EVENT", EVENT_RETURN);
    }

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

    public void btn_left() throws IOException {
        writeRaw(rawPacketLeft);
    }

    public void btn_right() throws IOException {
        writeRaw(rawPacketRight);
    }

    public void btn_return() throws IOException {
        writeRaw(rawPacketReturn);
    }

    public void readAndProcess() throws IOException, ProtocolException {
        JSONObject json = readObject();

        JSONArray plugins = json.optJSONArray("PLUGINS");
        if (plugins != null) {
            List<Plugin> list = new LinkedList<>();
            try {
                for (int i = 0; i < plugins.length(); ++i) {
                    JSONObject p = plugins.getJSONObject(i);
                    String name = p.getString("NAME");
                    String description = p.getString("DESCRIPTION");
                    list.add(new Plugin(name, description));
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
