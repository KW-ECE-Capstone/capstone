package com.example.crawling_sample;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.ArrayList;


public class UtubeAdapter extends RecyclerView.Adapter<UtubeAdapter.UtubeViewHolder> {

    ArrayList<SearchData> mList;
    Context context;

    public UtubeAdapter(Context context, ArrayList<SearchData> list) {
        this.context = context;
        this.mList = list;
    }

    public class UtubeViewHolder extends RecyclerView.ViewHolder{

        ImageView titleImage;
        TextView titleText;
        TextView dateText;



        public UtubeViewHolder(@NonNull View itemView) {
            super(itemView);

            this.titleImage=itemView.findViewById(R.id.titleImage);
            this.titleText=itemView.findViewById(R.id.titleText);
            this.dateText=itemView.findViewById(R.id.dateText);



            //유튜브 영상을 클릭하면 재생이 되는 액티비티로 이동
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position=getAdapterPosition();
                    Intent intent = new Intent(context, UtubePlay.class);
                    intent.putExtra("id", mList.get(position).getVideoId());
                    context.startActivity(intent);
                }
            });

        }
    }





    @NonNull
    @Override
    public UtubeAdapter.UtubeViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {


        //item_utube xml파일을 객체화 시킨다.
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_utube, viewGroup, false);
        UtubeAdapter.UtubeViewHolder holder = new UtubeAdapter.UtubeViewHolder(view);

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull UtubeAdapter.UtubeViewHolder viewholder, int position) {


        //영상제목 세팅
        viewholder.titleText.setText(mList.get(position).getTitle());
        //날짜 세팅
        viewholder.dateText.setText(mList.get(position).getPublishedAt());


        //이미지를 넣어주기 위해 이미지url을 가져온다.
        String imageUrl = mList.get(position).getImageUrl();
        //영상 썸네일 세팅
        Glide.with(viewholder.titleImage)
                .load(imageUrl)
                .into(viewholder.titleImage);


    }

    @Override
    public int getItemCount() {
        return (null != mList ? mList.size() : 0);
    }
}



