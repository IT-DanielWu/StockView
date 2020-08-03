package ysn.com.stock.helper;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.support.annotation.ColorRes;
import android.view.MotionEvent;

import ysn.com.stock.R;
import ysn.com.stock.interceptor.FenShiUnitInterceptor;
import ysn.com.stock.manager.FenShiDataManager;
import ysn.com.stock.paint.LazyPaint;
import ysn.com.stock.paint.LazyTextPaint;
import ysn.com.stock.utils.NumberUtils;
import ysn.com.stock.view.base.StockView;

/**
 * @Author yangsanning
 * @ClassName FenShiSlideHelper
 * @Description 滑动辅助类
 * @Date 2020/4/16
 */
public class FenShiSlideHelper {

    /**
     * 滑动的阈值
     */
    private static final int TOUCH_SLOP = 20;

    /**
     * FenShiView 相关参数
     */
    private StockView stockView;
    private float viewWidth, viewHeight;
    private float timeTableHeight, timeTableMinY, timeTableMaxY;
    private float textMargin, tableMargin;
    private float topTableHeight, topTableMaxY;
    private float bottomTableHeight, bottomTableMaxY, bottomTableMinY;
    private boolean hasBottomTable;
    private int totalCount;

    /**
     * 数据管理
     */
    private FenShiDataManager dataManager;

    private LazyPaint lazyPaint;

    private float slideX, slideY;
    private float textRectHalfHeight;
    private float slideLineY;
    private String slideValue;
    private int slideNum;

    private boolean isLongPress;
    private Runnable longPressRunnable = () -> {
        isLongPress = true;
        stockView.postInvalidate();
    };

    /**
     * 分时单位转换拦截器
     */
    private FenShiUnitInterceptor fenShiUnitInterceptor;

    public FenShiSlideHelper(StockView stockView, FenShiDataManager dataManager) {
        this.stockView = stockView;
        this.dataManager = dataManager;
        this.lazyPaint = stockView.lazyPaint;
    }

    public void dispatchTouchEvent(MotionEvent ev) {
        stockView.getParent().requestDisallowInterceptTouchEvent(isLongPress);
    }

    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        /**
         * 因圆点是(0,topTableHeight), 为了方便计算, 这里也以圆点为中心
         * 圆点坐标更改: {@link StockView#onDraw(Canvas)}
         */
        float y = event.getY() - stockView.getTopTableHeight();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                slideX = x;
                slideY = y;
                stockView.postDelayed(longPressRunnable, 800);
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_MOVE:
                if (isLongPress) {
                    slideX = x;
                    slideY = y;
                    stockView.postInvalidate();
                } else {
                    if (Math.abs(slideX - x) > TOUCH_SLOP || Math.abs(slideY - y) > TOUCH_SLOP) {
                        stockView.removeCallbacks(longPressRunnable);
                        isLongPress = false;
                        stockView.postInvalidate();
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_OUTSIDE:
                isLongPress = false;
                stockView.postInvalidate();
                break;
            default:
                break;
        }
        return true;
    }

    public void draw(Canvas canvas) {
        if (isLongPress && dataManager.isPriceNoEmpty()) {
            // 初始化FenShiView相关参数
            initFenShiViewParam();

            // 初始化滑动数据
            initSlideData();

            // 绘制滑动线
            drawSlideLine(canvas);

            // 绘制滑动时间
            drawSlideTime(canvas);

            // 绘制滑动值
            drawSlideValue(canvas);
        }
    }

    private void initFenShiViewParam() {
        viewWidth = stockView.getViewWidth();
        viewHeight = stockView.getViewHeight();
        topTableHeight = stockView.getTopTableHeight();
        topTableMaxY = stockView.getTopTableMinY();
        timeTableHeight = stockView.getTimeTableHeight();
        timeTableMinY = stockView.getTimeTableMinY();
        timeTableMaxY = stockView.getTimeTableMaxY();
        tableMargin = stockView.getTableMargin();
        textMargin = stockView.getXYTextMargin();

        hasBottomTable = stockView.isEnabledBottomTable();
        if (hasBottomTable) {
            bottomTableHeight = stockView.getBottomTableHeight();
            bottomTableMaxY = stockView.getBottomTableMaxY();
            bottomTableMinY = stockView.getBottomTableMinY();
        }

        totalCount = stockView.getTotalCount();
    }

    /**
     * 初始化滑动数据
     */
    private void initSlideData() {
        // 文本框一半高度
        textRectHalfHeight = timeTableHeight / 2;

        int priceSize = dataManager.priceSize();

        if (slideX - tableMargin > 0 && slideX < tableMargin + viewWidth) {
            slideNum = (int) ((slideX - tableMargin) / viewWidth * totalCount);
        } else if (slideX - tableMargin - 2 < 0) {
            slideNum = 0;
        } else if (slideX > tableMargin + 1 + viewWidth) {
            slideNum = priceSize - 1;
        }
        if (slideNum >= priceSize) {
            slideNum = priceSize - 1;
        }

        // 分别对有下表格情况以及没有下表格情况进程处理
        if (hasBottomTable && slideY > 0) {
            // 滑动线Y坐标
            if (slideY <= bottomTableMinY + textRectHalfHeight) {
                slideLineY = bottomTableMinY + textRectHalfHeight;
            } else {
                slideLineY = Math.min(slideY, bottomTableMaxY - textRectHalfHeight);
            }

            // 滑动显示的值
            slideValue = getSlipVolume();
        } else {
            // 滑动线Y坐标
            if (slideY > -textRectHalfHeight) {
                slideLineY = -textRectHalfHeight;
            } else {
                slideLineY = Math.max(slideY, topTableMaxY + textRectHalfHeight);
            }

            // 滑动显示的值
            slideValue = getSlipPrice();
        }
    }

    /**
     * 绘制滑动线
     */
    private void drawSlideLine(Canvas canvas) {
        float lineX = Math.min(getX(slideNum), viewWidth - tableMargin);
        lazyPaint.setLineColor(getColor(R.color.stock_slide_line))
                // 绘制竖线
                .drawLine(canvas, lineX, -topTableHeight, lineX, viewHeight)
                // 绘制横线
                .drawLine(canvas, tableMargin, slideLineY, (viewWidth - tableMargin), slideLineY);
    }

    /**
     * 绘制滑动时间
     */
    private void drawSlideTime(Canvas canvas) {
        LazyTextPaint lazyTextPaint = lazyPaint.measure(dataManager.getTime(slideNum));
        float rectWidth = lazyTextPaint.width() + textMargin * 4;
        float rectHalfWidth = rectWidth / 2;

        float slideRectLeft = getX(slideNum) - rectHalfWidth;
        if (slideX < tableMargin + rectHalfWidth) {
            slideRectLeft = tableMargin;
        } else if (slideX > viewWidth - tableMargin - rectHalfWidth) {
            slideRectLeft = viewWidth - tableMargin - rectWidth;
        }

        float slideRectTop = timeTableMinY;
        float slideRectBottom = timeTableMaxY;
        float slideRectRight = slideRectLeft + rectWidth;

        // 绘制背景以及边框
        lazyPaint.drawRect(canvas, slideRectLeft, slideRectTop, slideRectRight, slideRectBottom, getColor(R.color.stock_area_fq))
                .setLineColor(getColor(R.color.stock_slide_line))
                .moveTo(slideRectLeft, slideRectTop)
                .lineTo(slideRectRight, slideRectTop)
                .lineTo(slideRectRight, slideRectBottom)
                .lineTo(slideRectLeft, slideRectBottom)
                .lineToClose(canvas, slideRectLeft, slideRectTop);

        // 绘制相应值
        lazyTextPaint.setColor(getColor(R.color.stock_text_title))
                .drawText(canvas, (slideRectLeft + textMargin * 2), (slideRectTop + ((timeTableHeight + lazyTextPaint.height()) / 2f)));
    }

    /**
     * 绘制成滑动值
     */
    private void drawSlideValue(Canvas canvas) {
        LazyTextPaint lazyTextPaint = lazyPaint.measure(slideValue);

        float slideRectTop = slideLineY - textRectHalfHeight;
        float slideRectBottom = slideLineY + textRectHalfHeight;
        float slideRectLeft;
        float slideRectRight;
        if (slideX < viewWidth / 3f) {
            slideRectLeft = viewWidth - lazyTextPaint.width() - (tableMargin + 1) * 11;
            slideRectRight = viewWidth - tableMargin - 1f;
        } else {
            slideRectLeft = tableMargin + 1;
            slideRectRight = slideRectLeft + lazyTextPaint.width() + (tableMargin + 1f) * 11;
        }

        // 绘制背景以及边框
        lazyPaint.drawRect(canvas, slideRectLeft, slideRectTop, slideRectRight, slideRectBottom, getColor(R.color.stock_area_fq))
                .setLineColor(getColor(R.color.stock_slide_line))
                .moveTo(slideRectLeft, slideRectTop)
                .lineTo(slideRectRight, slideRectTop)
                .lineTo(slideRectRight, slideRectBottom)
                .lineTo(slideRectLeft, slideRectBottom)
                .lineToClose(canvas, slideRectLeft, slideRectTop);

        // 绘制相应值
        lazyTextPaint.setColor(getColor(R.color.stock_text_title))
                .drawText(canvas, (slideRectLeft + (tableMargin + 1) * 4), (slideLineY + lazyTextPaint.height() / 2f));
    }

    /**
     * 获取滑动价格
     */
    private String getSlipPrice() {
        float slipPrice;
        if (slideY < topTableMaxY) {
            slipPrice = dataManager.maxPrice;
        } else if (slideY > -textRectHalfHeight) {
            slipPrice = dataManager.minPrice;
        } else {
            slipPrice = (Math.abs(slideY) * (dataManager.maxPrice - dataManager.minPrice)) / topTableHeight + dataManager.minPrice;
        }
        return fenShiUnitInterceptor == null ? NumberUtils.decimalFormat(slipPrice) : fenShiUnitInterceptor.slipPrice(slipPrice);
    }

    /**
     * 滑动成交量
     */
    private String getSlipVolume() {
        float slipVolume;
        if (slideY < bottomTableMinY) {
            slipVolume = dataManager.maxVolume;
        } else if (slideY > bottomTableMaxY) {
            slipVolume = 0;
        } else {
            slipVolume = (bottomTableHeight - (slideY - bottomTableMinY)) / bottomTableHeight * dataManager.maxVolume;
        }
        return fenShiUnitInterceptor == null ? NumberUtils.decimalFormat(slipVolume) : fenShiUnitInterceptor.slipVolume(slipVolume);
    }

    /**
     * 获取颜色
     */
    private int getColor(@ColorRes int colorRes) {
        return stockView.getContext().getResources().getColor(colorRes);
    }

    /**
     * 根据点获取X坐标
     */
    private float getX(int slideNum) {
        return stockView.getX(slideNum);
    }

    /**
     * 设置分时单位转换拦截器
     */
    public void setFenShiUnitInterceptor(FenShiUnitInterceptor fenShiUnitInterceptor) {
        this.fenShiUnitInterceptor = fenShiUnitInterceptor;
    }
}
