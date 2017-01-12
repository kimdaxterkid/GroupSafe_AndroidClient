package edu.vt.scm.groupsafe;

import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import org.json.JSONException;
import org.json.JSONObject;
import android.app.Activity;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.List;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class ChatFragment extends Fragment {

    private RecyclerView mMessagesView;
    private EditText mInputMessageView;
    private List<Message> mMessages = new ArrayList<Message>();
    private RecyclerView.Adapter mAdapter;
    private boolean mTyping = false;
    private Handler mTypingHandler = new Handler();
    private static final int REQUEST_LOGIN = 0;
    private static final int TYPING_TIMER_LENGTH = 600;
    private Socket mSocket;
    private Boolean connectOrNot = false;
    private Boolean isConnected = true;
    private MainActivity activity;
    private String mUsername;
    private String mGroupId;

    //Login and connect to server
    public void connectToServer(){
        mGroupId = activity.group.getGroupName();
        mSocket.emit("new user", mUsername, mGroupId);
        mSocket.connect();
    }

    public void disconnectToServer() {
        mGroupId = null;
        mSocket.emit("disconnect");
        mSocket.disconnect();
    }

    public void userLocationChanged(double x, double y) {
        mSocket.emit("locationUpdate", x, y);
    }

    public void hostUpdate(String newHost) {
        mSocket.emit("changeNewHost", newHost);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mAdapter = new MessageAdapter(activity, mMessages);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chat, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mMessagesView = (RecyclerView) view.findViewById(R.id.messages);
        mMessagesView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mMessagesView.setAdapter(mAdapter);
        mInputMessageView = (EditText) view.findViewById(R.id.message_input);
        mInputMessageView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int id, KeyEvent event) {
                if (id == R.id.send || id == EditorInfo.IME_NULL) {
                    attemptSend();
                    return true;
                }
                return false;
            }
        });

        mInputMessageView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (null == mUsername) return;
                if (!mSocket.connected()) return;

                if (!mTyping) {
                    mTyping = true;
                    mSocket.emit("typing");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        ImageButton sendButton = (ImageButton) view.findViewById(R.id.send_button);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptSend();
            }
        });
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (MainActivity) getActivity();
        setHasOptionsMenu(true);
        ChatApplication app = new ChatApplication();
        mSocket = app.getSocket();
        //mSocket.on(Socket.EVENT_CONNECT, onConnect);
        //mSocket.on(Socket.EVENT_DISCONNECT, onDisconnect);
//        mSocket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
//        mSocket.on(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
        mSocket.on("new message", onNewMessage);
        mSocket.on("new user join group", onNewUserJoinGroup);
        mSocket.on("leaveMessage", onLeaveMessage);
        mSocket.on("someoneElseUpdateLocation", locationUpdate);
        mSocket.on("changeHostOnMySide", changeHostOnMyside);
//        mSocket.on("user joined", onUserJoined);
//        mSocket.on("user left", onUserLeft);
//        mSocket.on("typing", onTyping);
//        mSocket.on("stop typing", onStopTyping);
        mUsername = activity.username;
    }


    private void attemptSend() {
        if (null == mUsername) return;
        if (!mSocket.connected()) return;

        mTyping = false;

        String message = mInputMessageView.getText().toString().trim();
        if (TextUtils.isEmpty(message)) {
            mInputMessageView.requestFocus();
            return;
        }

        mInputMessageView.setText("");
        //addMessage(mUsername, message);

        // perform the sending message attempt.
        mSocket.emit("send message", message);
    }

    private void addMessage(String username, String message) {
        mMessages.add(new Message.Builder(Message.TYPE_MESSAGE)
                .username(username).message(message).build());
        mAdapter.notifyItemInserted(mMessages.size() - 1);
        scrollToBottom();
    }

    private void scrollToBottom() {
        mMessagesView.scrollToPosition(mAdapter.getItemCount() - 1);
    }

    private void removeTyping(String username) {
        for (int i = mMessages.size() - 1; i >= 0; i--) {
            Message message = mMessages.get(i);
            if (message.getType() == Message.TYPE_ACTION && message.getUsername().equals(username)) {
                mMessages.remove(i);
                mAdapter.notifyItemRemoved(i);
            }
        }
    }

    private Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(!isConnected) {
                        //  made some changes here, to check whether the
                        if(null!=mUsername && mGroupId != null) {
                            mSocket.emit("new user", mUsername, mGroupId, connectOrNot);
                            isConnected = connectOrNot;
                        }

                        //Toast.makeText(getActivity().getApplicationContext(), "Connect", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    };

    private Emitter.Listener onDisconnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    isConnected = false;
                    mSocket.emit("disconnect", mUsername, mGroupId);
                    // Toast.makeText(getActivity().getApplicationContext(), "Disconnect", Toast.LENGTH_LONG).show();
                }
            });
        }
    };

    private Emitter.Listener onNewUserJoinGroup = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String nick;
                    try {
                        nick = data.getString("nick");
                        activity.userJoined(nick);
                    } catch (JSONException e) {
                        return;
                    }
                }
            });
        }
    };

    private Emitter.Listener onLeaveMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String nick;
                    try {
                        nick = data.getString("nick");
                        activity.userLeft(nick);
                    } catch (JSONException e) {
                        return;
                    }
                }
            });
        }
    };

    private Emitter.Listener locationUpdate = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String nick;
                    double xCoor;
                    double yCoor;
                    try {
                        nick = data.getString("nick");
                        xCoor = data.getDouble("x");
                        yCoor = data.getDouble("y");
                        Location location = new Location("");
                        location.setLatitude(xCoor);
                        location.setLongitude(yCoor);
                        activity.userLocationChanged(nick, location);
                    } catch (JSONException e) {
                        return;
                    }
                }
            });
        }
    };

    private Emitter.Listener changeHostOnMyside = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String nick;
                    try {
                        nick = data.getString("hostName");
                        activity.userPromoted(nick);
                    } catch (JSONException e) {
                        return;
                    }
                }
            });
        }
    };

    private Emitter.Listener onNewMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String msg;
                    String nick;
                    try {
                        nick = data.getString("nick");
                        msg = data.getString("msg");
                    } catch (JSONException e) {
                        return;
                    }
                    addMessage(nick, msg);
                }
            });
        }
    };
}