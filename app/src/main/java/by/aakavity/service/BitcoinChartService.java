package by.aakavity.service;

import by.aakavity.model.BitcoinPrice;
import by.aakavity.model.BitcoinPriceData;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Set;

public class BitcoinChartService extends JFrame {

    private static final String APP_TITLE = "Bitcoin Price Checker";
    private static final String CHART_TITLE = "Bitcoin Prices with one-day prediction";
    private static final String CATEGORY_AXIS_LABEL = "Date";
    private static final String VALUE_AXIS_LABEL = "Price";
    private static final String HISTORICAL_LINE_LABEL = "Historical Bitcoin Price";
    private static final String PREDICTED_LINE_LABEL = "Predicted Bitcoin Price";
    private static final DateTimeFormatter DAY_N_MONTH_FORMAT = DateTimeFormatter.ofPattern("dd/MM");
    private static final int VISIBLE_DATA_OFFSET = 500;
    private static final int MONTH_SIZE = 30;
    private static final int TOOLBAR_HEIGHT = 30;

    public BitcoinChartService(BitcoinPriceData bitcoinPrices, double predict) {
        super(APP_TITLE);

        JPanel chartPanel = createChartPanel(bitcoinPrices, predict);
        add(chartPanel, BorderLayout.CENTER);

        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        setSize(gd.getDisplayMode().getWidth(), gd.getDisplayMode().getHeight() - TOOLBAR_HEIGHT);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    private JPanel createChartPanel(BitcoinPriceData bitcoinPrices, double predict) {
        JFreeChart chart = ChartFactory.createLineChart(CHART_TITLE, CATEGORY_AXIS_LABEL, VALUE_AXIS_LABEL,
                createDataset(bitcoinPrices, predict));

        customizePlot(chart.getCategoryPlot(),
                bitcoinPrices.getMin() - VISIBLE_DATA_OFFSET,
                bitcoinPrices.getMax() + VISIBLE_DATA_OFFSET,
                bitcoinPrices.getHistorical().size()
        );

        return new ChartPanel(chart);
    }

    private CategoryDataset createDataset(BitcoinPriceData bitcoinPrices, double predict) {
        var showYear = bitcoinPrices.getHistorical().size() >= LocalDate.now().getDayOfYear();
        var dataset = new DefaultCategoryDataset();

        Set<BitcoinPrice> data = bitcoinPrices.getHistorical();
        data.add(bitcoinPrices.getCurrent());

        data.forEach(price -> dataset.addValue(price.getPrice(), HISTORICAL_LINE_LABEL, showYear ? price.getDate() :
                price.getDate().format(DAY_N_MONTH_FORMAT)));

        data.add(BitcoinPrice.builder().date(LocalDate.now().plusDays(1)).price(predict).build());

        var countOfHistoricalDataToSkip = data.size() - 2;
        data.stream().skip(countOfHistoricalDataToSkip)
                .forEach(price -> dataset.addValue(price.getPrice(), PREDICTED_LINE_LABEL, showYear ? price.getDate() :
                        price.getDate().format(DAY_N_MONTH_FORMAT)));

        return dataset;
    }

    private void customizePlot(CategoryPlot plot, Double minVal, Double maxVal, int historicalDataCount) {
        var rangeAxis = (NumberAxis) plot.getRangeAxis();
        var domainAxis = plot.getDomainAxis();
        var renderer = plot.getRenderer();

        rangeAxis.setRange(minVal, maxVal);

        if (historicalDataCount <= MONTH_SIZE) {
            renderer.setDefaultItemLabelGenerator(new StandardCategoryItemLabelGenerator());
            renderer.setDefaultItemLabelsVisible(true);
        } else {
            domainAxis.setTickLabelsVisible(false);
        }

        renderer.setSeriesPaint(0, new Color(255, 0, 0));
        renderer.setSeriesPaint(1, new Color(0, 0, 255));
    }
}
