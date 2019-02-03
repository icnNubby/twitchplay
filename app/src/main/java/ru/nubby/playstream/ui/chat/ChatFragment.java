package ru.nubby.playstream.ui.chat;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import ru.nubby.playstream.R;
import ru.nubby.playstream.model.ChatMessage;

public class ChatFragment extends Fragment implements ChatContract.View {
    private ChatContract.Presenter mPresenter;
    private RecyclerView mChatRecyclerview;
    private final int MESSAGE_CAPACITY = 100; // TODO get from prefs

    public static ChatFragment newInstance() {

        Bundle args = new Bundle();

        ChatFragment fragment = new ChatFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_chat, container, false);
        mChatRecyclerview = fragmentView.findViewById(R.id.chat_messages_recyclerview);
        mChatRecyclerview.setLayoutManager(new LinearLayoutManager(getActivity()));
        mChatRecyclerview.setAdapter(new ChatMessagesAdapter(new ArrayList<>()));
        setRetainInstance(true);
        return fragmentView;
    }

    @Override
    public void setPresenter(ChatContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public boolean hasPresenterAttached() {
        return mPresenter != null;
    }

    @Override
    public void onResume() {
        super.onResume();
        mPresenter.subscribe();
    }

    @Override
    public void onPause() {
        super.onPause();
        mPresenter.unsubscribe();
    }

    @Override
    public void addChatMessage(ChatMessage message) {
        ChatMessagesAdapter chatMessagesAdapter = (ChatMessagesAdapter) mChatRecyclerview.getAdapter();
        if (chatMessagesAdapter!= null) chatMessagesAdapter.addNewMessage(message);
    }

    private class ChatMessagesViewHolder extends RecyclerView.ViewHolder {
        private ChatMessage mChatMessage;
        private TextView mTextViewContents;

        public void bind(ChatMessage chatMessage) {
            mChatMessage = chatMessage;
            mTextViewContents.setText(mChatMessage.getUser() + ": " + mChatMessage.getMessage());
        }

        public ChatMessagesViewHolder(@NonNull View itemView) {
            super(itemView);
            mTextViewContents = itemView.findViewById(R.id.chat_message);
        }

    }

    private class ChatMessagesAdapter extends RecyclerView.Adapter<ChatMessagesViewHolder> {
        private List<ChatMessage> mChatMessages;

        public ChatMessagesAdapter(List<ChatMessage> chatMessages) {
            mChatMessages = chatMessages;
        }

        public void addNewMessage(ChatMessage message) {
            //TODO implement scrollability while pushing new elements to list
            while (mChatMessages.size() >= MESSAGE_CAPACITY) {
                mChatMessages.remove(0);
                notifyItemRangeRemoved(0,1);
            }
            mChatMessages.add(message);
            notifyItemInserted(mChatMessages.size());
            mChatRecyclerview.smoothScrollToPosition(mChatMessages.size());
        }

        @NonNull
        @Override
        public ChatMessagesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(getActivity()).inflate(R.layout.chat_element, parent, false);
            ChatMessagesViewHolder listViewHolder = new ChatMessagesViewHolder(view);
            return listViewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull ChatMessagesViewHolder holder, int position) {
            holder.bind(mChatMessages.get(position));
        }

        @Override
        public int getItemCount() {
            return mChatMessages.size();
        }
    }

}
