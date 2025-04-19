package com.example.calculator;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

    private List<String> historyItems = new ArrayList<>();
    private final OnHistoryItemClickListener listener;

    public interface OnHistoryItemClickListener {
        void onItemClick(String item);
    }

    public HistoryAdapter(OnHistoryItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_history, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        String historyItem = historyItems.get(position);
        holder.bind(historyItem, listener);
    }

    @Override
    public int getItemCount() {
        return historyItems.size();
    }

    public void setHistoryItems(List<String> items) {
        this.historyItems = new ArrayList<>(items);
        notifyDataSetChanged();
    }

    static class HistoryViewHolder extends RecyclerView.ViewHolder {
        private final TextView expressionTextView;
        private final TextView resultTextView;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            expressionTextView = itemView.findViewById(R.id.historyExpression);
            resultTextView = itemView.findViewById(R.id.historyResult);
        }

        public void bind(final String item, final OnHistoryItemClickListener listener) {
            // Parse the history item format "expression = result"
            if (item.contains("=")) {
                String[] parts = item.split("=", 2);
                String expression = parts[0].trim();
                String result = parts[1].trim();
                
                expressionTextView.setText(expression);
                resultTextView.setText("= " + result);
            } else {
                // Fallback if the format is unexpected
                expressionTextView.setText(item);
                resultTextView.setText("");
            }
            
            itemView.setOnClickListener(v -> listener.onItemClick(item));
        }
    }
}