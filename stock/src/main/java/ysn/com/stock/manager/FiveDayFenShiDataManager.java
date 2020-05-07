package ysn.com.stock.manager;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import ysn.com.stock.bean.IFenShi;
import ysn.com.stock.utils.NumberUtils;

/**
 * @Author yangsanning
 * @ClassName FiveDayFenShiDataManager
 * @Description 五日分时数据管理器
 * @Date 2020/5/7
 */
public class FiveDayFenShiDataManager {

    private DecimalFormat decimalFormat;
    public FenShiDataManager dataManager1;
    public FenShiDataManager dataManager2;
    public FenShiDataManager dataManager3;
    public FenShiDataManager dataManager4;
    public FenShiDataManager dataManager5;

    /**
     * 昨收(中间坐标值)
     */
    public float lastClose = 0.0f;

    /**
     * 最大价格(左上角)
     */
    public float maxPrice = 0.0f;

    /**
     * 最小价格(左下角)
     */
    public float minPrice = 0.0f;

    /**
     * 百分比(右侧百分比坐标值)
     */
    public String percent = " 100%";

    /**
     * 当前交易量
     */
    public List<Float> volumeList = new ArrayList<>();

    /**
     * 最大交易量
     */
    public float maxVolume;

    /**
     * 最大交易量坐标值
     */
    public String maxVolumeString = "";

    /**
     * 交易量中间坐标
     */
    public String centreVolumeString = "";

    public FiveDayFenShiDataManager(DecimalFormat decimalFormat) {
        dataManager1 = new FenShiDataManager(decimalFormat);
        dataManager2 = new FenShiDataManager(decimalFormat);
        dataManager3 = new FenShiDataManager(decimalFormat);
        dataManager4 = new FenShiDataManager(decimalFormat);
        dataManager5 = new FenShiDataManager(decimalFormat);
        this.decimalFormat = decimalFormat;
    }

    public <T extends IFenShi> void setData1(T fenShi) {
        dataManager1.setData(fenShi);
    }

    public <T extends IFenShi> void setData2(T fenShi) {
        dataManager2.setData(fenShi);
    }

    public <T extends IFenShi> void setData3(T fenShi) {
        dataManager3.setData(fenShi);
    }

    public <T extends IFenShi> void setData4(T fenShi) {
        dataManager4.setData(fenShi);
    }

    public <T extends IFenShi> void setData5(T fenShi) {
        dataManager5.setData(fenShi);
        initPeak();
    }

    private void initPeak() {
        maxPrice = 0;
        maxPrice = Math.max(maxPrice, dataManager1.getMaxPrice());
        maxPrice = Math.max(maxPrice, dataManager2.getMaxPrice());
        maxPrice = Math.max(maxPrice, dataManager3.getMaxPrice());
        maxPrice = Math.max(maxPrice, dataManager4.getMaxPrice());
        maxPrice = Math.max(maxPrice, dataManager5.getMaxPrice());

        minPrice = dataManager5.lastClose * 2 - maxPrice;

        maxVolume = 0;
        maxVolume = Math.max(maxVolume, dataManager1.getMaxVolume());
        maxVolume = Math.max(maxVolume, dataManager2.getMaxVolume());
        maxVolume = Math.max(maxVolume, dataManager3.getMaxVolume());
        maxVolume = Math.max(maxVolume, dataManager4.getMaxVolume());
        maxVolume = Math.max(maxVolume, dataManager5.getMaxVolume());

        initData();
    }

    private void initData() {
        if (Math.abs(minPrice - lastClose) > Math.abs(maxPrice - lastClose)) {
            float temp = maxPrice;
            maxPrice = minPrice;
            minPrice = temp;
        }

        if (maxPrice > lastClose) {
            minPrice = lastClose * 2 - maxPrice;
        } else {
            minPrice = maxPrice;
            maxPrice = lastClose * 2 - maxPrice;
        }

        // 百分比坐标值
        percent = decimalFormat.format(((maxPrice - lastClose) / lastClose * 100)) + "%";

        maxVolumeString = NumberUtils.getVolume((int) maxVolume / 100);
        centreVolumeString = NumberUtils.getVolume((int) maxVolume / 200);
    }
}
