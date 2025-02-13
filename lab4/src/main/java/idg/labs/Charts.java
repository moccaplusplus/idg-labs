package idg.labs;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.xy.XYDataset;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;

import static java.util.stream.IntStream.range;

public class Charts {
    public static void displayChart(JFreeChart chart, int width, int height) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame(chart.getTitle().getText());
            ChartPanel panel = new ChartPanel(chart);
            panel.setSize(width, height);
            frame.add(panel, BorderLayout.CENTER);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.pack();
            frame.setVisible(true);
            frame.setLocationRelativeTo(null);
            frame.toFront();
        });
    }

    public static JFreeChart barChart(
            String title, String subtitle, String xLabel, String yLabel, CategoryDataset dataset) {
        JFreeChart chart = ChartFactory.createBarChart(title, xLabel, yLabel, dataset);
        addSubtitle(chart, subtitle);
        setupChart(chart);
        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setDrawBarOutline(false);
        renderer.setShadowVisible(false);
        renderer.setGradientPaintTransformer(null);
        renderer.setBarPainter(new StandardBarPainter());
        plot.setRangeGridlinePaint(Color.gray);
        plot.setRangeGridlinesVisible(true);
        plot.setDomainGridlinesVisible(false);
        return chart;
    }

    public static JFreeChart scatterPlot(
            String title, String subtitle, String xLabel, String yLabel, XYDataset dataset) {
        JFreeChart chart = ChartFactory.createScatterPlot(title, xLabel, yLabel, dataset);
        addSubtitle(chart, subtitle);
        setupXYPlot(chart.getXYPlot(), 9);
        setupChart(chart);
        return chart;
    }

    public static JFreeChart lineChart(
            String title, String subtitle, String xLabel, String yLabel, XYDataset dataset) {
        JFreeChart chart = ChartFactory.createXYLineChart(title, xLabel, yLabel, dataset);
        addSubtitle(chart, subtitle);
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setDefaultStroke(new BasicStroke(1.5f));
        renderer.setAutoPopulateSeriesStroke(false);
        chart.getXYPlot().setRenderer(renderer);
        setupXYPlot(chart.getXYPlot(), 5);
        setupChart(chart);
        return chart;
    }

    private static void addSubtitle(JFreeChart chart, String subtitle) {
        if (subtitle != null && !subtitle.isEmpty()) {
            chart.addSubtitle(new TextTitle(subtitle));
        }
    }

    private static void setupXYPlot(XYPlot plot, double shapeSize) {
        Shape shape = new Ellipse2D.Double(-shapeSize / 2, -shapeSize / 2, shapeSize, shapeSize);
        range(0, plot.getSeriesCount()).forEach(i -> plot.getRenderer().setSeriesShape(i, shape));
        plot.setDomainGridlinePaint(Color.gray);
        plot.setRangeGridlinePaint(Color.gray);
        plot.setRangeGridlinesVisible(true);
        plot.setDomainGridlinesVisible(true);
    }

    private static void setupChart(JFreeChart chart) {
        chart.setPadding(new RectangleInsets(10, 10, 10, 10));
        chart.setAntiAlias(true);
        chart.getTitle().setFont(new Font(chart.getTitle().getFont().getFontName(), Font.PLAIN, 15));
        chart.getTitle().setPadding(new RectangleInsets(5, 5, 5, 5));
        range(0, chart.getSubtitleCount()).mapToObj(chart::getSubtitle)
                .filter(TextTitle.class::isInstance).map(TextTitle.class::cast)
                .forEach(subtitle -> {
                    subtitle.setFont(new Font(subtitle.getFont().getFontName(), Font.PLAIN, 12));
                    subtitle.setPaint(Color.darkGray);
                    subtitle.setPadding(new RectangleInsets(5, 5, 5, 5));
                });
        Plot plot = chart.getPlot();
        plot.setBackgroundPaint(Color.white);
        plot.setOutlinePaint(Color.gray);
        chart.getLegend().setPosition(RectangleEdge.BOTTOM);
        chart.getLegend().setFrame(new BlockBorder(10, 50, 5, 25, Color.white));
    }
}
