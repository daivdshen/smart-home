package com.intelliving.app;

import android.content.Context;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.intelliving.app.R;
import com.intelliving.app.utils.DataItem;
import com.intelliving.app.utils.Utils;
import com.comelitgroup.module.api.CGModule;

import java.util.ArrayList;

/**
 * Created by simone.mutti on 01/11/17.
 */

public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.ViewHolder>{

    private static final String TAG = "HomeAdapter";
    private Context mContext;

    private ArrayList<DataItem> mDataset = new ArrayList<>();

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private ConstraintLayout mLayout;
        private TextView mTextTitleView;
        private TextView mTextDescriptionView;
        private TextView mTextIdView;
        private ImageView mIcon;

        public View view;
        public ViewHolder(View v) {
            super(v);
            view = v;

            mLayout = v.findViewById(R.id.elementLayout);
            mTextTitleView = v.findViewById(R.id.layout_item_title);
            mTextDescriptionView = v.findViewById(R.id.layout_item_description);
            mTextIdView = v.findViewById(R.id.layout_item_id);
            mIcon = v.findViewById(R.id.icon);
        }
    }

    public HomeAdapter(Context context, ArrayList<DataItem> dataset) {
        mContext = context;
        mDataset.clear();
        mDataset.addAll(dataset);
    }

    @Override
    public HomeAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_item, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final DataItem item = mDataset.get(position);

        holder.mTextTitleView.setText(item.getName());
        holder.mTextDescriptionView.setText(item.getDescription());
        holder.mTextIdView.setText(item.getId());

        int resId = Utils.getPreviewForElement(item.getId(), item.getType());
        Glide.with(holder.view).load(resId).into(holder.mIcon);

        holder.mLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String id = holder.mTextIdView.getText().toString();
                Log.i(TAG, "perform action against: " + holder.mTextTitleView.getText().toString());
                CGModule.getInstance(mContext).call(id);
            }
        });

    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}
