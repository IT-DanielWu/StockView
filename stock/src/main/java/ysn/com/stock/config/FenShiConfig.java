package ysn.com.stock.config;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

import ysn.com.stock.R;

/**
 * @Author yangsanning
 * @ClassName FenShiConfig
 * @Description {@link ysn.com.stock.view.FenShiView} 的参数信息
 * @Date 2020/7/23
 */
public class FenShiConfig {

    public static final String[] TIME_TEXT = new String[]{"09:30", "11:30/13:00", "15:00"};

    /**
     * 价格线宽度
     */
    public int priceStrokeWidth;

    /**
     * heartRadius: 心脏半径
     * heartDiameter: 心脏直径
     * HEART_INIT_ALPHA: 初始透明度
     * HEART_BEAT_RATE: 心率
     * HEART_BEAT_FRACTION_RATE: 心跳动画时间
     */
    public int heartRadius;
    public int heartDiameter;
    public int heartInitAlpha;
    public long heartBeatRate;
    public long heartBeatFractionRate;

    public boolean isEnabledSlide;

    public FenShiConfig(Context context, @Nullable AttributeSet attrs) {
        initAttr(context, attrs);
    }

    public void initAttr(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.FenShiView);

        priceStrokeWidth = typedArray.getDimensionPixelSize(R.styleable.FenShiView_fsv_price_stroke_width, 2);

        heartRadius = typedArray.getDimensionPixelSize(R.styleable.FenShiView_fsv_heart_radius, 5);
        heartDiameter = typedArray.getDimensionPixelSize(R.styleable.FenShiView_fsv_heart_diameter, 40);
        heartInitAlpha = typedArray.getInteger(R.styleable.FenShiView_fsv_heart_init_alpha, 255);
        heartBeatRate = typedArray.getInteger(R.styleable.FenShiView_fsv_heart_beat_rate, 2000);
        heartBeatFractionRate = typedArray.getInteger(R.styleable.FenShiView_fsv_heart_beat_fraction_rate, 2000);

        isEnabledSlide = typedArray.getBoolean(R.styleable.FenShiView_fsv_is_enabled_slide, Boolean.FALSE);

        typedArray.recycle();
    }

}
