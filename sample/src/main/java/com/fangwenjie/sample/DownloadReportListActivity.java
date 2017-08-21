package com.fangwenjie.sample;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by fangwenjie on 2017/6/3.
 */

public class DownloadReportListActivity extends AppCompatActivity {

    @BindView(R.id.activity_download_report_list_content)
    RecyclerView content;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_report_list);
        ButterKnife.bind(this);

        ContentAdapter adapter = new ContentAdapter();
        content.setAdapter(adapter);
        content.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        init();
    }

    private void init() {

    }

    public static class ContentAdapter extends RecyclerView.Adapter<ContentViewHolder> {

        private List<ContentData> mDataset = new ArrayList<>();

        public void setDataset(List<ContentData> datas) {
            if (datas != null) {
                mDataset.clear();
                mDataset.addAll(datas);
                notifyDataSetChanged();
            }
        }


        @Override
        public ContentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ContentViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_content, parent, false));
        }

        @Override
        public void onBindViewHolder(ContentViewHolder holder, int position) {
            ContentData data = mDataset.get(position);
            holder.onBindData(data);
        }

        @Override
        public int getItemCount() {
            return mDataset.size();
        }
    }

    public static class ContentViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.item_content_name)
        TextView contentName;

        public ContentViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void onBindData(ContentData data) {
            contentName.setText(data.contentName);
        }
    }

    public static class ContentData {
        public String contentName;
    }


}
