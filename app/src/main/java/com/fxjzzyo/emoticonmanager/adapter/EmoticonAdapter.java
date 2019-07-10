package com.fxjzzyo.emoticonmanager.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.fxjzzyo.emoticonmanager.R;
import com.fxjzzyo.emoticonmanager.bean.EmoticonBean;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by fanlulin on 2019-07-08.
 */
public class EmoticonAdapter extends RecyclerView.Adapter<EmoticonAdapter.ViewHolder> {
    public static final String TAG = "EmoticonAdapter";
    private Context mContext;
    private List<EmoticonBean> mEmoticonBeans;

    public EmoticonAdapter(List<EmoticonBean> emoticonBeans) {
        this.mEmoticonBeans = emoticonBeans;
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.iv_emoticon_img)
        ImageView emoticonImage;
        @BindView(R.id.tv_emoticon_content)
        TextView tvEmoticonContent;
        @BindView(R.id.cv)
        CardView cardView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (mContext == null) {
            mContext = parent.getContext();
        }
        View view = LayoutInflater.from(mContext).inflate(R.layout.emoticon_item, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        EmoticonBean emoticonBean = mEmoticonBeans.get(position);
        holder.tvEmoticonContent.setText(emoticonBean.getEmoticonContent());

        Glide.with(mContext).load(emoticonBean.getEmoticonImgURI()).into(holder.emoticonImage);

        if (onItemlickListener != null) {
            holder.cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onItemlickListener.onItemClick(view, holder.getLayoutPosition());
                }
            });
        }

        if (onItemLongClickListener != null) {
            holder.cardView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    onItemLongClickListener.onItemLongClick(view, holder.getLayoutPosition());
                    return true;
                }
            });
        }


    }


    @Override
    public int getItemCount() {
        return mEmoticonBeans.size();
    }


    public interface OnItemLongClickListener {
        void onItemLongClick(View view, int position);
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    private OnItemLongClickListener onItemLongClickListener;
    private OnItemClickListener onItemlickListener;

    public void setOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener) {
        this.onItemLongClickListener = onItemLongClickListener;
    }

    public void setOnItemlickListener(OnItemClickListener onItemlickListener) {
        this.onItemlickListener = onItemlickListener;
    }
}
