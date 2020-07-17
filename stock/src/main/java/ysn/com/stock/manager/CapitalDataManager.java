package ysn.com.stock.manager;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import ysn.com.stock.bean.Extremum;
import ysn.com.stock.bean.ICapitalData;

/**
 * @Author yangsanning
 * @ClassName CapitalDataManager
 * @Description {@link ysn.com.stock.view.CapitalView} 资金数据管理器
 * @Date 2020/7/15
 */
public class CapitalDataManager {

    private Extremum extremum = new Extremum();
    private Extremum priceExtremum = new Extremum();
    private Extremum inFlowExtremum = new Extremum();
    private List<? extends ICapitalData> dataList = new ArrayList<>();

    /**
     * 获取最大值
     */
    public float getMaximum() {
        return extremum.getMaximum();
    }

    /**
     * 获取最小值
     */
    public float getMinimum() {
        return extremum.getMinimum();
    }

    /**
     * 获取极差
     */
    public float getPeek() {
        return extremum.getPeek();
    }

    /**
     * 获取价格极值实体
     */
    public Extremum getPriceExtremum() {
        return priceExtremum;
    }

    /**
     * 获取 InFlow 极值实体
     */
    public Extremum getInFlowExtremum() {
        return inFlowExtremum;
    }

    /**
     * 是否为空
     */
    public boolean isEmpty() {
        return dataList.isEmpty();
    }

    /**
     * 数据长度
     */
    public int size() {
        return dataList.size();
    }

    /**
     * 根据 position 获取价格
     */
    public float getPrice(int position) {
        return dataList.get(position).getPrice();
    }

    /**
     * 根据 position 获取总资金净流入
     */
    public float getFinanceInFlow(int position) {
        return dataList.get(position).getFinanceInFlow();
    }

    /**
     * 根据 position 获取主力净流入
     */
    public float getMainInFlow(int position) {
        return dataList.get(position).getMainInFlow();
    }

    /**
     * 根据 position 获取散户净流入
     */
    public float getRetailInFlow(int position) {
        return dataList.get(position).getRetailInFlow();
    }

    /**
     * 设置数据
     */
    public <T extends ICapitalData> void setNewData(@NonNull List<T> dataList) {
        this.dataList = dataList;
        int size = dataList.size();

        if (size > 0) {
            extremum.init(dataList.get(0).getPrice());
            priceExtremum.init(dataList.get(0).getPrice());
            inFlowExtremum.init(dataList.get(0).getFinanceInFlow());
            for (T data : dataList) {
                float price = data.getPrice();
                float financeInFlow = data.getFinanceInFlow();
                float mainInFlow = data.getMainInFlow();
                float retailInFlow = data.getRetailInFlow();
                extremum.convert(financeInFlow, mainInFlow, price, retailInFlow);
                priceExtremum.convert(price);
                inFlowExtremum.convert(financeInFlow, mainInFlow, retailInFlow);
            }
            extremum.calculatePeek();
            priceExtremum.calculatePeek();
            inFlowExtremum.calculatePeek();
        } else {
            extremum.reset();
        }
    }
}
