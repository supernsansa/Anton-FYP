package com.bsl4kids.antonsfyp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.Collections;
import java.util.List;

public class WordPreviewAdapter extends RecyclerView.Adapter {

    private Context context;
    private LayoutInflater inflater;
    List<WordPreview> data= Collections.emptyList();
    WordPreview current;
    int currentPos=0;
    private OnItemClickListener clickListener;

    // create constructor to initialize context and data sent from MainActivity
    public WordPreviewAdapter(Context context, List<WordPreview> data){
        this.context=context;
        inflater= LayoutInflater.from(context);
        this.data=data;
    }

    // Inflate the layout when ViewHolder created
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view=inflater.inflate(R.layout.word_preview_container, parent,false);
        MyHolder holder=new MyHolder(view);
        return holder;
    }

    // Bind data
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        // Get current position of item in recyclerview to bind data and assign values from list
        MyHolder myHolder = (MyHolder) holder;
        WordPreview current = data.get(position);
        myHolder.textName.setText(current.getName());
        myHolder.textDefinition.setText(current.getDefinition());

    }

    // return total item from List
    @Override
    public int getItemCount() {
        return data.size();
    }

    public void setClickListener(OnItemClickListener itemClickListener) {
        this.clickListener = itemClickListener;
    }

    class MyHolder extends RecyclerView.ViewHolder implements  View.OnClickListener{

        TextView textName;
        TextView textDefinition;

        // create constructor to get widget reference
        public MyHolder(View itemView) {
            super(itemView);
            textName= (TextView) itemView.findViewById(R.id.textName);
            textDefinition = (TextView) itemView.findViewById(R.id.textDefinition);
            itemView.setOnClickListener(this); // bind the listener
        }

        @Override
        public void onClick(View view) {
            clickListener.onClick(view, getPosition()); // call the onClick in the OnItemClickListener
        }
    }

}
