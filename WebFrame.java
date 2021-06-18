import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

public class WebFrame extends JFrame {
    private final int MAX_SIZE = 40;
    private final int MAX_HEIGHT = 40;
    private Launcher launcher;
    private DefaultTableModel model;
    private JTable table;
    private JPanel panel;

    private JButton fetchSingle;
    private JButton fetchConcurrent;
    private JButton stopButton;

    private JLabel runningLabel;
    private JLabel completedLabel;
    private JLabel elapsedLabel;
    private AtomicInteger runningNum;
    private AtomicInteger urlNum;
    private long timeStarted;

    private JTextField textField;
    private JProgressBar progressBar;

    List<String> urls;
    public WebFrame() throws FileNotFoundException {
        launcher = null;
        setName("WebLoader");
        setSize(new Dimension(400, 500));
        setUpTable();
        urls = new ArrayList<>();
        readUrlsFromFile();
        setUpBelow();
        setVisible(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    public void updateDisplayInformation(int row){
        runningNum.decrementAndGet();
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                runningLabel.setText("Running: " + runningNum);
            }
        });
        urlNum.incrementAndGet();
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                completedLabel.setText("Completed: " + urlNum);
            }
        });
        runningLabel.setText("Running: " + runningNum);
        completedLabel.setText("Completed: " + 0);
        long elapsedTime = System.currentTimeMillis() - timeStarted;
        elapsedLabel.setText("Elapsed: " + elapsedTime);
        progressBar.setValue(urlNum.get());
    }

    private void readUrlsFromFile() throws FileNotFoundException {
        BufferedReader reader = new BufferedReader(new FileReader("links.txt"));
        int row = 0;
        while(true){
            String currUrl;
            try {
                currUrl = reader.readLine();
                if(currUrl == null) break;
            } catch (IOException e) {
                break;
            }
            model.setRowCount(model.getRowCount() + 1);
            model.setValueAt(currUrl, row, 0);
            urls.add(currUrl);
            row++;
        }
    }

    private void setUpBelow(){
        setUpFetchButtons();
        setUpTextField();
        setUpThreeLabels();
        setUpProgressBar();
        setStopButton();
    }

    private void setStopButton(){
        stopButton = new JButton("Stop");
        stopButton.setEnabled(false);
        stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(launcher != null) launcher.interrupt();
                fetchSingle.setEnabled(true);
                fetchConcurrent.setEnabled(true);
                stopButton.setEnabled(false);
                progressBar.setValue(0);
                //renewEverything();
            }
        });
        panel.add(stopButton);
    }

    private void setUpProgressBar(){
        progressBar = new JProgressBar();
        progressBar.setValue(0);
        progressBar.setMaximum(urls.size());
        panel.add(progressBar);
    }

    private void setUpTextField(){
        textField = new JTextField();
        Dimension d = new Dimension(MAX_SIZE, MAX_HEIGHT);
        textField.setMaximumSize(d);
        textField.setText("4");
        panel.add(textField);
    }

    private void setUpThreeLabels(){
        timeStarted = System.currentTimeMillis();
        runningLabel = new JLabel("Running: " + 0);
        panel.add(runningLabel);
        completedLabel = new JLabel("Completed: " + 0);
        panel.add(completedLabel);
        elapsedLabel = new JLabel("Elapsed: " ); // pirobashi egrea
        panel.add(elapsedLabel);
    }

    private void setUpFetchButtons(){
        fetchSingle = new JButton("Single Thread Fetch");
        fetchSingle.setEnabled(true);
        fetchConcurrent = new JButton("Concurrent Fetch");
        fetchConcurrent.setEnabled(true);
        fetchSingle.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fetchUrls(1);
            }
        });
        panel.add(fetchSingle);
        fetchConcurrent.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fetchUrls(Integer.parseInt(textField.getText()));
            }
        });
        panel.add(fetchConcurrent);

    }

    private void fetchUrls(int threadsLimit) {
        returnToStartState();
        launcher = new Launcher(threadsLimit);
        launcher.start();
    }

    private void returnToStartState(){
        fetchSingle.setEnabled(false);
        fetchConcurrent.setEnabled(false);
        stopButton.setEnabled(true);
        renewEverything();
    }

    private void renewEverything(){
        timeStarted = System.currentTimeMillis();
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                progressBar.setValue(0);
                runningLabel.setText("Running: " + 0);
                completedLabel.setText("Completed: " + 0);
                elapsedLabel.setText("Elapsed: " );
                for (int i = 0; i < table.getRowCount(); i++){
                    table.setValueAt("", i, 1);
                }
            }
        });
    }

    private void setUpTable(){
        model = new DefaultTableModel(new String[] { "url", "status"}, 0);
        table = new JTable(model);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        JScrollPane scrollpane = new JScrollPane(table);
        scrollpane.setPreferredSize(new Dimension(600,300));
        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(scrollpane);
        add(panel);
    }

    public void updateRow(String s, int row){
        model.setValueAt(s, row, 1);
    }

    class Launcher extends Thread{
        private int limit;
        private Semaphore workers;
        List<WebWorker> workersList;
        public Launcher(int numThreads){
            workersList = new ArrayList<>();
            runningNum = new AtomicInteger(0);
            urlNum = new AtomicInteger(0);
            limit = numThreads;
            workers = new Semaphore(limit);
        }
        @Override
        public void run(){
            runningNum.incrementAndGet();
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    runningLabel.setText("Running: " + runningNum);
                }
            });
            try {
                for (int i = 0; i < urls.size(); i++){
                    workers.acquire();
                    WebWorker currWorker = new WebWorker(WebFrame.this, i, urls.get(i), workers);
                    workersList.add(currWorker);
                    runningNum.incrementAndGet();
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            runningLabel.setText("Running: " + runningNum);
                        }
                    });
                    currWorker.start();
                }
            } catch (InterruptedException e){
                for (WebWorker currWorker : workersList){
                    currWorker.interrupt();
                }
                //returnToStartState();
            }
            runningNum.decrementAndGet();
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    runningLabel.setText("Running: " + runningNum);
                }
            });
            try{
                for(WebWorker w : workersList){
                    w.join();
                }
            }catch (InterruptedException e){

            }
            //renewEverything();
            stopButton.setEnabled(false);
            fetchConcurrent.setEnabled(true);
            fetchSingle.setEnabled(true);
        }
    }

    public static void main(String[] args) throws FileNotFoundException {
        WebFrame frame = new WebFrame();
    }

}
