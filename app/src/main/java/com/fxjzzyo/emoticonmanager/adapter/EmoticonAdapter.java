package com.fxjzzyo.emoticonmanager.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.fxjzzyo.emoticonmanager.R;
import com.fxjzzyo.emoticonmanager.bean.EmoticonBean;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by fanlulin on 2019-07-08.
 */
public class EmoticonAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final String TAG = "EmoticonAdapter";
    private Context mContext;
    private List<EmoticonBean> mEmoticonBeans;
    private boolean hasMore;
    private static final int NORMAL_TYPE = 0;
    private static final int FOOT_TYPE = 1;
    private boolean isFootHide;

    public EmoticonAdapter(Context context, List<EmoticonBean> emoticonBeans, boolean hasMore) {
        this.mContext = context;
        this.hasMore = hasMore;
        this.mEmoticonBeans = emoticonBeans;
    }

    class NormalHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.iv_emoticon_img)
        ImageView emoticonImage;
        @BindView(R.id.tv_emoticon_content)
        TextView tvEmoticonContent;
        @BindView(R.id.cv)
        CardView cardView;

        public NormalHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    class FootViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.tv_foot)
        TextView tvFoot;

        public FootViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == NORMAL_TYPE) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.emoticon_item, parent, false);
            return new NormalHolder(view);
        } else {
            View view = LayoutInflater.from(mContext).inflate(R.layout.foot_view, parent, false);
            return new FootViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof NormalHolder) {// 正常的条目
            NormalHolder normalHolder = (NormalHolder) holder;
            EmoticonBean emoticonBean = mEmoticonBeans.get(position);
            normalHolder.tvEmoticonContent.setText(emoticonBean.getEmoticonContent());
            RequestOptions options = new RequestOptions()
                    .placeholder(R.mipmap.default_img)// 图片加载出来前，显示的图片
                    .fallback(R.mipmap.default_img) // url为空的时候,显示的图片
                    .error(R.mipmap.default_img);// 图片加载失败后，显示的图片
            Glide.with(mContext).load(emoticonBean.getEmoticonImgURI()).apply(options)
                    .into(normalHolder.emoticonImage);

            if (onItemlickListener != null) {
                normalHolder.cardView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        onItemlickListener.onItemClick(view, holder.getLayoutPosition());
                    }
                });
            }

            if (onItemLongClickListener != null) {
                normalHolder.cardView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        onItemLongClickListener.onItemLongClick(view, holder.getLayoutPosition());
                        return true;
                    }
                });
            }
        } else {// FootView 条目
            FootViewHolder footViewHolder = (FootViewHolder) holder;
            footViewHolder.tvFoot.setVisibility(View.VISIBLE);
            if (hasMore) {
                isFootHide = false;
                if (mEmoticonBeans.size() > 0) {
                    footViewHolder.tvFoot.setText("上拉加载更多哦");
                }
            } else {
                isFootHide = true;
                if (mEmoticonBeans.size() > 0) {
                    footViewHolder.tvFoot.setText("没有更多数据了~");
                }
            }

        }

    }

    /**
     * 底部 Footview 横跨整行
     *
     * @param recyclerView
     */
    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);

        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if (layoutManager != null && !(layoutManager instanceof GridLayoutManager)) return;

        GridLayoutManager gridLayoutManager = (GridLayoutManager) layoutManager;
        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (mEmoticonBeans.size() == 0) {
                    return gridLayoutManager.getSpanCount();
                }

                if (getItemViewType(position) == FOOT_TYPE) {
                    return gridLayoutManager.getSpanCount();
                }
                return 1;
            }
        });

    }

    public int getRealItemCount() {
        return mEmoticonBeans.size();
    }


    @Override
    public int getItemCount() {
        return mEmoticonBeans.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == getItemCount() - 1) {
            return FOOT_TYPE;
        } else {
            return NORMAL_TYPE;
        }
    }


    /**
     * 更新列表
     * 在原有的数据之上增加新数据
     * @param newDatas
     * @param hasMore
     */
    public void updateList(List<EmoticonBean> newDatas, boolean hasMore) {
        if (newDatas != null) {
            mEmoticonBeans.addAll(newDatas);
        }
        this.hasMore = hasMore;
        notifyDataSetChanged();
    }

    /**
     * 清空列表
     */
    public void clearList(){
        mEmoticonBeans.clear();
        notifyDataSetChanged();
    }

    public void addItem(EmoticonBean emoticonBean, int position) {
        mEmoticonBeans.add(position, emoticonBean);
        notifyItemInserted(position);
    }

    public void updateItem(EmoticonBean emoticonBean, int position) {
        mEmoticonBeans.get(position).setEmoticonContent(emoticonBean.getEmoticonContent());
        notifyItemChanged(position);
    }

    public void removeItem(int position) {
        mEmoticonBeans.remove(position);
        notifyItemRemoved(position);
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

    public boolean isFootHide() {
        return isFootHide;
    }
}
