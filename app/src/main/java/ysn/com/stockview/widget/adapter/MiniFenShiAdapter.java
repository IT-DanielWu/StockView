package ysn.com.stockview.widget.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import ysn.com.stock.view.MiniFenShiView;
import ysn.com.stockview.R;
import ysn.com.stockview.bean.FenShiTime;

/**
 * @Author yangsanning
 * @ClassName MiniFenShiAdapter
 * @Description 一句话概括作用
 * @Date 2019/6/13
 * @History 2019/6/13 author: description:
 */
public class MiniFenShiAdapter extends RecyclerView.Adapter<MiniFenShiAdapter.MiniFenShiHolder> {

    private List<FenShiTime> data;
    private int size;
    private LayoutInflater inflate;

    public MiniFenShiAdapter(List<FenShiTime> data, Context context) {
        this.data = data;
        size = this.data.size();
        inflate = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public MiniFenShiHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflate.inflate(R.layout.item_mini_fen_shi, parent, false);
        return new MiniFenShiHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MiniFenShiHolder holder, int position) {
        FenShiTime fenShiTime = data.get(position % size);
        holder.codeTextView.setText(fenShiTime.getCode());
        holder.miniFenShiView.setNewData(fenShiTime);
        holder.itemView.setOnClickListener(view -> {
            Toast.makeText(view.getContext(), fenShiTime.getCode(), Toast.LENGTH_SHORT).show();
            Log.d("test", fenShiTime.getCode());
        });
    }

    @Override
    public int getItemCount() {
        return Integer.MAX_VALUE;
    }

    static class MiniFenShiHolder extends RecyclerView.ViewHolder {
        MiniFenShiView miniFenShiView;
        TextView codeTextView;

        MiniFenShiHolder(View view) {
            super(view);
            miniFenShiView = view.findViewById(R.id.mini_fen_shi_item_view);
            codeTextView = view.findViewById(R.id.mini_fen_shi_item_code);
        }
    }
}
