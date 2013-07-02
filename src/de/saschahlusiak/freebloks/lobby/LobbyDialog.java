package de.saschahlusiak.freebloks.lobby;

import de.saschahlusiak.freebloks.R;
import de.saschahlusiak.freebloks.controller.SpielClient;
import de.saschahlusiak.freebloks.controller.SpielClientInterface;
import de.saschahlusiak.freebloks.model.Spiel;
import de.saschahlusiak.freebloks.model.Stone;
import de.saschahlusiak.freebloks.model.Turn;
import de.saschahlusiak.freebloks.network.NET_CHAT;
import de.saschahlusiak.freebloks.network.NET_SERVER_STATUS;
import de.saschahlusiak.freebloks.network.NET_SET_STONE;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethod;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class LobbyDialog extends Dialog implements SpielClientInterface {
	SpielClient spiel;
	Handler handler = new Handler();
	ListView chatList;
	ArrayAdapter<ChatEntry> adapter;
	NET_SERVER_STATUS lastStatus = null;

	public LobbyDialog(Context context,
			OnCancelListener cancelListener) {
		super(context, true, cancelListener);
		setContentView(R.layout.lobby_dialog);

		getWindow().setLayout(LayoutParams.FILL_PARENT,	LayoutParams.WRAP_CONTENT);
		setTitle(R.string.lobby_waiting_for_players);
		
		findViewById(R.id.startButton).setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				LobbyDialog.this.spiel.request_start();
			}
		});
		findViewById(R.id.chatButton).setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				sendChat();
			}
		});
		((EditText)findViewById(R.id.chatText)).setOnEditorActionListener(new OnEditorActionListener() {			
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_NULL) {
					sendChat();
					return true;
				}
				return false;
			}
		});

		adapter = new ChatListAdapter(getContext());
		chatList = (ListView)findViewById(R.id.chatList);
		chatList.setAdapter(adapter);
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		adapter.clear();
	}
	
	public void setSpiel(SpielClient spiel) {
		this.spiel = spiel;
		if (spiel != null)
			spiel.addClientInterface(this);
	}
	
	void sendChat() {
		EditText edit = (EditText)findViewById(R.id.chatText);
		String s = edit.getText().toString();
		if (s.length() < 1)
			return;
		
		NET_CHAT chat = new NET_CHAT(s + "\n");
		LobbyDialog.this.spiel.send(chat);
		edit.setText("");
	}
	
	@Override
	protected void onStop() {
		if (spiel != null)
			spiel.removeClientInterface(this);
		super.onStop();
	}

	@Override
	public void newCurrentPlayer(int player) {
		
	}

	@Override
	public void stoneWillBeSet(NET_SET_STONE s) {
		
	}
	
	@Override
	public void stoneHasBeenSet(NET_SET_STONE s) {
		
	}

	@Override
	public void hintReceived(NET_SET_STONE s) {
		
	}

	@Override
	public void gameFinished() {
		
	}

	@Override
	public void chatReceived(final NET_CHAT c) {
		chatList.post(new Runnable() {
			@Override
			public void run() {
				/* TODO: if there is a client without a name, try to get the name of the
				 * first assigned color rather than the client name
				 * 
				 * i.e. "Blue" instead of "Client 0"
				 */
				ChatEntry e = new ChatEntry(c.client, c.text, getClientName(c.client));
				adapter.add(e);
			}
		});
	}
	
	String getClientName(int client) {
		if (lastStatus == null)
			return getContext().getString(R.string.client_d, client);
		return lastStatus.getClientName(getContext().getResources(), client);
	}

	@Override
	public void gameStarted() {
		dismiss();
	}

	@Override
	public void stoneUndone(Stone s, Turn t) {
		
	}

	@Override
	public void serverStatus(final NET_SERVER_STATUS status) {
		handler.post(new Runnable() {
			@Override
			public void run() {
				lastStatus = status;
				
				((TextView)findViewById(R.id.server)).setText("" + spiel.getLastHost());
				((TextView)findViewById(R.id.clients)).setText(getContext().getString(R.string.connected_clients, status.clients));
				
				TextView v = (TextView)findViewById(R.id.your_color);
				String colorNames[] = getContext().getResources().getStringArray(R.array.color_names);
				int colors[] = { Color.BLUE, Color.YELLOW, Color.RED , Color.GREEN };

				v.setText(R.string.lobby_no_color);
				v.setTextColor(Color.WHITE);
				for (int i = 0; i < Spiel.PLAYER_MAX; i++) if (spiel.spiel.is_local_player(i)) {
					v.setText(colorNames[i]);
					v.setTextColor(colors[i]);
				}
			}
		});
	}

	@Override
	public void onConnected(Spiel spiel) {
		
	}

	@Override
	public void onDisconnected(Spiel spiel) {
		dismiss();
	}
}
