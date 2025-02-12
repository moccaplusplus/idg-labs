package idg.labs;

import org.apache.commons.math3.util.Pair;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.chart.util.ExportUtils;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class Chart {
    public static void showAndSave(List<Pair<Topology, List<Pair<Integer, Integer>>>> data) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Average Cover Time by Topology than by Token Count"); // calls the super class constructor
            JPanel chartPanel = createChartPanel(createDataset(data));
            frame.add(chartPanel, BorderLayout.CENTER);
            frame.setSize(800, 600);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

    private static JPanel createChartPanel(XYDataset dataset) {
        String chartTitle = "Average Cover Time by Topology than by Token Count";
        String xAxisLabel = "Number of Tokens";
        String yAxisLabel = "Average Cover Time (Move Count)";

        JFreeChart chart = ChartFactory.createXYLineChart(chartTitle,
                xAxisLabel, yAxisLabel, dataset);
        customizeChart(chart);

        File imageFile = new File("XYLineChart.png");

        try {
            ExportUtils.writeAsPNG(chart, 800, 600, imageFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new ChartPanel(chart);
    }

    private static XYDataset createDataset(List<Pair<Topology, List<Pair<Integer, Integer>>>> data) {    // this method creates the data as time seris
        XYSeriesCollection dataset = new XYSeriesCollection();
        data.forEach(topologyAndResults -> {
            XYSeries series = new XYSeries(topologyAndResults.getFirst().name());
            topologyAndResults.getSecond().forEach(tokenCountAndAvgMoves ->
                    series.add(tokenCountAndAvgMoves.getFirst(), tokenCountAndAvgMoves.getSecond()));
            dataset.addSeries(series);
        });
        return dataset;
    }

    private static void customizeChart(JFreeChart chart) {
        String fontFamily = "Calibri";
        chart.setPadding(new RectangleInsets(20, 10, 10, 10));
        chart.getTitle().setFont(new Font(fontFamily, Font.BOLD, 28));
        chart.addSubtitle(new TextTitle("", new Font(fontFamily, Font.PLAIN, 21)));

        XYPlot plot = chart.getXYPlot();
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();

        // sets paint color for each series
//        renderer.setSeriesPaint(0, Color.RED);
//        renderer.setSeriesPaint(1, Color.GREEN);
//        renderer.setSeriesPaint(2, Color.YELLOW);
//
//        // sets thickness for series (using strokes)
//        renderer.setSeriesStroke(0, new BasicStroke(4.0f));
//        renderer.setSeriesStroke(1, new BasicStroke(3.0f));
//        renderer.setSeriesStroke(2, new BasicStroke(2.0f));

        // sets paint color for plot outlines
//        plot.setOutlinePaint(Color.BLUE);
//        plot.setOutlineStroke(new BasicStroke(2.0f));

        // sets renderer for lines
        plot.setRenderer(renderer);
        plot.setDomainGridlinePaint(Color.gray);
        plot.setRangeGridlinePaint(Color.gray);
        plot.setBackgroundPaint(Color.white);
        plot.setOutlinePaint(Color.gray);
        plot.setRangeGridlinesVisible(true);
        plot.setDomainGridlinesVisible(true);

        Font labelFont = new Font(fontFamily, Font.BOLD, 13);
        chart.getLegend().setFrame(BlockBorder.NONE);
        chart.getLegend().setItemFont(labelFont);
    }
}