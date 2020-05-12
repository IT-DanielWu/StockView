package ysn.com.stock.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.MotionEvent;

import ysn.com.stock.R;
import ysn.com.stock.bean.IFenShi;
import ysn.com.stock.helper.FenShiSlideHelper;
import ysn.com.stock.manager.FenShiDataManager;

/**
 * @Author yangsanning
 * @ClassName FenShiView
 * @Description 普通分时图
 * @Date 2019/5/4
 * @History 2019/5/4 author: description:
 */
public class FenShiView extends StockView {

    private static final String[] TIME_TEXT = new String[]{"09:30", "11:30/13:00", "15:00"};

    private FenShiDataManager dataManager;

    /**
     * 价格线宽度
     */
    private int priceStrokeWidth;

    /**
     * heartRadius: 心脏半径
     * heartDiameter: 心脏直径
     * HEART_INIT_ALPHA: 初始透明度
     * HEART_BEAT_RATE: 心率
     * HEART_BEAT_FRACTION_RATE: 心跳动画时间
     */
    private int heartRadius;
    private int heartDiameter;
    private int heartInitAlpha;
    private long heartBeatRate;
    private long heartBeatFractionRate;

    private Path pricePath;
    private Paint pricePaint;
    private Path avePricePath;
    private Paint avePricePaint;
    private Path priceAreaPath;
    private Paint priceAreaPaint;

    /**
     * bottomTableMaxY: 下表格最大宽度
     * pillarSpace: 柱状图间距
     * maxPillarHeight: 柱状图绘制最大高度
     */
    private float bottomTableMaxY;
    private Paint pillarPaint;
    private float pillarSpace;
    private float maxPillarHeight;

    /**
     * isBeat: 是否跳动
     * beatFraction: 变化率
     */
    private boolean isBeat = false;
    private float beatFraction;
    private Paint heartPaint;
    private ValueAnimator beatAnimator;
    private Handler beatHandler = new Handler();
    private Runnable beatRunnable = new Runnable() {
        @Override
        public void run() {
            beatAnimator.start();
            invalidate();
            beatHandler.postDelayed(this, heartBeatRate);
        }
    };

    private boolean isEnabledBottomTable;
    private boolean isEnabledSlide;
    private FenShiSlideHelper fenShiSlideHelper;

    public FenShiView(Context context) {
        super(context);
    }

    public FenShiView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public FenShiView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public FenShiView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void init(AttributeSet attrs) {
        super.init(attrs);
        dataManager = new FenShiDataManager(decimalFormat);
        if (isEnabledSlide) {
            fenShiSlideHelper = new FenShiSlideHelper(this, dataManager);
        }
    }

    @Override
    protected void initAttr(AttributeSet attrs) {
        super.initAttr(attrs);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.FenShiView);

        priceStrokeWidth = typedArray.getDimensionPixelSize(R.styleable.FenShiView_fsv_price_stroke_width, 2);

        heartRadius = typedArray.getDimensionPixelSize(R.styleable.FenShiView_fsv_heart_radius, 5);
        heartDiameter = typedArray.getDimensionPixelSize(R.styleable.FenShiView_fsv_heart_diameter, 40);
        heartInitAlpha = typedArray.getInteger(R.styleable.FenShiView_fsv_heart_init_alpha, 255);
        heartBeatRate = typedArray.getInteger(R.styleable.FenShiView_fsv_heart_beat_rate, 2000);
        heartBeatFractionRate = typedArray.getInteger(R.styleable.FenShiView_fsv_heart_beat_fraction_rate, 2000);

        isEnabledBottomTable = typedArray.getBoolean(R.styleable.FenShiView_fsv_is_enabled_bottom_tab, Boolean.FALSE);
        isEnabledSlide = typedArray.getBoolean(R.styleable.FenShiView_fsv_is_enabled_slide, Boolean.FALSE);

        typedArray.recycle();
    }

    @Override
    protected void initPaint() {
        super.initPaint();
        // 初始化价格
        pricePath = new Path();
        pricePaint = new Paint();
        pricePaint.setColor(getColor(R.color.stock_price_line));
        pricePaint.setAntiAlias(true);
        pricePaint.setStyle(Paint.Style.STROKE);
        pricePaint.setStrokeWidth(priceStrokeWidth);

        // 初始化均价
        avePricePath = new Path();
        avePricePaint = new Paint();
        avePricePaint.setColor(getColor(R.color.stock_ave_price_line));
        avePricePaint.setAntiAlias(true);
        avePricePaint.setStyle(Paint.Style.STROKE);
        avePricePaint.setStrokeWidth(priceStrokeWidth);

        // 初始化价格区域
        priceAreaPath = new Path();
        priceAreaPaint = new Paint();
        priceAreaPaint.setColor(getColor(R.color.stock_price_line));
        priceAreaPaint.setStyle(Paint.Style.FILL);
        priceAreaPaint.setStrokeWidth(2);
        priceAreaPaint.setAlpha(15);

        // 初始化扩散圆
        heartPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        heartPaint.setAntiAlias(true);
        beatAnimator = ValueAnimator.ofFloat(0, 1f).setDuration(heartBeatFractionRate);
        beatAnimator.addUpdateListener(animation -> {
            beatFraction = (float) animation.getAnimatedValue();
            invalidate();
        });

        // 初始化柱形图
        pillarPaint = new Paint();
    }

    @Override
    public boolean hasBottomTable() {
        return isEnabledBottomTable;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        // 这里对柱形图最大高度进行限制, 避免顶到时间表格难看
        maxPillarHeight = (bottomTableHeight - 1) * 0.95f;
        bottomTableMaxY = getBottomTableMaxY();
        initPillarSpace();
    }

    /**
     * 初始化柱状图间距
     */
    private void initPillarSpace() {
        // pillarSpace= 宽 - 边距 - 柱子间距(1f)
        pillarSpace = (viewWidth - (tableMargin * 2) - (totalCount * 1f)) / totalCount;
        pillarPaint.setStrokeWidth(pillarSpace);
    }

    /**
     * 绘制时间坐标
     */
    @Override
    protected void onTimeTextDraw(Canvas canvas) {
        super.onTimeTextDraw(canvas);
        textPaint.setColor(getColor(R.color.stock_text_title));

        // 绘制开始区域时间值
        textPaint.getTextBounds(TIME_TEXT[0], (0), TIME_TEXT[0].length(), textRect);
        canvas.drawText(TIME_TEXT[0], tableMargin, getTimeTextY(), textPaint);

        // 绘制中间区域时间值
        textPaint.getTextBounds(TIME_TEXT[1], (0), TIME_TEXT[1].length(), textRect);
        canvas.drawText(TIME_TEXT[1], (((viewWidth - textRect.right) >> 1) - tableMargin), getTimeTextY(), textPaint);

        // 绘制结束区域时间值
        textPaint.getTextBounds(TIME_TEXT[2], (0), TIME_TEXT[2].length(), textRect);
        canvas.drawText(TIME_TEXT[2], (viewWidth - textRect.right - tableMargin), getTimeTextY(), textPaint);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (fenShiSlideHelper != null) {
            fenShiSlideHelper.dispatchTouchEvent(event);
        }
        return super.dispatchTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (fenShiSlideHelper != null) {
            return fenShiSlideHelper.onTouchEvent(event);
        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void onChildDraw(Canvas canvas) {
        super.onChildDraw(canvas);

        if (dataManager.isPriceEmpty()) {
            return;
        }

        // 绘制上表格坐标
        drawTopTableCoordinate(canvas);

        if (hasBottomTable()) {
            // 绘制价格、价格区域、均线、闪烁点、柱形图
            drawPriceLineAndPillar(canvas);

            // 绘制下表格坐标
            drawBottomTableCoordinate(canvas);
        }else {
            drawPriceLine(canvas);
        }

        if (fenShiSlideHelper != null) {
            fenShiSlideHelper.draw(canvas);
        }
    }

    /**
     * 绘制上表格坐标
     */
    private void drawTopTableCoordinate(Canvas canvas) {
        // 价格最大值
        String text = decimalFormat.format(dataManager.maxPrice);
        textPaint.setColor(getColor(R.color.stock_red));
        textPaint.getTextBounds(text, (0), text.length(), textRect);
        float textMargin = getTextMargin();
        float y = (getTopTableMaxY() + textRect.height() + textMargin);
        canvas.drawText(text, textMargin, y, textPaint);

        // 增幅
        text = "+" + dataManager.percent;
        textPaint.getTextBounds(text, 0, text.length(), textRect);
        canvas.drawText(text, (viewWidth - textRect.width() - textMargin), y, textPaint);

        // 价格最小值
        textPaint.setColor(getColor(R.color.stock_green));
        y = getTopTableMinY() - textMargin;
        canvas.drawText(decimalFormat.format(dataManager.minPrice), textMargin, y, textPaint);

        // 减幅
        text = "-" + dataManager.percent;
        textPaint.setColor(getColor(R.color.stock_green));
        textPaint.getTextBounds(text, 0, text.length(), textRect);
        canvas.drawText(text, (viewWidth - textRect.width() - textMargin), y, textPaint);

        // 中间坐标
        textPaint.setColor(getColor(R.color.stock_text_title));
        text = decimalFormat.format(dataManager.lastClose);
        canvas.drawText(text, textMargin, (-(topTableHeight - textRect.height()) / 2f), textPaint);
    }

    /**
     * 绘制价格、价格区域、均线、闪烁点、柱形图
     * 相比于drawPriceLine多了柱形图绘制, 之所以加多一个方法是为了减少循环耗时，以及避免没必要的判断
     */
    private void drawPriceLineAndPillar(Canvas canvas) {
        // 抽取第一个点确定Path的圆点
        float price = dataManager.getPrice(0);
        pricePath.moveTo(tableMargin, getPriceY(price));
        priceAreaPath.moveTo(tableMargin, getTopTableMinY());
        priceAreaPath.lineTo(tableMargin, getPriceY(price));
        avePricePath.moveTo(tableMargin, getPriceY(dataManager.getAvePrice(0)));

        // 柱状图第一个点要跟昨收做对比
        float pillarX = getPillarX(0);
        float pillarStopY = getPillarHeight(0);
        pillarPaint.setColor(getColor(dataManager.getPrice(0) >= dataManager.lastClose ? R.color.stock_red : R.color.stock_green));
        canvas.drawLine(pillarX, bottomTableMaxY, pillarX, pillarStopY, pillarPaint);

        // 对后续点做处理
        for (int i = 1; i < dataManager.priceSize(); i++) {
            price = dataManager.getPrice(i);
            pricePath.lineTo(getX(i), getPriceY(price));
            priceAreaPath.lineTo(getX(i), getPriceY(price));
            avePricePath.lineTo(getX(i), getPriceY(dataManager.getAvePrice(i)));

            if (isBeat && dataManager.isLastPrice(i)) {
                // 绘制扩散圆
                heartPaint.setColor(getColor(R.color.stock_price_line));
                heartPaint.setAlpha((int) (heartInitAlpha - heartInitAlpha * beatFraction));
                canvas.drawCircle(getX(i), getPriceY(price), (heartRadius + heartDiameter * beatFraction), heartPaint);

                // 绘制中心圆
                heartPaint.setAlpha(255);
                heartPaint.setColor(getColor(R.color.stock_price_line));
                canvas.drawCircle(getX(i), getPriceY(price), heartRadius, heartPaint);
            }

            // 柱形图绘制
            pillarPaint.setColor(getColor(dataManager.getPrice(i) >= dataManager.getPrice(i - 1) ? R.color.stock_red : R.color.stock_green));
            pillarX = getPillarX(i);
            pillarStopY = getPillarHeight(i);
            canvas.drawLine(pillarX, bottomTableMaxY, pillarX, pillarStopY, pillarPaint);
        }

        // 价格颜色区域需要进行闭合处理
        priceAreaPath.lineTo(getX(dataManager.getLastPricePosition()), getTopTableMinY());
        priceAreaPath.close();

        // 绘制曲线以及区域
        canvas.drawPath(pricePath, pricePaint);
        canvas.drawPath(priceAreaPath, priceAreaPaint);
        canvas.drawPath(avePricePath, avePricePaint);

        // 使用完后，重置画笔
        pricePath.reset();
        priceAreaPath.reset();
        avePricePath.reset();
    }

    /**
     * 获取第i个柱状图的绘制位置（x坐标）
     *
     * @param i 第几个
     * @return 第i个柱状图的绘制位置（x坐标）
     */
    private float getPillarX(int i) {
        return tableMargin + (pillarSpace * i) + (i * 1f) + 1;
    }

    /**
     * 获取第i个柱状图的高度（stop y坐标）
     *
     * @param i 第几个
     * @return 第i个柱状图的高度（stop y坐标）
     */
    private float getPillarHeight(int i) {
        return bottomTableMaxY - (dataManager.getVolume(i) * maxPillarHeight) / dataManager.maxVolume;
    }

    /**
     * 绘制下表格坐标
     */
    private void drawBottomTableCoordinate(Canvas canvas) {
        // 下表格最大量
        textPaint.getTextBounds(dataManager.maxVolumeString, 0, dataManager.maxVolumeString.length(), textRect);
        float x = viewWidth - tableMargin - xYTextMargin - textRect.width();
        canvas.drawText(dataManager.maxVolumeString, x, (getBottomTableMinY() + textRect.height() + xYTextMargin), textPaint);

        // 下表格中间值
        textPaint.getTextBounds(dataManager.centreVolumeString, 0, dataManager.centreVolumeString.length(), textRect);
        canvas.drawText(dataManager.centreVolumeString, x, (getBottomTableMinY() + (bottomTableHeight + textRect.height()) / 2), textPaint);
    }

    /**
     * 绘制价格、价格区域、均线、闪烁点
     */
    private void drawPriceLine(Canvas canvas) {
        // 抽取第一个点确定Path的圆点
        float priceY = getPriceY(dataManager.getPrice(0));
        pricePath.moveTo(tableMargin,priceY );
        priceAreaPath.moveTo(tableMargin, getTopTableMinY());
        priceAreaPath.lineTo(tableMargin,priceY);
        avePricePath.moveTo(tableMargin, getPriceY(dataManager.getAvePrice(0)));

        // 对后续点做处理
        for (int i = 1; i < dataManager.priceSize(); i++) {
            priceY = getPriceY(dataManager.getPrice(i));
            pricePath.lineTo(getX(i), priceY);
            priceAreaPath.lineTo(getX(i), priceY);
            avePricePath.lineTo(getX(i), getPriceY(dataManager.getAvePrice(i)));

            if (isBeat && dataManager.isLastPrice(i)) {
                // 绘制扩散圆
                heartPaint.setColor(getColor(R.color.stock_price_line));
                heartPaint.setAlpha((int) (heartInitAlpha - heartInitAlpha * beatFraction));
                canvas.drawCircle(getX(i), priceY, (heartRadius + heartDiameter * beatFraction), heartPaint);

                // 绘制中心圆
                heartPaint.setAlpha(255);
                heartPaint.setColor(getColor(R.color.stock_price_line));
                canvas.drawCircle(getX(i), priceY, heartRadius, heartPaint);
            }
        }

        // 价格颜色区域需要进行闭合处理
        priceAreaPath.lineTo(getX(dataManager.getLastPricePosition()), getTopTableMinY());
        priceAreaPath.close();

        // 绘制曲线以及区域
        canvas.drawPath(pricePath, pricePaint);
        canvas.drawPath(priceAreaPath, priceAreaPaint);
        canvas.drawPath(avePricePath, avePricePaint);

        // 使用完后，重置画笔
        pricePath.reset();
        priceAreaPath.reset();
        avePricePath.reset();
    }

    /**
     * 根据当前价格值获取相应y轴坐标
     *
     * @param price 当前价格
     * @return 当前价格的相应y轴坐标
     */
    private float getPriceY(float price) {
        return getY(price, dataManager.minPrice, dataManager.maxPrice);
    }

    public <T extends IFenShi> void setData(T fenShi) {
        dataManager.setData(fenShi);
        invalidate();
        startBeat();
    }

    /**
     * 开始心跳
     */
    public void startBeat() {
        stopBeat();
        if (dataManager.isTimeNotEmpty() && isBeatTime()) {
            isBeat = true;
            beatHandler.post(beatRunnable);
        }
    }

    /**
     * 是否可以开启心跳
     */
    public boolean isBeatTime() {
        String lastTime = dataManager.getLastTime();
        return !"11:30".equals(lastTime) && !"15:00".equals(lastTime);
    }

    /**
     * 停止心跳
     */
    public void stopBeat() {
        isBeat = false;
        beatHandler.removeCallbacks(beatRunnable);
    }
}
