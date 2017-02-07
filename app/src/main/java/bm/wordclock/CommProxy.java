package bm.wordclock;

import android.app.Activity;
import android.support.annotation.NonNull;

import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Created by mrks on 04.02.17.
 *
 * Translate all events of the UI (running in the UI thread) to calls in the network thread and
 * vice versa.
 */
public class CommProxy {
    /** Callbacks from this object (intend: these will be called on the UI thread) */
    private final WCCommCallbacks mCallbacks;
    /** Networking: connection handling and protocol, extended by thread decoupling */
    private final WCProtocolProxy mProxy;
    /** Activity we're running on (our callbacks will be injected in its looper) */
    private final Activity mActivity;
    /** Input thread (most of the time: blocking read on network socket) */
    private Thread mInThread;
    /** Output thread (most of the time: waiting for some packet to send) */
    private Thread mOutThread;
    /** Flag whether threads are supposed to run */
    private volatile boolean mRunning;

    /** Implementation of the callbacks from the wordclock: just call the corresponding method
     * on the UI thread */
    private final WCCallbacks mProxyCallbacks = new WCCallbacks() {
        @Override
        public void setPlugins(final Collection<Plugin> plugins) {
            runOnActivity(new Runnable() {
                @Override
                public void run() {
                    mCallbacks.setPlugins(plugins);
                }
            });
        }

        @Override
        public void setActivePlugin(final int index) {
            runOnActivity(new Runnable() {
                @Override
                public void run() {
                    mCallbacks.setActivePlugin(index);
                }
            });
        }
    };

    public CommProxy(@NonNull Activity activity, @NonNull String hostName,
                     @NonNull WCCommCallbacks callbacks) {
        mActivity = activity;
        mCallbacks = callbacks;
        mProxy = new WCProtocolProxy(hostName, mProxyCallbacks);
    }

    public void start() {
        mRunning = true;
        mInThread = new Thread(new ReceiveLoop());
        mOutThread = new Thread(mProxy);
        mInThread.start();
        mOutThread.start();
    }

    public void stop() {
        mRunning = false;
        mInThread.interrupt();
        mOutThread.interrupt();
        mProxy.disconnect();
    }

    /** Translate calls from the UI thread to messages for the wordclock and enqueue them in an
     * output queue.
     * Idea: We could collect Runnables in a queue, which are handled by the output thread,
     *  thereby creating and sending the messages.
     *  But this is what we actually do instead: we create the message right now, and enqueue
     *  the message into the queue. This way we have way less calls in between and creating the
     *  message is not expected to cause an ANR.
     */
    private class WCProtocolProxy extends WCProtocol implements Runnable {
        /** Output Queue of messages */
        private final LinkedBlockingDeque<byte[]> outQueue = new LinkedBlockingDeque<>();

        private WCProtocolProxy(@NonNull String hostName, @NonNull WCCallbacks callbacks) {
            super(hostName, callbacks);
        }

        private void post(byte [] pkt) {
            outQueue.add(pkt);
        }

        @Override
        public void run() {
            while (mRunning) {
                byte [] l;
                try {
                    /* get a message from the queue */
                    l = outQueue.take();
                } catch (InterruptedException e) {
                    continue;
                }
                try {
                    /* Send it */
                    super.writeRaw(l);
                } catch (IOException ignored) {
                    /* input thread will handle that */
                    outQueue.clear();
                }
            }
        }

        @Override
        public void writeObject(JSONObject obj) throws UnsupportedEncodingException {
            /* this is where the magic begins: we let the methods of WCProtocol build the message,
             * but when they call writeObject they will land here, enqueuing the message
             */
            post(getRawBuffer(obj));
        }

        @Override
        public void writeRaw(byte [] message) {
            /* similar */
            post(message);
        }
    }

    public void setActivePlugin(int index) {
        try {
            mProxy.setActivePlugin(index);
        } catch (IOException ignored) {
        }
    }

    private void runOnActivity(Runnable r) {
        if (mRunning)
            mActivity.runOnUiThread(r);
    }

    /** This is the input thread, it also handles the connection state */
    private class ReceiveLoop implements Runnable {
        private void setStateOnActivity(final WCCommCallbacks.STATE state) {
            runOnActivity(new Runnable() {
                @Override
                public void run() {
                    mCallbacks.setState(state);
                }
            });
        }

        private void tryConnect() {
            while (mRunning) {
                try {
                    mProxy.connect();
                    setStateOnActivity(WCCommCallbacks.STATE.CONNECTED);
                    return;
                } catch (UnknownHostException e) {
                    setStateOnActivity(WCCommCallbacks.STATE.HOST_UNKNOWN);
                } catch (IOException e) {
                    setStateOnActivity(WCCommCallbacks.STATE.COULD_NOT_CONNECT);
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) {
                }
            }
        }

        @Override
        public void run() {
            while (mRunning) {
                /* Try to connect */
                tryConnect();

                if (!mRunning)
                    break;

                try {
                    /* Get configuration */
                    mProxy.getConfiguration();

                    /* read and process packets */
                    while (mRunning)
                        mProxy.readAndProcess();
                } catch (WCCommunication.ProtocolException | IOException e) {
                    mProxy.disconnect();
                    /* TODO: give more information to the user, especially on ProtocolException */
                    setStateOnActivity(WCCommCallbacks.STATE.DISCONNECTED);
                }
            }
            mProxy.disconnect();
        }
    }
}
