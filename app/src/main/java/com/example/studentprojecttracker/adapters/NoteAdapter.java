package com.example.studentprojecttracker.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studentprojecttracker.R;
import com.example.studentprojecttracker.models.Note;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {

    private final Context context;
    private final List<Note> notes;
    private NoteActionListener listener;

    public interface NoteActionListener {
        void onEdit(Note note);
        void onDelete(Note note);
    }

    public void setNoteActionListener(NoteActionListener listener) {
        this.listener = listener;
    }

    public NoteAdapter(Context context, List<Note> notes) {
        this.context = context;
        this.notes = notes;
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_note, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        Note note = notes.get(position);

        holder.textAuthor.setText(note.getAuthorName());
        holder.textNote.setText(note.getText());

        // Format timestamp
        SimpleDateFormat sdf = new SimpleDateFormat("MMM d, h:mm a", Locale.getDefault());
        holder.textTime.setText(sdf.format(note.getTimestamp()));

        // Handle three-dot menu
        holder.noteOptions.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(context, holder.noteOptions);
            popup.getMenuInflater().inflate(R.menu.note_menu, popup.getMenu());
            popup.setOnMenuItemClickListener(item -> {
                if (listener != null) {
                    int id = item.getItemId();
                    if (id == R.id.action_edit) {
                        listener.onEdit(note);
                        return true;
                    } else if (id == R.id.action_delete) {
                        listener.onDelete(note);
                        return true;
                    }
                }
                return false;
            });
            popup.show();
        });
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    static class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView textAuthor, textNote, textTime;
        ImageView noteOptions;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            textAuthor = itemView.findViewById(R.id.textAuthor);
            textNote = itemView.findViewById(R.id.textNote);
            textTime = itemView.findViewById(R.id.textTime);
            noteOptions = itemView.findViewById(R.id.noteOptions);
        }
    }
}
