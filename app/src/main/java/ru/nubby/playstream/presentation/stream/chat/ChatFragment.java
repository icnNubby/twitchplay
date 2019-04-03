package ru.nubby.playstream.presentation.stream.chat;

import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import ru.nubby.playstream.R;
import ru.nubby.playstream.model.ChatMessage;
import ru.nubby.playstream.model.Stream;
import ru.nubby.playstream.presentation.base.BaseFragment;
import ru.nubby.playstream.presentation.base.PresenterFactory;

public class ChatFragment extends BaseFragment implements ChatContract.View {
    private final int MESSAGE_CAPACITY = 100; // TODO get from prefs

    @Inject
    public PresenterFactory mPresenterFactory;

    private ChatContract.Presenter mPresenter;
    private RecyclerView mChatRecyclerview;
    private ProgressBar mProgressBar;

    private Stream mCurrentStream;

    @Inject
    public ChatFragment() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPresenter = ViewModelProviders.of(this, mPresenterFactory).get(ChatPresenter.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_chat, container, false);
        mChatRecyclerview = fragmentView.findViewById(R.id.chat_messages_recyclerview);
        mChatRecyclerview.setLayoutManager(new LinearLayoutManager(getActivity()));
        mChatRecyclerview.setAdapter(new ChatMessagesAdapter(new ArrayList<>()));
        mProgressBar = fragmentView.findViewById(R.id.stream_buffer_chat_progressbar);
        setRetainInstance(true);
        return fragmentView;
    }

    @Override
    public void onResume() {
        super.onResume();
        mPresenter.subscribe(this, this.getLifecycle(), mCurrentStream);
    }

    @Override
    public void onPause() {
        super.onPause();
        mPresenter.unsubscribe();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mChatRecyclerview = null;
        mProgressBar = null;
    }

    @Override
    public boolean hasPresenterAttached() {
        return mPresenter != null;
    }

    @Override
    public void addChatMessage(ChatMessage message) {
        ChatMessagesAdapter chatMessagesAdapter = (ChatMessagesAdapter) mChatRecyclerview.getAdapter();
        if (chatMessagesAdapter != null) chatMessagesAdapter.addNewMessage(message);
    }

    @Override
    public void displayInfoMessage(InfoMessage message) {
        ChatMessage chatMessage = new ChatMessage(getString(R.string.system_user),
                getResources().getStringArray(R.array.chat_info_messages)[message.ordinal()],
                "#" + Integer.toHexString(getResources().getColor(R.color.colorAccent)));

        ChatMessagesAdapter chatMessagesAdapter = (ChatMessagesAdapter) mChatRecyclerview.getAdapter();
        if (chatMessagesAdapter != null) chatMessagesAdapter.addNewMessage(chatMessage);
    }

    @Override
    public void displayLoading(boolean loadingState) {
        mProgressBar.setIndeterminate(loadingState);
        if (loadingState) {
            mProgressBar.setVisibility(View.VISIBLE);
        } else {
            mProgressBar.setVisibility(View.GONE);
        }
    }

    public void setCurrentStream(Stream currentStream) {
        mCurrentStream = currentStream;
    }

    private class ChatMessagesViewHolder extends RecyclerView.ViewHolder {
        private ChatMessage mChatMessage;
        private TextView mTextViewContents;

        public void bind(ChatMessage chatMessage) {
            if (!chatMessage.isEmpty()) {
                mChatMessage = chatMessage;
                SpannableStringBuilder builder = new SpannableStringBuilder();
                SpannableString user = new SpannableString(mChatMessage.getUser());
                if (!mChatMessage.getColor().isEmpty())
                    user.setSpan(new ForegroundColorSpan(Color.parseColor(mChatMessage.getColor())),
                            0,
                            user.length(),
                            0);
                builder.append(user);
                builder.append(": ");
                builder.append(mChatMessage.getMessage());
                mTextViewContents.setText(builder, TextView.BufferType.SPANNABLE);
            }
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
                notifyItemRangeRemoved(0, 1);
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
