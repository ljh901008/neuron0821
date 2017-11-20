package neuron.com.room.Activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import neuron.com.comneuron.BaseActivity;
import neuron.com.comneuron.R;

/**
 * Created by ljh on 2017/7/20.
 */
public class WaterCombinedChartActivity extends BaseActivity implements View.OnClickListener{
    private ImageButton back_ibtn;
    private Button titleRight_ibtn;
    private TextView deviceName_tv, roomName_tv;
    private CombinedChart combinedChart;
    private Intent intent;
    private String otherMsg,deviceName,deviceId;
    private List<String> xAxisValues;
    private List<Float> lineValues;
    private List<Float> barValues;
    private String deviceRoom,deviceType, roomId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.waterlinechart);
        init();
        setListener();
    }
    private void init(){
        intent = getIntent();
        otherMsg = intent.getStringExtra("otherMsg");
        deviceName = intent.getStringExtra("deviceName");
        deviceId = intent.getStringExtra("deviceId");
        deviceRoom = intent.getStringExtra("deviceRoom");
        deviceType = intent.getStringExtra("deviceType");
        roomId = intent.getStringExtra("roomId");

        back_ibtn = (ImageButton) findViewById(R.id.waterlinechart_back_ibtn);
        titleRight_ibtn = (Button) findViewById(R.id.waterlinechart_edit_btn);
        deviceName_tv = (TextView) findViewById(R.id.waterlinechart_devicename_tv);
        roomName_tv = (TextView) findViewById(R.id.waterlinechart_roomnama_tv);
        combinedChart = (CombinedChart) findViewById(R.id.waterlinechart_chart);
        roomName_tv.setText(deviceRoom);
        deviceName_tv.setText(deviceName);
        analysis(otherMsg);
    }
    private void setListener(){
        back_ibtn.setOnClickListener(this);
        titleRight_ibtn.setOnClickListener(this);
    }
    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.waterlinechart_back_ibtn://返回
                finish();
                this.overridePendingTransition(0, R.anim.activity_close);
                break;
            case R.id.waterlinechart_edit_btn://编辑
                Intent intent = new Intent(WaterCombinedChartActivity.this, EditActivity.class);
                intent.putExtra("deviceName", deviceName);
                intent.putExtra("deviceRoom", deviceRoom);
                intent.putExtra("brand", "");
                intent.putExtra("serial", "");
                intent.putExtra("roomId", roomId);
                intent.putExtra("deviceId", deviceId);
                intent.putExtra("deviceType", deviceType);
                startActivityForResult(intent,100);
                break;
            default:
                break;
        }
    }

    private void analysis(String data){
        try {
            if (!TextUtils.isEmpty(data)) {
                JSONArray jsonArray = new JSONArray(data);
                int length = jsonArray.length();
                if (length > 0) {
                    xAxisValues = new ArrayList<>();
                    lineValues = new ArrayList<>();
                    barValues = new ArrayList<>();
                    for (int i = 0; i < length; i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        xAxisValues.add(jsonObject.getString("date"));
                        lineValues.add((float) jsonObject.getDouble("tds"));
                        barValues.add((float) jsonObject.getDouble("flow"));
                    }
                    setCombineChart(combinedChart, xAxisValues, lineValues, barValues, "TDS（mg/L）", "水量（L）");
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == 100) {
                if (data != null) {
                    deviceRoom = data.getStringExtra("roomName");
                    roomName_tv.setText(deviceRoom);
                    deviceName = data.getStringExtra("deviceName");
                    deviceName_tv.setText(deviceName);
                }
            }
        }
    }

    /**
     * 设置柱线组合图样式，柱图依赖左侧y轴，线图依赖右侧y轴
     */
    public static void setCombineChart(CombinedChart combineChart, final List<String> xAxisValues, List<Float> lineValues, List<Float> barValues, String lineTitle, String barTitle) {
        combineChart.getDescription().setEnabled(false);//设置描述
        combineChart.setPinchZoom(true);//设置按比例放缩柱状图
        combineChart.setDoubleTapToZoomEnabled(false);//设置为false以禁止通过在其上双击缩放图表。
        combineChart.setTouchEnabled(false);//启用/禁用与图表的所有可能的触摸交互。
        combineChart.setDragEnabled(false);//启用/禁用拖动（平移）图表。
        combineChart.setScaleEnabled(false);//启用/禁用缩放图表上的两个轴。
       /* MPChartMarkerView markerView = new MPChartMarkerView(combineChart.getContext(), R.layout.custom_marker_view);
        combineChart.setMarker(markerView);*/

        //设置绘制顺序，让线在柱的上层
        combineChart.setDrawOrder(new CombinedChart.DrawOrder[]{
                CombinedChart.DrawOrder.BAR, CombinedChart.DrawOrder.LINE
        });

        //x坐标轴设置
        XAxis xAxis = combineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setAxisLineWidth(2.5f);
        xAxis.setAxisLineColor(Color.rgb(238, 238, 238));
        xAxis.setTextColor(Color.rgb(238,238,238));
        xAxis.setLabelCount(xAxisValues.size() + 2);
        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float v, AxisBase axisBase) {
                if (v < 0 || v > (xAxisValues.size() - 1))//使得两侧柱子完全显示
                    return "";
                return xAxisValues.get((int) v);
            }
        });

        //y轴设置
        YAxis leftAxis = combineChart.getAxisLeft();
        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        leftAxis.setDrawGridLines(false);
        leftAxis.setDrawLabels(false);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setAxisLineWidth(2.5f);
        leftAxis.setAxisLineColor(Color.rgb(238, 238, 238));

        final YAxis rightAxis = combineChart.getAxisRight();
        rightAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);//设置标签在内侧
        rightAxis.setAxisLineColor(Color.rgb(255, 229, 83));
        rightAxis.setTextColor(Color.rgb(255, 229, 83));
        rightAxis.setDrawGridLines(false);
        rightAxis.setLabelCount(3,true);
        rightAxis.setAxisMaximum(300);
        rightAxis.setAxisMinimum(0);
        rightAxis.setAxisLineWidth(2.5f);
        rightAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                if ((int) value == 150) {
                    return (int) value + " 优";
                } else if ((int) value == 300) {
                    return (int) value + " 差";
                }
                return String.valueOf((int)value);
            }
        });

        //图例设置
        Legend legend = combineChart.getLegend();
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);
        legend.setDirection(Legend.LegendDirection.LEFT_TO_RIGHT);
        legend.setForm(Legend.LegendForm.SQUARE);
        legend.setTextSize(12f);
        legend.setTextColor(Color.rgb(238, 238, 238));

        //设置组合图数据
        CombinedData data = new CombinedData();
        data.setData(generateLineData(lineValues, lineTitle));
        data.setData(generateBarData(barValues, barTitle));
        combineChart.setData(data);//设置组合图数据源
        //使得两侧柱子完全显示
        xAxis.setAxisMinimum(combineChart.getCombinedData().getXMin() - 1f);
        xAxis.setAxisMaximum(combineChart.getCombinedData().getXMax() + 1f);

        combineChart.setExtraTopOffset(30);
        combineChart.setExtraBottomOffset(10);
        combineChart.animateX(1500);//数据显示动画，从左往右依次显示
        combineChart.invalidate();
    }

    /**
     * 生成线图数据
     */
    private static LineData generateLineData(List<Float> lineValues, String lineTitle) {
        ArrayList<Entry> lineEntries = new ArrayList<>();

        for (int i = 0, n = lineValues.size(); i < n; ++i) {
            lineEntries.add(new Entry(i, lineValues.get(i)));
        }

        LineDataSet lineDataSet = new LineDataSet(lineEntries, lineTitle);
        lineDataSet.setColor(Color.rgb(255, 229, 83));
        lineDataSet.setLineWidth(2.5f);//设置线的宽度
        //lineDataSet.setDrawCircleHole(false);
        lineDataSet.setCircleColor(Color.rgb(244, 219, 100));//设置圆圈的颜色
        lineDataSet.setCircleColorHole(Color.YELLOW);//设置圆圈内部洞的颜色
        //lineDataSet.setValueTextColor(Color.rgb(254,116,139));
        lineDataSet.setAxisDependency(YAxis.AxisDependency.RIGHT);//设置线数据依赖于右侧y轴
        lineDataSet.setDrawValues(false);//不绘制线的数据

        LineData lineData = new LineData(lineDataSet);
        lineData.setValueTextSize(10f);
        lineData.setValueFormatter(new IValueFormatter() {
            @Override
            public String getFormattedValue(float value, Entry entry, int i, ViewPortHandler viewPortHandler) {
                return double2String(value, 2);
            }
        });

        return lineData;
    }

    /**
     * 生成柱图数据
     *
     * @param barValues
     * @return
     */
    private static BarData generateBarData(List<Float> barValues, String barTitle) {

        ArrayList<BarEntry> barEntries = new ArrayList<>();

        for (int i = 0, n = barValues.size(); i < n; ++i) {
            barEntries.add(new BarEntry(i, barValues.get(i)));
        }

        BarDataSet barDataSet = new BarDataSet(barEntries, barTitle);
        barDataSet.setColor(Color.rgb(238, 238, 238));
        barDataSet.setValueTextColor(Color.rgb(238, 238, 238));
        barDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);

        BarData barData = new BarData(barDataSet);
        barData.setValueTextSize(10f);
        barData.setBarWidth(0.4f);
        barData.setValueFormatter(new IValueFormatter() {
            @Override
            public String getFormattedValue(float value, Entry entry, int i, ViewPortHandler viewPortHandler) {
                return double2String(value, 2);
            }
        });

        return barData;
    }
    /**
     * 将double转为数值，并最多保留num位小数。例如当num为2时，1.268为1.27，1.2仍为1.2；1仍为1，而非1.00;100.00则返回100。
     *
     * @param d
     * @param num 小数位数
     * @return
     */
    public static String double2String(double d, int num) {
        NumberFormat nf = NumberFormat.getNumberInstance();
        nf.setMaximumFractionDigits(num);//保留两位小数
        nf.setGroupingUsed(false);//去掉数值中的千位分隔符

        String temp = nf.format(d);
        if (temp.contains(".")) {
            String s1 = temp.split("\\.")[0];
            String s2 = temp.split("\\.")[1];
            for (int i = s2.length(); i > 0; --i) {
                if (!s2.substring(i - 1, i).equals("0")) {
                    return s1 + "." + s2.substring(0, i);
                }
            }
            return s1;
        }
        return temp;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
            this.overridePendingTransition(0, R.anim.activity_close);
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }
}
