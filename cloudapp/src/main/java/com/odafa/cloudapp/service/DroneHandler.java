package com.odafa.cloudapp.service;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.odafa.cloudapp.dto.DataPoint;
import com.odafa.cloudapp.dto.DroneInfo;
import com.odafa.cloudapp.utils.DataMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j

public class DroneHandler {
    private static final long MAX_WAIT_TIME = 10_000L;
    private final String droneId;
    
    private volatile long lastUpdatetime;
    private DroneInfo lastStatus;

    private final Socket droneSocket;
    private final InputStream streamIn;
    private final OutputStream streamout;

    private final BlockingQueue<byte[]> indoxMessageBuffer;
    private final ExecutorService handlerExecutor;
    private final ControlManager manager;
    
    public DroneHandler(ControlManager controlManager, Socket clientSocket) {
        this.manager = controlManager;
        this.droneSocket = clientSocket;
        this.indoxMessageBuffer = new ArrayBlockingQueue<>(1024);
        this.handlerExecutor = Executors.newFixedThreadPool(2);
        try {
            this.streamIn = droneSocket.getInputStream();
            this.streamout = droneSocket.getOutputStream();
            droneId = DataMapper.extractDroneIdFromNetwork(droneSocket);

        } catch (Exception e) {
            close();
            throw new RuntimeException(e);
        }
        manager.SetControlHandlerForDroneId(droneId, this);
        log.info("Control Connection Established ID {}, IP {} ", droneId, droneSocket.getInetAddress().toString());
    }

    public void activate() {
        handlerExecutor.execute(() -> {
            while(!droneSocket.isClosed()) {
                try{
                    this.lastStatus = DataMapper.fromNetworkToDroneInfo(streamIn);
                    this.lastUpdatetime = System.currentTimeMillis();
                } catch (Exception e) {
                    log.info("Control Connection with {} Closed, reason: {}", droneSocket.getInetAddress().toString(), e.getMessage());
					close();
                }
            }
            close();
        });

        handlerExecutor.execute(() -> {
            while (!droneSocket.isClosed()) {
                try{
                    streamout.write(indoxMessageBuffer.take());
                    streamout.flush();

                } catch (SocketException se) {
                    log.info("Socket has been closed: {}", se.getMessage());
                    close();

                } catch (Exception e) {
                    log.error(e.getMessage());
                }
            }
        });
    }

    public void sendMissionData(List<DataPoint> dataPoints) {
        final byte[] message = DataMapper.toNetworkMessage(dataPoints);
        this.indoxMessageBuffer.add(message);

        log.debug("Sending Mission Data: {}", dataPoints);
    }

    public void sendCommand(int commandCode) {
        final byte[] message = DataMapper.toNetworkMessage(commandCode);
        this.indoxMessageBuffer.add(message);

        log.debug("Sending Command Code: {} For Drone ID {}", commandCode, droneId);
    }

    public DroneInfo getDroneLastStatus() {
        if (isMaxWaitTimeExceeded()) {
			log.warn("Maximum Wait Time for Drone ID {} exceeded. Control socket closed", droneId);
			close();
		}
		return this.lastStatus;
    }

    private boolean isMaxWaitTimeExceeded() {
        return System.currentTimeMillis() - lastUpdatetime > MAX_WAIT_TIME;
    }

    private void close() {
        try{
            droneSocket.close();
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        try{
            streamIn.close();
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        try{
            streamout.close();
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        manager.removeControlHandlerForDroneId(droneId);
        handlerExecutor.shutdownNow();
    }



   

 

}
