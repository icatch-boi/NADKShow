package com.icatchtek.nadk.show.timeline;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.icatchtek.nadk.show.R;

import java.util.List;

/**
 * Created by sha.liu on 2023/9/4.
 */
public class StringAdapter extends RecyclerView.Adapter<StringAdapter.ViewHolder> {

    private List<String> lists;
    private OnItemClickListener mOnItemClickListener;

    private int selectPosition = 5;
    private Context context;

    public StringAdapter(List<String> lists, Context context) {
        this.lists = lists;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_text_rv, parent, false);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /**
                 * 第五步，使用getTag方法获取点击的item的position
                 */
                if (mOnItemClickListener != null) {
                    //参数v即创建的View视图，依次生成的item，这里是获取Tag，设置Tag需要在绑定的ViewHolder方法中处理Tag
                    selectPosition = (int)v.getTag();
                    mOnItemClickListener.onItemClick(v, (int) v.getTag());
                    notifyDataSetChanged();

                }
            }
        });
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.tvText.setText(lists.get(position));
        holder.itemView.setTag(position);
        if (selectPosition == position) {
            holder.tvText.setTextColor(context.getColor(R.color.timelineTextSelect));
            holder.tvText.setTypeface(Typeface.DEFAULT_BOLD);
        } else {
            holder.tvText.setTextColor(context.getColor(R.color.timelineTextUnSelect));
            holder.tvText.setTypeface(Typeface.DEFAULT);
        }

    }

    @Override
    public int getItemCount() {
        return lists.size();
    }

    /**
     * 第二步，为Activity提供设置OnItemClickListener的接口
     */
    public void setOnItemClickListener(OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    public void setSelectPosition(int position) {
        selectPosition = position;
        notifyDataSetChanged();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView tvText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvText = itemView.findViewById(R.id.tv_text);
        }
    }

    /**
     * 在Activity中设置item点击事件的方法第一步：
     * 第一步，定义接口,在activity里面使用setOnItemClickListener方法并创建此接口的对象、实现其方法
     */
    public static interface OnItemClickListener {
        //接口方法里面的参数，可以传递任意你想回调的数据，不止View 和 Item 的位置position
        void onItemClick(View view, int position);
    }

}
