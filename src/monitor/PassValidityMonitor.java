package monitor;

import model.BusPass;
import service.BusPassService;

import java.sql.SQLException;
import java.util.List;

/**
 * Background Monitoring Thread for Pass Validity.
 * Demonstrates:
 * - Multithreading (extends Thread)
 * - Concurrency & Synchronization considerations
 * - Non-blocking asynchronous status inspection
 */
public class PassValidityMonitor extends Thread {
    private final BusPassService passService;
    private final long checkIntervalMillis;
    private volatile boolean running = true;
    private ExpiryAlertListener alertListener;

    /**
     * Interface to broadcast alerts back to GUI or console listeners safely.
     */
    public interface ExpiryAlertListener {
        void onPassStatusUpdate(int expiredCount, int expiringSoonCount);
    }

    public PassValidityMonitor(BusPassService passService, long checkIntervalMillis) {
        super("PassValidityMonitorThread");
        this.passService = passService;
        this.checkIntervalMillis = checkIntervalMillis;
        // Run as daemon so it doesn't prevent JVM exit when GUI closes
        setDaemon(true);
    }

    public void setAlertListener(ExpiryAlertListener listener) {
        this.alertListener = listener;
    }

    public void stopMonitoring() {
        this.running = false;
        interrupt();
    }

    @Override
    public void run() {
        System.out.println("[MONITOR THREAD] Background Pass Validity Monitor started (Interval: " + 
                           (checkIntervalMillis / 1000) + "s).");

        while (running) {
            try {
                // Thread-safe invocation of service layer
                synchronized (passService) {
                    List<BusPass> expired = passService.getExpiredPasses();
                    List<BusPass> expiringSoon = passService.getPassesExpiringSoon(7);

                    int expCount = expired != null ? expired.size() : 0;
                    int soonCount = expiringSoon != null ? expiringSoon.size() : 0;

                    if (alertListener != null) {
                        alertListener.onPassStatusUpdate(expCount, soonCount);
                    }
                }

                // Sleep between monitoring cycles
                Thread.sleep(checkIntervalMillis);

            } catch (InterruptedException e) {
                System.out.println("[MONITOR THREAD] Background monitor thread interrupted. Terminating.");
                break;
            } catch (SQLException e) {
                System.err.println("[MONITOR THREAD] Database error during background scan: " + e.getMessage());
                try {
                    Thread.sleep(checkIntervalMillis);
                } catch (InterruptedException ie) {
                    break;
                }
            }
        }
        System.out.println("[MONITOR THREAD] Monitor thread stopped.");
    }
}
