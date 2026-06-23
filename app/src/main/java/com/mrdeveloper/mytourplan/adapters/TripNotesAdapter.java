package com.mrdeveloper.mytourplan.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mrdeveloper.mytourplan.R;
import com.mrdeveloper.mytourplan.models.TripNote;

import java.util.List;

public class TripNotesAdapter extends RecyclerView.Adapter<TripNotesAdapter.NoteViewHolder> {

    public interface OnNoteActionListener {
        void onEditNote(TripNote note);
        void onDeleteNote(TripNote note);
    }

    private List<TripNote> notes;
    private final OnNoteActionListener listener;

    public TripNotesAdapter(List<TripNote> notes, OnNoteActionListener listener) {
        this.notes = notes;
        this.listener = listener;
    }

    public void setNotes(List<TripNote> notes) {
        this.notes = notes;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_trip_note, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        TripNote note = notes.get(position);
        holder.tvNoteTitle.setText(note.getTitle());
        holder.tvNoteContent.setText(note.getContent());
        
        String dateText = note.getCreatedAt();
        if (dateText != null && dateText.length() > 10) {
            // Simply format date format: YYYY-MM-DD
            dateText = dateText.substring(0, 10);
        }
        holder.tvNoteDate.setText(dateText != null ? dateText : "");

        holder.ivEditNote.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditNote(note);
            }
        });

        holder.ivDeleteNote.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteNote(note);
            }
        });
    }

    @Override
    public int getItemCount() {
        return notes != null ? notes.size() : 0;
    }

    static class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView tvNoteTitle, tvNoteContent, tvNoteDate;
        ImageView ivEditNote, ivDeleteNote;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNoteTitle = itemView.findViewById(R.id.tvNoteTitle);
            tvNoteContent = itemView.findViewById(R.id.tvNoteContent);
            tvNoteDate = itemView.findViewById(R.id.tvNoteDate);
            ivEditNote = itemView.findViewById(R.id.ivEditNote);
            ivDeleteNote = itemView.findViewById(R.id.ivDeleteNote);
        }
    }
}
