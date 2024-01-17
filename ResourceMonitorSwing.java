//      Ramirez Inzunza Pedro Miguel - 7CM3
//      Sistemas Distribuidos - Proyecto 5
//      Clase ResourceMonitorSwing: Encargada de ser la clase que monitoriza y muestra el uso de
//      memoria y CPU de las clases WebServer y ProcessingServer. El nombre proviene del uso de los
//      métodos nativos de java en Swing.

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.util.ArrayList;
import java.util.List;

public class ResourceMonitorSwing extends JFrame {
    private final List<Integer> webServerCpuUsageData;
    private final List<Integer> webServerMemoryUsageData;
    private final List<Integer> processingServerCpuUsageData;
    private final List<Integer> processingServerMemoryUsageData;
    private final int MAX_DATA_POINTS = 50;

    private boolean monitoring = false;

    public ResourceMonitorSwing() {
        super("Resource Monitor - Swing");

        webServerCpuUsageData = new ArrayList<>();
        webServerMemoryUsageData = new ArrayList<>();
        processingServerCpuUsageData = new ArrayList<>();
        processingServerMemoryUsageData = new ArrayList<>();

        JButton startButton = new JButton("Iniciar Monitoreo");
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startMonitoring();
            }
        });

        JPanel chartPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawChart(g);
            }
        };

        setLayout(new BorderLayout());
        add(chartPanel, BorderLayout.CENTER);
        add(startButton, BorderLayout.SOUTH);

        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    private void drawChart(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        g2d.setColor(Color.BLUE);
        drawLines(g2d, webServerCpuUsageData, "WebServer CPU Usage", Color.BLUE);
        drawLines(g2d, webServerMemoryUsageData, "WebServer Memory Usage", Color.GREEN);

        g2d.setColor(Color.RED);
        drawLines(g2d, processingServerCpuUsageData, "ProcessingServer CPU Usage", Color.RED);
        drawLines(g2d, processingServerMemoryUsageData, "ProcessingServer Memory Usage", Color.ORANGE);
    }

    private void drawLines(Graphics2D g2d, List<Integer> data, String label, Color color) {
        int width = getWidth();
        int height = getHeight();

        if (!data.isEmpty()) {
            int dataSize = Math.min(MAX_DATA_POINTS, data.size());
            int xIncrement = width / (dataSize == 1 ? 1 : dataSize - 1);

            for (int i = 0; i < dataSize - 1; i++) {
                int x1 = i * xIncrement;
                int y1 = height - data.get(i);
                int x2 = (i + 1) * xIncrement;
                int y2 = height - data.get(i + 1);

                g2d.setColor(color);
                g2d.drawLine(x1, y1, x2, y2);
            }

            // Etiqueta del último valor
            g2d.setColor(Color.BLACK);
            g2d.drawString(label + ": " + data.get(dataSize - 1), width - 150, height - 5);
        }
    }

    private void startMonitoring() {
        if (!monitoring) {
            monitoring = true;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (monitoring) {
                        updateData();
                        repaint();

                        try {
                            Thread.sleep(1000); // Actualizar cada segundo
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();
        }
    }

    private void updateData() {
        // Simulación de datos de uso de CPU y memoria para WebServer y ProcessingServer
        int webServerCpuUsage = getProcessCpuUsage("WebServer");
        int webServerMemoryUsage = getProcessMemoryUsage("WebServer");

        int processingServerCpuUsage = getProcessCpuUsage("ProcessingServer");
        int processingServerMemoryUsage = getProcessMemoryUsage("ProcessingServer");

        webServerCpuUsageData.add(webServerCpuUsage);
        webServerMemoryUsageData.add(webServerMemoryUsage);
        processingServerCpuUsageData.add(processingServerCpuUsage);
        processingServerMemoryUsageData.add(processingServerMemoryUsage);

        // Mantener solo los últimos MAX_DATA_POINTS puntos de datos
        if (webServerCpuUsageData.size() > MAX_DATA_POINTS) {
            webServerCpuUsageData.remove(0);
        }

        if (webServerMemoryUsageData.size() > MAX_DATA_POINTS) {
            webServerMemoryUsageData.remove(0);
        }

        if (processingServerCpuUsageData.size() > MAX_DATA_POINTS) {
            processingServerCpuUsageData.remove(0);
        }

        if (processingServerMemoryUsageData.size() > MAX_DATA_POINTS) {
            processingServerMemoryUsageData.remove(0);
        }
    }

    private int getProcessCpuUsage(String processName) {
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        return (int) (osBean.getSystemLoadAverage() * 100); // Uso de CPU en porcentaje
    }

    private int getProcessMemoryUsage(String processName) {
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        return (int) (memoryBean.getHeapMemoryUsage().getUsed() / (1024 * 1024)); // Uso de memoria en MB
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new ResourceMonitorSwing().setVisible(true);
            }
        });
    }
}
