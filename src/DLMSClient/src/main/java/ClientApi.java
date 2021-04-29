import go.javaappnet.Javaappnet;

import java.io.EOFException;
import java.time.Duration;
import java.time.Instant;

import static spark.Spark.*;

public class ClientApi {
    private static String serverAddr = null;
    private static String EncrKey = "000102030405060708090a0b0c0d0e0f";
    private static int port = 5999;
    private static long consecTimeout = 0;
    private static long requestNum = 0;
    private static long timeouts = 0;
    private static ScionSmartMeterClient meter = null;
    public static void main(String[] args) {

        Object mutex = new Object();
        get("/connect/:serverAdd", (request, response) -> {
            try{
                meter = new ScionSmartMeterClient();
                StringBuilder sb = new StringBuilder();
                synchronized (mutex) {
                    serverAddr = request.params(":serverAdd");
                    meter.init(serverAddr, port, EncrKey);
                    String[] status = meter.initConnect();
                    sb.append(String.format("{\"Power\":\"%s\",", status[0]));
                    sb.append(String.format("\"Message\":\"%s\"}", status[1]));
                }
                return sb;
            }catch (Exception e){
                return "Error";
            }
        });

        get("/power/:connection", (request, response) -> {
            if(meter != null) {
                try {
                    StringBuilder sb = new StringBuilder();
                    synchronized (mutex) {
                        if (request.params(":connection").equals("connect")) {
                            meter.setMeterDisconnect(false);
                            return "{\"Power\":\"Connected\"}";
                        }else{
                            meter.setMeterDisconnect(true);
                            return "{\"Power\":\"Disconnected\"}";
                        }
                    }

                } catch (Exception e) {
                    return "{\"status\":\"error\"}";
                }
            }
            return "{\"status\":\"meter not connected\"}";
        });

        get("/message/:message", (request, response) -> {
            if(meter != null) {
                try {
                    StringBuilder sb = new StringBuilder();
                    synchronized (mutex) {
                       meter.setMessage(request.params(":message"));
                    }
                    return String.format("{\"Message\":\"%s\"}",request.params(":message"));
                } catch (Exception e) {
                    return "{\"status\":\"error\"}";
                }
            }
            return "{\"status\":\"meter not connected\"}";
        });

        get("/readmeter", (request, response) -> {
            if(meter != null) {
                try {
                    StringBuilder sb = new StringBuilder();
                    synchronized (mutex) {
                        Instant start = Instant.now();
                        meter.readMeter();
                        Instant end = Instant.now();
                        Duration timeElapsed = Duration.between(start, end);
                        consecTimeout = 0;
                        requestNum++;
                        double latency = timeElapsed.toMillis();
                        System.out.printf("Request Number: %d Timeouts: %d Latency: %f\n",requestNum,timeouts,latency);
//                        System.out.println("Time taken: "+ timeElapsed.toMillis() +" milliseconds");
                    }
                    return meter.getMeterData().toJsonString();
                }
                catch (Exception e) {
                    e.printStackTrace();
                    if(serverAddr != null && consecTimeout>3){
                        consecTimeout = 0;
                        try{
                            synchronized (mutex) {
                                meter.init(serverAddr, port, EncrKey);
                            }
                        }catch (Exception e2){
                            e2.printStackTrace();
                        }
                    }
                    consecTimeout++;
                    timeouts++;
                    return "{\"status\":\"error\"}";
                }
            }
            return "{\"status\":\"meter not connected\"}";
        });
    }
}

