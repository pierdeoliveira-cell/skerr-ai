package com.skerr.ai.adapter;

import android.content.Context;
import android.view.*;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerView.widget.RecyclerView;
import com.skerr.ai.R;
import com.skerr.ai.model.Conversation;
import java.util.List;

public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.VH> {

    public interface OnConversationClick { void onClick(Conversation conv); }

    private final Context context;
    private List<Conversation> list;
    private final OnConversationClick listener;

    public ConversationAdapter(Context ctx, List<Conversation> list, OnConversationClick listener) {
        this.context = ctx;
        this.list = list;
        this.listener = listener;
    }

    public void updateList(List<Conversation> newList) {
        this.list = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_conversation, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int pos) {
        Conversation conv = list.get(pos);
        holder.tvTitle.setText(conv.getTitle());
        holder.tvCount.setText(conv.getMessages().size() + " msgs");
        holder.itemView.setOnClickListener(v -> listener.onClick(conv));
    }

    @Override
    public int getItemCount() { return list.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvTitle, tvCount;
        VH(View v) {
            super(v);
            tvTitle = v.findViewById(R.id.tv_title);
            tvCount = v.findViewById(R.id.tv_count);
        }
    }
}
