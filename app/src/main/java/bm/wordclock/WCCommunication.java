package bm.wordclock;

import android.support.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.ByteBuffer;

/**
 * Created by mrks on 04.02.17.
 */

public class WCCommunication {
    private DataInputStream dis;
    private DataOutputStream dos;
    private Socket socket;
    private boolean connected = false;

    private final String mHostName;

    private static final int PORT = 8081;
    public static final int API_LEVEL = 1;

    public static class ProtocolException extends Exception {
        public ProtocolException(String what) {
            super(what);
        }
    }

    public static class APILevelException extends ProtocolException {
        private final int mReportedLevel;

        public APILevelException(int reportedLevel) {
            super("API level mismatch. Reported " + reportedLevel + ", expected " + API_LEVEL);
            mReportedLevel = reportedLevel;
        }
    }

    public WCCommunication(@NonNull String hostName) {
        mHostName = hostName;
    }

    public synchronized void connect() throws IOException {
        disconnect();
        InetAddress addr = InetAddress.getByName(mHostName);
        socket = new Socket(addr, PORT);
        InputStream is = socket.getInputStream();
        dis = new DataInputStream(is);
        OutputStream os = socket.getOutputStream();
        dos = new DataOutputStream(os);
        connected = true;
    }

    public synchronized void disconnect() {
        if (connected) {
            try {
                if (dis != null)
                    dis.close();
            } catch (IOException e) {
            } finally {
                dis = null;
            }
            try {
                if (dos != null)
                    dos.close();
            } catch (IOException e) {
            } finally {
                dos = null;
            }
            try {
                if (socket != null)
                    socket.close();
            } catch (IOException e) {
            } finally {
                socket = null;
            }
            connected = false;
        }
    }

    public JSONObject readObject() throws IOException, JSONException, ProtocolException {
        int len = dis.readInt();
        byte[] buff = new byte[len];
        dis.readFully(buff);
        String response = new String(buff, "UTF-8");
        JSONObject obj = new JSONObject(response);
        try {
            int apiLevel = obj.getInt("API");
            if (apiLevel != API_LEVEL)
                throw new APILevelException(apiLevel);
        } catch (JSONException e) {
            throw new ProtocolException("Could not read API level");
        }
        return obj;
    }

    public synchronized void writeObject(JSONObject obj) throws IOException {
        String serialized = obj.toString();
        byte[] buff = serialized.getBytes("UTF-8");
        if (dos != null) {
            dos.writeInt(buff.length);
            dos.write(buff);
            dos.flush();
        }
    }

    protected synchronized void writeRaw(byte [] message) throws IOException {
        if (dos != null) {
            dos.write(message);
            dos.flush();
        }
    }

    public static JSONObject createMessage() {
        JSONObject req = new JSONObject();
        try {
            req.put("API", API_LEVEL);
        } catch (JSONException e) {
        } /* should never happen */
        return req;
    }

    protected static byte [] getRawBuffer(JSONObject obj) throws UnsupportedEncodingException {
        String serialized = obj.toString();
        byte[] buff = serialized.getBytes("UTF-8");
        return ByteBuffer.allocate(4 + buff.length)
                .putInt(buff.length)
                .put(buff)
                .array();
    }

}
