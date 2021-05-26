package co.example.chatapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class Adapter extends RecyclerView.Adapter <Adapter.MyViewHolder> {

    private List<String> chatMessages;

    //parent.getContext = contextdir.

    public Adapter(List<String> chatMessages) {
        this.chatMessages = chatMessages;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_list_row,parent,false);
        return new MyViewHolder(itemView);

        //-- buradaki itemview parametresi Myviewholderdaki sınıfa girecek.
        //-- bu da recyclerview holderdaki viewholdera paslayacak

    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        String chatMessage3  = chatMessages.get(position);
        holder.chatMessage2.setText(chatMessage3);

    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        public TextView chatMessage2;


        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            chatMessage2= itemView.findViewById(R.id.recycler_view_text_view);
        }
    }
}
