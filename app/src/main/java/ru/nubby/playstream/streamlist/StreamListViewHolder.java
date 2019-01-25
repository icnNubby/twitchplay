package ru.nubby.playstream.streamlist;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import ru.nubby.playstream.R;
import ru.nubby.playstream.model.Stream;

public class StreamListViewHolder extends RecyclerView.ViewHolder {
    private Stream mStream;
    private TextView mTextViewStreamDescription;

    public void bind(Stream stream) {
        mStream = stream;
        mTextViewStreamDescription.setText(stream.getStreamer_name() + "___"  + stream.getTitle());
    }

    public StreamListViewHolder(@NonNull View itemView) {
        super(itemView);
        mTextViewStreamDescription = itemView.findViewById(R.id.stream_description);
    }
}
