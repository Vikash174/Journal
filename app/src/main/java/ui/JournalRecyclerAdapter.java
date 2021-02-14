package ui;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.selfevaluation.R;
import com.squareup.picasso.Picasso;

import java.util.List;

import model.Journal;

public class JournalRecyclerAdapter extends RecyclerView.Adapter<JournalRecyclerAdapter.ViewHolder> {
    private Context context;
    private final List<Journal> journalList;

    public JournalRecyclerAdapter(Context context, List<Journal> journalList) {
        this.context = context;
        this.journalList = journalList;
    }

    @NonNull
    @Override
    public JournalRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.journal_row,parent,false);

        return new ViewHolder(view,context);
    }

    @Override
    public void onBindViewHolder(@NonNull JournalRecyclerAdapter.ViewHolder holder, int position) {
        Journal journal = journalList.get(position);
        String imageUrl;

        holder.title.setText(journal.getTitle());
        holder.thoughts.setText(journal.getThought());
        holder.name.setText(journal.getUserName());
        String timeAgo = (String) DateUtils.getRelativeTimeSpanString(journal.getTimeAdded().getSeconds()* 1000);

        holder.dateAdded.setText(timeAgo);
        imageUrl = journal.getImageUrl();
       /*
         use picasso to load the image
        */

        Picasso.get()
                .load(imageUrl)
                .placeholder(R.drawable.imageimage)
                .fit()
                .into(holder.imageView);

    }

    @Override
    public int getItemCount() {
        return journalList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public TextView title,thoughts,dateAdded,name;
        public ImageView imageView;
        String userId ;
        String username;
        public ViewHolder(@NonNull View itemView, Context ctx) {
            super(itemView);
            context = ctx;
            title = itemView.findViewById(R.id.journalTitleList);
            thoughts = itemView.findViewById(R.id.journal_thoughts_list);
            dateAdded = itemView.findViewById(R.id.journal_timestamp_list);
            imageView = itemView.findViewById(R.id.journalImageList);
            name = itemView.findViewById(R.id.journalRowUsername);

        }
    }
}
