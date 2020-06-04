## MiniFenShiView

## 效果预览

| [MiniFenShiView]                    | 
| ------------------------------- | 
| <img src="images/minifenshiview.gif" height="512" /> |


## 主要文件
| 名字             | 摘要           |
| ---------------- | -------------- |
| [MiniFenShiView] | 迷你分时图 |


### 1. 基本用法

#### 1.1 布局中添加
```android
<ysn.com.stock.view.MiniFenShiView
  android:layout_width="100dp"
  android:layout_height="65dp"
  android:layout_marginTop="5dp"
  android:layout_marginBottom="5dp" />
```

#### 1.2 设置数据

##### 1.2.1 首先实现 [IFenShi] 以及 [IFenShiData] 
```android
public class FenShiTime implements IFenShi {

    @Override
    public String getFenShiCode() {
        return code;
    }

    @Override
    public List<? extends IFenShiData> getFenShiData() {
        return data;
    }

    @Override
    public float getFenShiLastClose() {
        return settlement;
    }

    public static class DataBean implements IFenShiData {
        @Override
        public String getFenShiTime() {
            return dateTime;
        }

        @Override
        public float getFenShiPrice() {
            return trade;
        }

        @Override
        public float getFenShiVolume() {
            return volume;
        }

        @Override
        public float getFenShiAvgPrice() {
            return avgPrice;
        }
    }
}

```
##### 1.2.2 然后再进行数据设置
```android
  miniFenShiView.setData(iFenShi);
```


### 2. 配置属性([Attributes])

#### FenShiView
|name|format|description|
|:---:|:---:|:---:|
| mfsv_up_color | color | 涨颜色 |
| mfsv_down_color | color | 跌颜色 |
| mfsv_equal_color | color | 不涨不跌颜色 |
| mfsv_gradient_bottom_color | color | 渐变底部颜色 |
| mfsv_stroke_width | dimension | 实线宽 |
| mfsv_area_alpha | integer | 价格区域透明度 |
| mfsv_dotted_line_width | dimension | 虚线宽 |
| mfsv_dotted_line_space | dimension | 虚线间距 |

[MiniFenShiView]:https://github.com/yangsanning/StockView/blob/master/stock/src/main/java/ysn/com/stock/view/MiniFenShiView.java
[IFenShi]:https://github.com/yangsanning/StockView/blob/master/stock/src/main/java/ysn/com/stock/bean/IFenShi.java
[IFenShiData]:https://github.com/yangsanning/StockView/blob/master/stock/src/main/java/ysn/com/stock/bean/IFenShiData.java
[Attributes]:https://github.com/yangsanning/StockView/blob/master/stock/src/main/res/values/attrs_mini_fen_shi.xml

