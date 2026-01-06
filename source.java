import java.awt.Desktop;
import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.*;
import java.util.stream.*;

class HotspotDetector {

    private static final String[] Variables = {
        "LAPTOP-KPU3L0OC"
    };
    
    private static final String[] HOTSPOT_NAME_PATTERNS = {
        "Android", "iPhone", "iPad", "Galaxy", "Pixel", "OnePlus",
        "Xiaomi", "Huawei", "Oppo", "Vivo", "Realme", "Nokia",
        "DIRECT-", "SM-[A-Z0-9]", "GT-[A-Z0-9]", "Redmi", "Mi ",
        "'s iPhone", "'s Galaxy", "'s Pixel", "'s Android"
    };
    
    private static final Map<String, String> MOBILE_OUIS = new HashMap<>() {{
        // Mobile
        put("00:50:5", "Samsung");
        put("00:25:BC", "Apple");
        put("00:26:B", "Apple");
        put("A8:66:7", "Google Pixel");
        put("F0:D1:A", "Google");
        put("5C:8D:4", "Xiaomi");
        put("F8:A4:5", "OnePlus");
        put("DC:44:B", "Huawei");
        put("B0:B9:8", "Samsung Galaxy");
        
        // Apple 
        put("00:03:93", "Apple");
        put("00:05:02", "Apple");
        put("00:0A:27", "Apple");
        put("00:0A:95", "Apple");
        put("00:0D:93", "Apple");
        put("00:10:FA", "Apple");
        put("00:11:24", "Apple");
        put("00:14:51", "Apple");
        put("00:16:CB", "Apple");
        put("00:17:F2", "Apple");
        put("00:19:E3", "Apple");
        put("00:1B:63", "Apple");
        put("00:1C:B3", "Apple");
        put("00:1D:4F", "Apple");
        put("00:1E:52", "Apple");
        put("00:1E:C2", "Apple");
        put("00:1F:5B", "Apple");
        put("00:1F:F3", "Apple");
        put("00:21:E9", "Apple");
        put("00:22:41", "Apple");
        put("00:23:12", "Apple");
        put("00:23:32", "Apple");
        put("00:23:6C", "Apple");
        put("00:23:DF", "Apple");
        put("00:24:36", "Apple");
        put("00:25:00", "Apple");
        put("00:26:08", "Apple");
        put("00:26:4A", "Apple");
        put("00:26:BB", "Apple");
        put("04:0C:CE", "Apple");
        put("04:15:52", "Apple");
        put("04:26:65", "Apple");
        put("04:48:9A", "Apple");
        put("04:4B:ED", "Apple");
        put("04:54:53", "Apple");
        put("08:00:07", "Apple");
        put("08:66:98", "Apple");
        put("08:70:45", "Apple");
        put("0C:3E:9F", "Apple");
        put("0C:4D:E9", "Apple");
        put("0C:74:C2", "Apple");
        put("10:41:7F", "Apple");
        put("10:9A:DD", "Apple");
        put("10:DD:B1", "Apple");
        put("14:10:9F", "Apple");
        put("14:5A:05", "Apple");
        put("14:8F:C6", "Apple");
        put("18:34:51", "Apple");
        put("18:3D:A2", "Apple");
        put("18:AF:61", "Apple");
        put("18:E7:F4", "Apple");
        put("1C:AB:A7", "Apple");
        put("20:3C:AE", "Apple");
        put("20:AB:37", "Apple");
        put("20:C9:D0", "Apple");
        put("24:A0:74", "Apple");
        put("24:AB:81", "Apple");
        put("28:37:37", "Apple");
        put("28:6A:BA", "Apple");
        put("28:A0:2B", "Apple");
        put("28:E1:4C", "Apple");
        put("28:E7:CF", "Apple");
        put("2C:BE:08", "Apple");
        put("2C:F0:A2", "Apple");
        put("30:10:E4", "Apple");
        put("30:35:AD", "Apple");
        put("34:12:F9", "Apple");
        put("34:15:9E", "Apple");
        put("34:36:3B", "Apple");
        put("38:0F:4A", "Apple");
        put("38:48:4C", "Apple");
        put("38:C9:86", "Apple");
        put("3C:15:C2", "Apple");
        put("40:30:04", "Apple");
        put("40:33:1A", "Apple");
        put("40:B3:95", "Apple");
        put("40:D3:2D", "Apple");
        put("44:2A:60", "Apple");
        put("44:D8:84", "Apple");
        put("48:43:7C", "Apple");
        put("4C:57:CA", "Apple");
        put("4C:74:BF", "Apple");
        put("50:EA:D6", "Apple");
        put("54:26:96", "Apple");
        put("54:72:4F", "Apple");
        put("54:E4:3A", "Apple");
        put("58:55:CA", "Apple");
        put("58:B0:35", "Apple");
        put("5C:59:48", "Apple");
        put("5C:95:AE", "Apple");
        put("5C:F9:38", "Apple");
        put("60:03:08", "Apple");
        put("60:33:4B", "Apple");
        put("60:69:44", "Apple");
        put("60:C5:47", "Apple");
        put("60:F8:1D", "Apple");
        put("60:FA:CD", "Apple");
        put("60:FB:42", "Apple");
        put("64:20:0C", "Apple");
        put("64:9A:BE", "Apple");
        put("64:A3:CB", "Apple");
        put("64:B0:A6", "Apple");
        put("64:E6:82", "Apple");
        put("68:5B:35", "Apple");
        put("68:96:7B", "Apple");
        put("68:A8:6D", "Apple");
        put("68:D9:3C", "Apple");
        put("6C:19:C0", "Apple");
        put("6C:3E:6D", "Apple");
        put("6C:40:08", "Apple");
        put("6C:4D:73", "Apple");
        put("6C:70:9F", "Apple");
        put("6C:72:E7", "Apple");
        put("6C:94:66", "Apple");
        put("70:11:24", "Apple");
        put("70:3E:AC", "Apple");
        put("70:56:81", "Apple");
        put("70:CD:60", "Apple");
        put("70:DE:E2", "Apple");
        put("70:EC:E4", "Apple");
        put("74:1B:B2", "Apple");
        put("74:E1:B6", "Apple");
        put("74:E2:F5", "Apple");
        put("78:31:C1", "Apple");
        put("78:7B:8A", "Apple");
        put("78:A3:E4", "Apple");
        put("78:CA:39", "Apple");
        put("78:D7:5F", "Apple");
        put("78:FD:94", "Apple");
        put("7C:01:91", "Apple");
        put("7C:04:D0", "Apple");
        put("7C:11:BE", "Apple");
        put("7C:6D:62", "Apple");
        put("7C:C3:A1", "Apple");
        put("7C:D1:C3", "Apple");
        put("7C:F0:5F", "Apple");
        put("80:49:71", "Apple");
        put("80:92:9F", "Apple");
        put("80:BE:05", "Apple");
        put("80:E6:50", "Apple");
        put("84:38:35", "Apple");
        put("84:78:8B", "Apple");
        put("84:85:06", "Apple");
        put("84:89:AD", "Apple");
        put("84:8E:0C", "Apple");
        put("84:FC:FE", "Apple");
        put("88:1F:A1", "Apple");
        put("88:53:D4", "Apple");
        put("88:63:DF", "Apple");
        put("88:66:5A", "Apple");
        put("88:E8:7F", "Apple");
        put("8C:00:6D", "Apple");
        put("8C:29:37", "Apple");
        put("8C:2D:AA", "Apple");
        put("8C:58:77", "Apple");
        put("8C:7C:92", "Apple");
        put("8C:85:90", "Apple");
        put("8C:8E:F2", "Apple");
        put("90:27:E4", "Apple");
        put("90:72:40", "Apple");
        put("90:8D:6C", "Apple");
        put("90:B0:ED", "Apple");
        put("90:B2:1F", "Apple");
        put("94:E9:6A", "Apple");
        put("98:01:A7", "Apple");
        put("98:5A:EB", "Apple");
        put("98:B8:E3", "Apple");
        put("98:D6:BB", "Apple");
        put("98:FE:94", "Apple");
        put("9C:04:EB", "Apple");
        put("9C:20:7B", "Apple");
        put("9C:35:EB", "Apple");
        put("9C:84:BF", "Apple");
        put("9C:F3:87", "Apple");
        put("A0:99:9B", "Apple");
        put("A0:D7:95", "Apple");
        put("A4:4E:31", "Apple");
        put("A4:5E:60", "Apple");
        put("A4:B1:97", "Apple");
        put("A4:C3:61", "Apple");
        put("A4:D1:8C", "Apple");
        put("A4:D9:31", "Apple");
        put("A8:20:66", "Apple");
        put("A8:5C:2C", "Apple");
        put("A8:88:08", "Apple");
        put("A8:96:8A", "Apple");
        put("A8:BB:CF", "Apple");
        put("AC:1F:74", "Apple");
        put("AC:29:3A", "Apple");
        put("AC:3C:0B", "Apple");
        put("AC:61:EA", "Apple");
        put("AC:87:A3", "Apple");
        put("AC:BC:32", "Apple");
        put("AC:CF:5C", "Apple");
        put("B0:34:95", "Apple");
        put("B0:65:BD", "Apple");
        put("B0:70:2D", "Apple");
        put("B4:18:D1", "Apple");
        put("B4:8B:19", "Apple");
        put("B4:F0:AB", "Apple");
        put("B4:F6:1C", "Apple");
        put("B8:09:8A", "Apple");
        put("B8:17:C2", "Apple");
        put("B8:41:A4", "Apple");
        put("B8:44:D9", "Apple");
        put("B8:53:AC", "Apple");
        put("B8:5D:0A", "Apple");
        put("B8:63:4D", "Apple");
        put("B8:78:2E", "Apple");
        put("B8:C1:11", "Apple");
        put("B8:E8:56", "Apple");
        put("B8:F6:B1", "Apple");
        put("BC:3B:AF", "Apple");
        put("BC:52:B7", "Apple");
        put("BC:54:2F", "Apple");
        put("BC:67:1C", "Apple");
        put("BC:6C:21", "Apple");
        put("BC:92:6B", "Apple");
        put("BC:9F:EF", "Apple");
        put("C0:1A:DA", "Apple");
        put("C0:63:94", "Apple");
        put("C0:84:7D", "Apple");
        put("C0:9F:42", "Apple");
        put("C0:CC:F8", "Apple");
        put("C0:CE:CD", "Apple");
        put("C0:D0:12", "Apple");
        put("C4:2C:03", "Apple");
        put("C4:61:8B", "Apple");
        put("C4:B3:01", "Apple");
        put("C8:2A:14", "Apple");
        put("C8:33:4B", "Apple");
        put("C8:69:CD", "Apple");
        put("C8:85:50", "Apple");
        put("C8:B5:B7", "Apple");
        put("C8:BC:C8", "Apple");
        put("C8:D0:83", "Apple");
        put("C8:E0:EB", "Apple");
        put("CC:08:8D", "Apple");
        put("CC:20:E8", "Apple");
        put("CC:25:EF", "Apple");
        put("CC:29:F5", "Apple");
        put("CC:78:5F", "Apple");
        put("D0:03:4B", "Apple");
        put("D0:23:DB", "Apple");
        put("D0:33:11", "Apple");
        put("D0:81:7A", "Apple");
        put("D0:A6:37", "Apple");
        put("D0:C5:F3", "Apple");
        put("D0:D2:B0", "Apple");
        put("D0:E1:40", "Apple");
        put("D4:20:6D", "Apple");
        put("D4:61:9D", "Apple");
        put("D4:9A:20", "Apple");
        put("D4:A3:3D", "Apple");
        put("D4:DC:CD", "Apple");
        put("D4:F4:6F", "Apple");
        put("D8:00:4D", "Apple");
        put("D8:1D:72", "Apple");
        put("D8:30:62", "Apple");
        put("D8:96:95", "Apple");
        put("D8:9E:3F", "Apple");
        put("D8:A2:5E", "Apple");
        put("D8:BB:2C", "Apple");
        put("DC:0C:5C", "Apple");
        put("DC:2B:2A", "Apple");
        put("DC:2B:61", "Apple");
        put("DC:37:18", "Apple");
        put("DC:3F:E6", "Apple");
        put("DC:56:E7", "Apple");
        put("DC:86:D8", "Apple");
        put("DC:9B:9C", "Apple");
        put("E0:05:C5", "Apple");
        put("E0:66:78", "Apple");
        put("E0:91:F5", "Apple");
        put("E0:AC:CB", "Apple");
        put("E0:B5:2D", "Apple");
        put("E0:B9:BA", "Apple");
        put("E0:C7:67", "Apple");
        put("E0:F5:C6", "Apple");
        put("E0:F8:47", "Apple");
        put("E4:25:E7", "Apple");
        put("E4:8B:7F", "Apple");
        put("E4:9A:79", "Apple");
        put("E4:CE:8F", "Apple");
        put("E8:04:0B", "Apple");
        put("E8:06:88", "Apple");
        put("E8:80:2E", "Apple");
        put("E8:8D:28", "Apple");
        put("EC:35:86", "Apple");
        put("EC:85:2F", "Apple");
        put("F0:18:98", "Apple");
        put("F0:24:75", "Apple");
        put("F0:4F:7C", "Apple");
        put("F0:98:9D", "Apple");
        put("F0:99:B6", "Apple");
        put("F0:B4:79", "Apple");
        put("F0:C1:F1", "Apple");
        put("F0:D1:A9", "Apple");
        put("F0:DB:E2", "Apple");
        put("F0:DC:E2", "Apple");
        put("F4:0F:24", "Apple");
        put("F4:1B:A1", "Apple");
        put("F4:37:B7", "Apple");
        put("F4:5C:89", "Apple");
        put("F4:F1:5A", "Apple");
        put("F4:F9:51", "Apple");
        put("F8:1E:DF", "Apple");
        put("F8:27:93", "Apple");
        put("F8:95:C7", "Apple");
        put("FC:25:3F", "Apple");
        put("FC:E9:98", "Apple");
        put("FC:FC:48", "Apple");
        
        // Microsoft/Windows device OUIs 
        put("00:15:5D", "Microsoft");
        put("00:03:FF", "Microsoft");
        put("00:12:5A", "Microsoft");
        put("00:17:FA", "Microsoft");
        put("00:50:F2", "Microsoft");
    }};
    
    private static final String[] HOTSPOT_GATEWAYS = {
        "192.168.43.1",   // Android hotspot
        "192.168.137.1",  // Windows PC hotspot
        "192.168.42.1",   // iOS hotspot
        "192.168.49.1",   // iOS hotspot alternate
        "192.168.173.1",  // Windows PC hotspot alternate
        "172.20.10.1",    // iOS USB tethering
        "192.168.2.1"     // macOS Internet Sharing
    };
    
    private List<String> suspiciousActivities = new ArrayList<>();
    private List<String> hotspotIndicators = new ArrayList<>();
    private boolean fakerDetected = false;
    private boolean IsVariable = false;
    private String deviceName = "";
    private String osName;
    private String osType;
    
    public static void main(String[] args) {
        try {
            System.out.println("Starting Hotspot Detector...");
            HotspotDetector detector = new HotspotDetector();
            detector.run();
        } catch (Exception e) {
            System.err.println("FATAL ERROR: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public HotspotDetector() {
        this.osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("win")) {
            osType = "windows";
        } else if (osName.contains("mac")) {
            osType = "macos";
        } else if (osName.contains("nux")) {
            osType = "linux";
        } else {
            osType = "unknown";
        }

        CheckVariables();
    }
    
    private void CheckVariables() {
        try {
            if (osType.equals("windows")) {
                // Get computer name on Windows
                deviceName = System.getenv("COMPUTERNAME");
                if (deviceName == null) {
                    deviceName = InetAddress.getLocalHost().getHostName();
                }
            } else if (osType.equals("macos") || osType.equals("linux")) {
                // Get hostname on Mac/Linux
                deviceName = InetAddress.getLocalHost().getHostName();
            }

            for (String LostVariables : Variables) {
                if (deviceName != null && deviceName.equalsIgnoreCase(LostVariables)) {
                    IsVariable = true;
                    break;
                }
            }
        } catch (Exception e) {
            System.out.println("Warning: Could not determine device name: " + e.getMessage());
            deviceName = "Unknown";
        }
    }
    
    public void run() {
        try {
            printBanner();
            System.out.println("Detected OS: " + osName + " (" + osType + ")");
            System.out.println("Device Name: " + deviceName);

            ConnectionInfo currentConnection = getCurrentConnection();
            List<NetworkProfile> profiles = getNetworkProfiles();
            HostedNetworkInfo hostedNetwork = checkHostedNetwork();
            List<ConnectedDevice> devices = getConnectedDevices();
            
            generateReport(currentConnection, profiles, hostedNetwork, devices);
            printSummary();
        } catch (Exception e) {
            System.err.println("ERROR in run(): " + e.getMessage());
            e.printStackTrace();
        }
    }
    private void printBanner() {
        System.out.println("\n _    _       _          ___     _               ");
        System.out.println("|_|  | |_ ___| |_ ___   |  _|___| |_ ___ ___ ___ ");
        System.out.println("| |  |   | .'|  _| -_|  |  _| .'| '_| -_|  _|_ -|");
        System.out.println("|_|  |_|_|__,|_| |___|  |_| |__,|_,_|___|_| |___|");
        System.out.println("\nSearching for Fakers!\n");
    }
    
    private ConnectionInfo getCurrentConnection() {
        System.out.println("[1/4] Analyzing current connection...");
        
        try {
            switch (osType) {
                case "windows":
                    return getCurrentConnectionWindows();
                case "macos":
                    return getCurrentConnectionMacOS();
                case "linux":
                    return getCurrentConnectionLinux();
                default:
                    System.out.println("  Unsupported OS for connection detection");
                    return null;
            }
        } catch (Exception e) {
            System.out.println("  Error detecting connection: " + e.getMessage());
            return null;
        }
    }
    
    private ConnectionInfo getCurrentConnectionWindows() throws Exception {
        ProcessBuilder pb = new ProcessBuilder("netsh", "wlan", "show", "interfaces");
        pb.redirectErrorStream(true);
        Process process = pb.start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        
        String ssid = null, bssid = null, channel = null, signal = null;
        String line;
        
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (line.startsWith("SSID") && !line.startsWith("BSSID")) {
                String[] parts = line.split(":", 2);
                if (parts.length > 1) {
                    ssid = parts[1].trim();
                }
            } else if (line.startsWith("BSSID")) {
                String[] parts = line.split(":", 2);
                if (parts.length > 1) {
                    bssid = parts[1].trim();
                }
            } else if (line.startsWith("Channel")) {
                String[] parts = line.split(":", 2);
                if (parts.length > 1) {
                    channel = parts[1].trim();
                }
            } else if (line.startsWith("Signal")) {
                String[] parts = line.split(":", 2);
                if (parts.length > 1) {
                    signal = parts[1].trim();
                }
            }
        }
        
        process.waitFor();
        
        if (ssid == null || ssid.isEmpty()) {
            System.out.println("  No WiFi connection detected");
            return null;
        }
        
        ConnectionInfo info = new ConnectionInfo(ssid, bssid, channel, signal);
        analyzeConnection(info);
        
        return info;
    }
    
    private ConnectionInfo getCurrentConnectionMacOS() throws Exception {
        ProcessBuilder pb = new ProcessBuilder("/System/Library/PrivateFrameworks/Apple80211.framework/Versions/Current/Resources/airport", "-I");
        Process process = pb.start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        
        String ssid = null, bssid = null, channel = null;
        String line;
        
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (line.startsWith("SSID:")) {
                ssid = line.split(":", 2)[1].trim();
            } else if (line.startsWith("BSSID:")) {
                bssid = line.split(":", 2)[1].trim();
            } else if (line.startsWith("channel:")) {
                channel = line.split(":", 2)[1].trim();
            }
        }
        
        if (ssid == null) return null;
        
        ConnectionInfo info = new ConnectionInfo(ssid, bssid, channel, "N/A");
        analyzeConnection(info);
        
        return info;
    }
    
    private ConnectionInfo getCurrentConnectionLinux() throws Exception {
        ConnectionInfo info = null;
        
        info = tryIwconfig();
        if (info != null) {
            analyzeConnection(info);
            return info;
        }
        
        info = tryIwCommand();
        if (info != null) {
            analyzeConnection(info);
            return info;
        }
        
        info = tryNmcli();
        if (info != null) {
            analyzeConnection(info);
            return info;
        }
        
        info = tryProcWireless();
        if (info != null) {
            analyzeConnection(info);
            return info;
        }
        
        return null;
    }
    
    private ConnectionInfo tryIwconfig() {
        try {
            ProcessBuilder pb = new ProcessBuilder("iwconfig");
            Process process = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            
            String ssid = null, bssid = null, channel = null, signal = null;
            String currentInterface = null;
            String line;
            
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                
                if (!line.isEmpty() && !line.startsWith(" ")) {
                    currentInterface = line.split("\\s+")[0];
                }
                
                if (line.contains("ESSID:")) {
                    String essidPart = line.substring(line.indexOf("ESSID:") + 6);
                    ssid = essidPart.replaceAll("\"", "").split("\\s+")[0];
                    if (ssid.equals("off/any") || ssid.isEmpty()) {
                        ssid = null;
                    }
                }
                
                if (line.contains("Access Point:")) {
                    String apPart = line.substring(line.indexOf("Access Point:") + 13).trim();
                    bssid = apPart.split("\\s+")[0];
                    if (bssid.equals("Not-Associated")) {
                        bssid = null;
                    }
                }
                
                if (line.contains("Signal level=")) {
                    String sigPart = line.substring(line.indexOf("Signal level=") + 13);
                    signal = sigPart.split("\\s+")[0];
                }
            }
            
            process.waitFor();
            
            if (ssid != null && !ssid.isEmpty()) {
                return new ConnectionInfo(ssid, bssid, channel, signal);
            }
        } catch (Exception e) {
        }
        return null;
    }
    
    private ConnectionInfo tryIwCommand() {
        try {
            ProcessBuilder pb = new ProcessBuilder("iw", "dev");
            Process process = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            
            String wlanInterface = null;
            String line;
            
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("Interface ")) {
                    wlanInterface = line.substring(10).trim();
                    break;
                }
            }
            process.waitFor();
            
            if (wlanInterface == null) return null;
            
            pb = new ProcessBuilder("iw", "dev", wlanInterface, "link");
            process = pb.start();
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            
            String ssid = null, bssid = null, signal = null;
            
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                
                if (line.startsWith("SSID:")) {
                    ssid = line.substring(5).trim();
                }
                
                if (line.startsWith("Connected to ")) {
                    bssid = line.substring(13).split("\\s+")[0];
                }
                
                if (line.contains("signal:")) {
                    String[] parts = line.split("signal:");
                    if (parts.length > 1) {
                        signal = parts[1].trim().split("\\s+")[0];
                    }
                }
            }
            
            process.waitFor();
            
            if (ssid != null && !ssid.isEmpty()) {
                return new ConnectionInfo(ssid, bssid, null, signal);
            }
        } catch (Exception e) {
        }
        return null;
    }
    
    private ConnectionInfo tryNmcli() {
        try {
            ProcessBuilder pb = new ProcessBuilder("nmcli", "-t", "-f", "ACTIVE,SSID,BSSID,CHAN,SIGNAL", "dev", "wifi");
            Process process = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("yes:")) {
                    String[] parts = line.substring(4).split(":");
                    if (parts.length >= 2) {
                        String ssid = parts[0];
                        String bssid = parts.length > 1 ? parts[1] : null;
                        String channel = parts.length > 2 ? parts[2] : null;
                        String signal = parts.length > 3 ? parts[3] : null;
                        
                        return new ConnectionInfo(ssid, bssid, channel, signal);
                    }
                }
            }
            process.waitFor();
        } catch (Exception e) {
        }
        return null;
    }
    
    private ConnectionInfo tryProcWireless() {
        try {
            BufferedReader reader = new BufferedReader(new FileReader("/proc/net/wireless"));
            String line;
            String wlanInterface = null;
            
            reader.readLine();
            reader.readLine();
            
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    wlanInterface = line.split(":")[0].trim();
                    break;
                }
            }
            reader.close();
            
            if (wlanInterface == null) return null;
            
            String operstatePath = "/sys/class/net/" + wlanInterface + "/operstate";
            reader = new BufferedReader(new FileReader(operstatePath));
            String operstate = reader.readLine();
            reader.close();
            
            if (operstate != null && operstate.equals("up")) {
                return new ConnectionInfo("Unknown (WiFi Connected)", null, null, null);
            }
        } catch (Exception e) {
        }
        return null;
    }

    private void analyzeConnection(ConnectionInfo info) {
        if (info == null) return;

        if (IsVariable) {
            System.out.println("  ✓ Connected to: " + info.ssid);
            return;
        }
        
        System.out.println("  Connected to: " + info.ssid);
        if (info.bssid != null) {
            System.out.println("  BSSID: " + info.bssid);
        }
        
        // mac detection
        if (osType.equals("macos")) {
            System.out.println("  [INFO] Running macOS hotspot detection...");
            String gateway = getMacOSGateway();
            
            if (gateway != null) {
                System.out.println("  [INFO] Default gateway: " + gateway);
                
                // check for windows hotspots
                if (gateway.equals("192.168.137.1") || gateway.equals("192.168.173.1")) {
                    info.isHotspot = true;
                    fakerDetected = true;
                    String indicator = "Gateway IP " + gateway + " indicates Windows PC hotspot";
                    info.hotspotIndicators.add(indicator);
                    suspiciousActivities.add("[Connection] " + indicator);
                    hotspotIndicators.add(indicator);
                    System.out.println("  [!] WINDOWS HOTSPOT DETECTED via gateway IP " + gateway + "!");
                }
                
                // Check for Android hotspot
                else if (gateway.equals("192.168.43.1")) {
                    info.isHotspot = true;
                    fakerDetected = true;
                    String indicator = "Gateway IP " + gateway + " indicates Android hotspot";
                    info.hotspotIndicators.add(indicator);
                    suspiciousActivities.add("[Connection] " + indicator);
                    hotspotIndicators.add(indicator);
                    System.out.println("  [!] ANDROID HOTSPOT DETECTED via gateway IP!");
                }
                
                // macOS Internet Sharing
                else if (gateway.equals("192.168.2.1")) {
                    info.isHotspot = true;
                    fakerDetected = true;
                    String indicator = "Gateway IP " + gateway + " indicates macOS Internet Sharing";
                    info.hotspotIndicators.add(indicator);
                    suspiciousActivities.add("[Connection] " + indicator);
                    hotspotIndicators.add(indicator);
                    System.out.println("  [!] MAC INTERNET SHARING DETECTED via gateway IP!");
                }
                
                // iOS tethering
                else if (gateway.equals("172.20.10.1")) {
                    info.isHotspot = true;
                    fakerDetected = true;
                    String indicator = "Gateway IP " + gateway + " indicates iOS/Mac tethering";
                    info.hotspotIndicators.add(indicator);
                    suspiciousActivities.add("[Connection] " + indicator);
                    hotspotIndicators.add(indicator);
                    System.out.println("  [!] IOS/MAC TETHERING DETECTED via gateway IP!");
                }
                
                else if (gateway.matches("192\\.168\\.(42|49)\\.[0-9]+")) {
                    info.isHotspot = true;
                    fakerDetected = true;
                    String indicator = "Gateway IP " + gateway + " indicates iOS hotspot";
                    info.hotspotIndicators.add(indicator);
                    suspiciousActivities.add("[Connection] " + indicator);
                    hotspotIndicators.add(indicator);
                    System.out.println("  [!] IOS HOTSPOT DETECTED via gateway IP!");
                }
            } else {
                System.out.println("  [WARNING] Could not detect gateway IP - some checks will be skipped");
            }
            
            // Check if BSSID is locally administered
            if (info.bssid != null && isLocallyAdministeredMAC(info.bssid)) {
                info.isHotspot = true;
                fakerDetected = true;
                String indicator = "BSSID is locally administered (software AP/Internet Sharing): " + info.bssid;
                info.hotspotIndicators.add(indicator);
                suspiciousActivities.add("[Connection] " + indicator);
                hotspotIndicators.add(indicator);
                System.out.println("  [!] SOFTWARE ACCESS POINT DETECTED via locally administered MAC!");
            }
            
            // Check for Apple device naming patterns
            if (info.ssid.matches(".*'s (iPhone|iPad|MacBook|iMac|Mac).*")) {
                info.isHotspot = true;
                fakerDetected = true;
                String indicator = "SSID matches Apple device naming pattern";
                info.hotspotIndicators.add(indicator);
                suspiciousActivities.add("[Connection] " + indicator);
                hotspotIndicators.add(indicator);
                System.out.println("  [!] APPLE DEVICE HOTSPOT DETECTED via SSID pattern!");
            }
        }
        
        // linux detection
        if (osType.equals("linux")) {
            String gateway = getLinuxGateway();
            if (gateway != null) {
                if (gateway.equals("192.168.137.1") || gateway.equals("192.168.173.1")) {
                    info.isHotspot = true;
                    fakerDetected = true;
                    String indicator = "Gateway IP " + gateway + " indicates Windows PC hotspot";
                    info.hotspotIndicators.add(indicator);
                    suspiciousActivities.add("[Connection] " + indicator);
                    hotspotIndicators.add(indicator);
                    System.out.println("  [!] WINDOWS HOTSPOT DETECTED via gateway IP!");
                }
                
                if (gateway.matches("192\\.168\\.(137|173|42|43)\\.[0-9]+")) {
                    info.isHotspot = true;
                    fakerDetected = true;
                    String indicator = "Suspicious hotspot gateway: " + gateway;
                    info.hotspotIndicators.add(indicator);
                    suspiciousActivities.add("[Connection] " + indicator);
                    hotspotIndicators.add(indicator);
                }
            }
            
            if (info.bssid != null && isLocallyAdministeredMAC(info.bssid)) {
                info.isHotspot = true;
                fakerDetected = true;
                String indicator = "BSSID is locally administered (software AP/hotspot): " + info.bssid;
                info.hotspotIndicators.add(indicator);
                suspiciousActivities.add("[Connection] " + indicator);
                hotspotIndicators.add(indicator);
            }
        }
        
        // Check SSID name patterns
        for (String pattern : HOTSPOT_NAME_PATTERNS) {
            if (info.ssid.matches(".*" + pattern + ".*")) {
                info.isHotspot = true;
                fakerDetected = true;
                String indicator = "SSID matches mobile hotspot pattern: " + pattern;
                info.hotspotIndicators.add(indicator);
                suspiciousActivities.add("[Connection] " + indicator);
                hotspotIndicators.add(indicator);
            }
        }
        
        // Windows PC hotspot SSID patterns
        if (info.ssid.matches("(LAPTOP|DESKTOP|PC)-[A-Z0-9]+")) {
            info.isHotspot = true;
            fakerDetected = true;
            String indicator = "SSID matches Windows PC hotspot naming pattern";
            info.hotspotIndicators.add(indicator);
            suspiciousActivities.add("[Connection] " + indicator);
            hotspotIndicators.add(indicator);
        }
        
        // Check BSSID against mobile manufacturer OUIs
        if (info.bssid != null) {
            for (Map.Entry<String, String> entry : MOBILE_OUIS.entrySet()) {
                if (info.bssid.toUpperCase().startsWith(entry.getKey().toUpperCase())) {
                    info.isHotspot = true;
                    fakerDetected = true;
                    String indicator = "BSSID indicates " + entry.getValue() + " device";
                    info.hotspotIndicators.add(indicator);
                    suspiciousActivities.add("[Connection] " + indicator);
                    hotspotIndicators.add(indicator);
                }
            }
        }
        
        if (info.isHotspot) {
            System.out.println("  [!] HOTSPOT DETECTED!");
            for (String indicator : info.hotspotIndicators) {
                System.out.println("      - " + indicator);
            }
        }
    }
    
    private String getLinuxGateway() {
        try {
            ProcessBuilder pb = new ProcessBuilder("ip", "route", "show", "default");
            Process process = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            
            String line = reader.readLine();
            if (line != null && line.contains("via")) {
                String[] parts = line.split("\\s+");
                for (int i = 0; i < parts.length - 1; i++) {
                    if (parts[i].equals("via")) {
                        return parts[i + 1];
                    }
                }
            }
            process.waitFor();
        } catch (Exception e) {
        }
        return null;
    }
    
    private String getMacOSGateway() {
        System.out.println("  [DEBUG] Starting macOS gateway detection...");
        
        // netstat with awk
        try {
            ProcessBuilder pb = new ProcessBuilder("sh", "-c", "netstat -nr | grep default | awk '{print $2}' | head -1");
            Process process = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String gateway = reader.readLine();
            process.waitFor();
            
            if (gateway != null && gateway.matches("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}")) {
                System.out.println("  [DEBUG] Gateway found: " + gateway);
                return gateway.trim();
            }
        } catch (Exception e) {
            System.out.println("  [DEBUG] netstat method failed: " + e.getMessage());
        }
        
        // route command
        try {
            ProcessBuilder pb = new ProcessBuilder("sh", "-c", "route -n get default | grep gateway | awk '{print $2}'");
            Process process = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String gateway = reader.readLine();
            process.waitFor();
            
            if (gateway != null && gateway.matches("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}")) {
                System.out.println("  [DEBUG] Gateway found via route: " + gateway);
                return gateway.trim();
            }
        } catch (Exception e) {
            System.out.println("  [DEBUG] route method failed: " + e.getMessage());
        }
        
        System.out.println("  [DEBUG] All gateway detection methods failed");
        return null;
    }
    
    private boolean isLocallyAdministeredMAC(String mac) {
        if (mac == null || mac.length() < 2) return false;
        
        try {
            String firstOctet = mac.substring(0, 2).replace(":", "").replace("-", "");
            int octet = Integer.parseInt(firstOctet, 16);
            return (octet & 0x02) != 0;
        } catch (Exception e) {
            return false;
        }
    }
    
    private List<NetworkProfile> getNetworkProfiles() {
        System.out.println("\n[2/4] Scanning network profiles...");
        List<NetworkProfile> profiles = new ArrayList<>();
        
        try {
            switch (osType) {
                case "windows":
                    return getNetworkProfilesWindows();
                case "macos":
                    return getNetworkProfilesMacOS();
                case "linux":
                    return getNetworkProfilesLinux();
                default:
                    System.out.println("  Unsupported OS for profile detection");
                    return profiles;
            }
        } catch (Exception e) {
            System.out.println("  Error scanning profiles: " + e.getMessage());
            return profiles;
        }
    }
    
    private List<NetworkProfile> getNetworkProfilesWindows() throws Exception {
        List<NetworkProfile> profiles = new ArrayList<>();
        
        ProcessBuilder pb = new ProcessBuilder("netsh", "wlan", "show", "profiles");
        pb.redirectErrorStream(true);
        Process process = pb.start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.contains("All User Profile") || line.contains("User Profile")) {
                String[] parts = line.split(":", 2);
                if (parts.length > 1) {
                    String ssid = parts[1].trim();
                    boolean isHotspot = false;

                    if (!IsVariable) {
                        for (String pattern : HOTSPOT_NAME_PATTERNS) {
                            if (ssid.matches(".*" + pattern + ".*")) {
                                isHotspot = true;
                                String activity = "[Profile] Hotspot profile found: " + ssid;
                                suspiciousActivities.add(activity);
                                hotspotIndicators.add(activity);
                                break;
                            }
                        }
                    }
                    
                    profiles.add(new NetworkProfile(ssid, isHotspot));
                }
            }
        }
        
        process.waitFor();
        
        if (IsVariable) {
            System.out.println("  Found " + profiles.size() + " profiles");
        } else {
            long hotspotCount = profiles.stream().filter(p -> p.isHotspot).count();
            System.out.println("  Found " + profiles.size() + " profiles (" + hotspotCount + " hotspot profiles)");
        }
        
        return profiles;
    }
    
    private List<NetworkProfile> getNetworkProfilesMacOS() throws Exception {
        List<NetworkProfile> profiles = new ArrayList<>();
        
        ProcessBuilder pb = new ProcessBuilder("networksetup", "-listpreferredwirelessnetworks", "en0");
        Process process = pb.start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        
        String line;
        reader.readLine();
        
        while ((line = reader.readLine()) != null) {
            String ssid = line.trim();
            if (!ssid.isEmpty()) {
                boolean isHotspot = false;

                if (!IsVariable) {
                    for (String pattern : HOTSPOT_NAME_PATTERNS) {
                        if (ssid.matches(".*" + pattern + ".*")) {
                            isHotspot = true;
                            String activity = "[Profile] Hotspot profile found: " + ssid;
                            suspiciousActivities.add(activity);
                            hotspotIndicators.add(activity);
                            break;
                        }
                    }
                }
                
                profiles.add(new NetworkProfile(ssid, isHotspot));
            }
        }
        
        if (IsVariable) {
            System.out.println("  Found " + profiles.size() + " profiles");
        } else {
            long hotspotCount = profiles.stream().filter(p -> p.isHotspot).count();
            System.out.println("  Found " + profiles.size() + " profiles (" + hotspotCount + " hotspot profiles)");
        }
        
        return profiles;
    }
    
    private List<NetworkProfile> getNetworkProfilesLinux() throws Exception {
        List<NetworkProfile> profiles = new ArrayList<>();
        
        try {
            ProcessBuilder pb = new ProcessBuilder("nmcli", "-t", "-f", "NAME,TYPE", "connection", "show");
            Process process = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length >= 2 && parts[1].contains("wireless")) {
                    String ssid = parts[0];
                    boolean isHotspot = false;

                    if (!IsVariable) {
                        for (String pattern : HOTSPOT_NAME_PATTERNS) {
                            if (ssid.matches(".*" + pattern + ".*")) {
                                isHotspot = true;
                                String activity = "[Profile] Hotspot profile found: " + ssid;
                                suspiciousActivities.add(activity);
                                hotspotIndicators.add(activity);
                                break;
                            }
                        }
                    }
                    
                    profiles.add(new NetworkProfile(ssid, isHotspot));
                }
            }
            
            if (!profiles.isEmpty()) {
                if (IsVariable) {
                    System.out.println("  Found " + profiles.size() + " profiles");
                } else {
                    long hotspotCount = profiles.stream().filter(p -> p.isHotspot).count();
                    System.out.println("  Found " + profiles.size() + " profiles (" + hotspotCount + " hotspot profiles)");
                }
                return profiles;
            }
        } catch (Exception e) {
        }
        
        try {
            File nmConnDir = new File("/etc/NetworkManager/system-connections");
            if (nmConnDir.exists() && nmConnDir.isDirectory()) {
                File[] connFiles = nmConnDir.listFiles();
                if (connFiles != null) {
                    for (File connFile : connFiles) {
                        if (connFile.isFile()) {
                            String ssid = connFile.getName();
                            if (ssid.endsWith(".nmconnection")) {
                                ssid = ssid.substring(0, ssid.length() - 13);
                            }
                            
                            boolean isHotspot = false;
                            if (!IsVariable) {
                                for (String pattern : HOTSPOT_NAME_PATTERNS) {
                                    if (ssid.matches(".*" + pattern + ".*")) {
                                        isHotspot = true;
                                        String activity = "[Profile] Hotspot profile found: " + ssid;
                                        suspiciousActivities.add(activity);
                                        hotspotIndicators.add(activity);
                                        break;
                                    }
                                }
                            }
                            
                            profiles.add(new NetworkProfile(ssid, isHotspot));
                        }
                    }
                }
            }
        } catch (Exception e) {
        }
        
        if (profiles.isEmpty()) {
            try {
                File wpaConfig = new File("/etc/wpa_supplicant/wpa_supplicant.conf");
                if (wpaConfig.exists() && wpaConfig.canRead()) {
                    BufferedReader reader = new BufferedReader(new FileReader(wpaConfig));
                    String line;
                    
                    while ((line = reader.readLine()) != null) {
                        line = line.trim();
                        if (line.startsWith("ssid=")) {
                            String ssid = line.substring(5).replaceAll("\"", "");
                            
                            boolean isHotspot = false;
                            if (!IsVariable) {
                                for (String pattern : HOTSPOT_NAME_PATTERNS) {
                                    if (ssid.matches(".*" + pattern + ".*")) {
                                        isHotspot = true;
                                        String activity = "[Profile] Hotspot profile found: " + ssid;
                                        suspiciousActivities.add(activity);
                                        hotspotIndicators.add(activity);
                                        break;
                                    }
                                }
                            }
                            
                            profiles.add(new NetworkProfile(ssid, isHotspot));
                        }
                    }
                    reader.close();
                }
            } catch (Exception e) {
            }
        }
        
        if (IsVariable) {
            System.out.println("  Found " + profiles.size() + " profiles");
        } else {
            long hotspotCount = profiles.stream().filter(p -> p.isHotspot).count();
            System.out.println("  Found " + profiles.size() + " profiles (" + hotspotCount + " hotspot profiles)");
        }
        
        return profiles;
    }
    
private HostedNetworkInfo checkHostedNetwork() {
    System.out.println("\n[3/4] Checking hosted network status...");
    
    try {
        if (osType.equals("windows")) {
            return checkHostedNetworkWindows();
        } else {
            System.out.println("  Hosted network check only supported on Windows");
            return new HostedNetworkInfo(false, null, 0);
        }
    } catch (Exception e) {
        System.out.println("  Error checking hosted network: " + e.getMessage());
        return new HostedNetworkInfo(false, null, 0);
    }
}

private HostedNetworkInfo checkHostedNetworkWindows() throws Exception {
    ProcessBuilder pb = new ProcessBuilder("netsh", "wlan", "show", "hostednetwork");
    pb.redirectErrorStream(true);
    Process process = pb.start();
    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
    
    String ssid = null;
    boolean isStarted = false;
    int clients = 0;
    String line;
    
    while ((line = reader.readLine()) != null) {
        line = line.trim();
        if (line.startsWith("SSID name")) {
            String[] parts = line.split(":", 2);
            if (parts.length > 1) {
                ssid = parts[1].trim().replace("\"", "");
            }
        } else if (line.startsWith("Status")) {
            String[] parts = line.split(":", 2);
            if (parts.length > 1 && parts[1].trim().equalsIgnoreCase("Started")) {
                isStarted = true;
            }
        } else if (line.startsWith("Number of clients")) {
            String[] parts = line.split(":", 2);
            if (parts.length > 1) {
                try {
                    clients = Integer.parseInt(parts[1].trim());
                } catch (NumberFormatException e) {
                    clients = 0;
                }
            }
        }
    }
    
    process.waitFor();

    if (IsVariable) {
        System.out.println("  Status: Inactive");
        return new HostedNetworkInfo(false, null, 0);
    }
    
    if (isStarted) {
        System.out.println("  [!] ACTIVE HOSTED NETWORK DETECTED!");
        System.out.println("      SSID: " + ssid);
        System.out.println("      Clients: " + clients);
        String activity = "[Hosted Network] Active hosted network: " + ssid + " (" + clients + " clients)";
        suspiciousActivities.add(activity);
        hotspotIndicators.add(activity);
        fakerDetected = true;
    } else {
        System.out.println("  Status: Inactive");
    }
    
    return new HostedNetworkInfo(isStarted, ssid, clients);
}

private List<ConnectedDevice> getConnectedDevices() {
    System.out.println("\n[4/4] Scanning for connected devices...");
    List<ConnectedDevice> devices = new ArrayList<>();
    
    try {
        switch (osType) {
            case "windows":
                return getConnectedDevicesWindows();
            case "macos":
                return getConnectedDevicesMacOS();
            case "linux":
                return getConnectedDevicesLinux();
            default:
                System.out.println("  Unsupported OS for device detection");
                return devices;
        }
    } catch (Exception e) {
        System.out.println("  Error scanning devices: " + e.getMessage());
        return devices;
    }
}

private List<ConnectedDevice> getConnectedDevicesWindows() throws Exception {
    List<ConnectedDevice> devices = new ArrayList<>();
    
    ProcessBuilder pb = new ProcessBuilder("arp", "-a");
    pb.redirectErrorStream(true);
    Process process = pb.start();
    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
    
    String line;
    Pattern pattern = Pattern.compile("\\s+(\\d+\\.\\d+\\.\\d+\\.\\d+)\\s+([0-9a-fA-F-]+)\\s+");
    
    while ((line = reader.readLine()) != null) {
        Matcher matcher = pattern.matcher(line);
        if (matcher.find()) {
            String ip = matcher.group(1);
            String mac = matcher.group(2);
            
            if (mac.startsWith("ff-ff") || mac.startsWith("01-00")) {
                continue;
            }
            
            devices.add(new ConnectedDevice(ip, mac, "Unknown"));
        }
    }
    
    process.waitFor();
    
    System.out.println("  Found " + devices.size() + " devices");
    
    return devices;
}

private List<ConnectedDevice> getConnectedDevicesMacOS() throws Exception {
    List<ConnectedDevice> devices = new ArrayList<>();
    
    ProcessBuilder pb = new ProcessBuilder("arp", "-a");
    Process process = pb.start();
    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
    
    String line;
    Pattern pattern = Pattern.compile("\\(([0-9.]+)\\)\\s+at\\s+([0-9a-f:]+)");
    
    while ((line = reader.readLine()) != null) {
        Matcher matcher = pattern.matcher(line);
        if (matcher.find()) {
            String ip = matcher.group(1);
            String mac = matcher.group(2);
            
            if (!mac.startsWith("ff:ff") && !mac.startsWith("01:00")) {
                devices.add(new ConnectedDevice(ip, mac, "Unknown"));
            }
        }
    }
    
    System.out.println("  Found " + devices.size() + " devices");
    
    return devices;
}

private List<ConnectedDevice> getConnectedDevicesLinux() throws Exception {
    List<ConnectedDevice> devices = new ArrayList<>();
    
    ProcessBuilder pb = new ProcessBuilder("ip", "neigh");
    Process process = pb.start();
    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
    
    String line;
    while ((line = reader.readLine()) != null) {
        String[] parts = line.split("\\s+");
        if (parts.length >= 5) {
            String ip = parts[0];
            String mac = parts[4];
            
            if (mac.matches("[0-9a-f:]+")) {
                devices.add(new ConnectedDevice(ip, mac, "Unknown"));
            }
        }
    }
    
    System.out.println("  Found " + devices.size() + " devices");
    
    return devices;
}

private void generateReport(ConnectionInfo connection, List<NetworkProfile> profiles, 
                           HostedNetworkInfo hosted, List<ConnectedDevice> devices) {
    System.out.println("\nGenerating HTML Report...");
    
    try {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head><title>WiFi Analysis Report</title>");
        html.append("<style>");
        html.append("body { font-family: Arial, sans-serif; margin: 20px; background: #f5f5f5; }");
        html.append("h1 { color: #2c3e50; }");
        html.append("h2 { color: #34495e; margin-top: 30px; }");
        html.append(".warning { background: #e74c3c; color: white; padding: 15px; margin: 10px 0; border-radius: 5px; }");
        html.append(".success { background: #27ae60; color: white; padding: 15px; margin: 10px 0; border-radius: 5px; }");
        html.append(".info { background: #3498db; color: white; padding: 15px; margin: 10px 0; border-radius: 5px; }");
        html.append("table { width: 100%; border-collapse: collapse; margin: 20px 0; background: white; }");
        html.append("th { background: #34495e; color: white; padding: 10px; text-align: left; }");
        html.append("td { padding: 10px; border-bottom: 1px solid #ddd; }");
        html.append(".hotspot-row { background: #ffebee; }");
        html.append("</style></head><body>");
        
        html.append("<h1>Hotspot Detection Report</h1>");
        html.append("<p>Generated: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("</p>");
        html.append("<p>Operating System: ").append(osName).append("</p>");
        html.append("<p>Device Name: ").append(deviceName).append("</p>");

        html.append("<h2>Suspicious Activities</h2>");
        if (IsVariable || suspiciousActivities.isEmpty()) {
            html.append("<div class='success'>No suspicious activities detected</div>");
        } else {
            html.append("<div class='warning'><strong>ALERT: ").append(suspiciousActivities.size())
                .append(" suspicious activity(ies) detected!</strong><ul>");
            for (String activity : suspiciousActivities) {
                html.append("<li>").append(activity).append("</li>");
            }
            html.append("</ul></div>");
        }
        
        if (connection != null) {
            html.append("<h2>Current Connection</h2>");
            if (!IsVariable && connection.isHotspot) {
                html.append("<div class='warning'><strong>[!] CONNECTED TO HOTSPOT: ").append(connection.ssid).append("</strong><br>");
                html.append("BSSID: ").append(connection.bssid != null ? connection.bssid : "N/A").append("<br>");
                html.append("Channel: ").append(connection.channel != null ? connection.channel : "N/A").append(" | Signal: ").append(connection.signal != null ? connection.signal : "N/A").append("<br><br>");
                html.append("<strong>Hotspot Indicators:</strong><ul>");
                for (String indicator : connection.hotspotIndicators) {
                    html.append("<li>").append(indicator).append("</li>");
                }
                html.append("</ul></div>");
            } else {
                html.append("<div class='info'>Connected to: <strong>").append(connection.ssid).append("</strong><br>");
                html.append("BSSID: ").append(connection.bssid != null ? connection.bssid : "N/A").append(" | Channel: ").append(connection.channel != null ? connection.channel : "N/A")
                    .append(" | Signal: ").append(connection.signal != null ? connection.signal : "N/A").append("</div>");
            }
        } else {
            html.append("<h2>Current Connection</h2>");
            html.append("<div class='info'>No active WiFi connection detected</div>");
        }
        
        html.append("<h2>Hosted Network Status</h2>");
        if (!IsVariable && hosted.isActive) {
            html.append("<div class='warning'>ACTIVE - SSID: ").append(hosted.ssid)
                .append(", Clients: ").append(hosted.clients).append("</div>");
        } else {
            html.append("<div class='success'>Inactive</div>");
        }
        
        html.append("<h2>Network Profiles</h2><table><tr><th>SSID</th><th>Type</th></tr>");
        if (profiles.isEmpty()) {
            html.append("<tr><td colspan='2'>No profiles found</td></tr>");
        } else {
            for (NetworkProfile profile : profiles) {
                String rowClass = (!IsVariable && profile.isHotspot) ? " class='hotspot-row'" : "";
                String type = (!IsVariable && profile.isHotspot) ? "HOTSPOT" : "WiFi";
                html.append("<tr").append(rowClass).append("><td>").append(profile.ssid)
                    .append("</td><td>").append(type).append("</td></tr>");
            }
        }
        html.append("</table>");
        
        html.append("<h2>Connected Devices</h2><table><tr><th>IP Address</th><th>MAC Address</th></tr>");
        if (devices.isEmpty()) {
            html.append("<tr><td colspan='2'>No devices found</td></tr>");
        } else {
            for (ConnectedDevice device : devices) {
                html.append("<tr><td>").append(device.ip).append("</td><td>").append(device.mac).append("</td></tr>");
            }
        }
        html.append("</table>");
        
        html.append("</body></html>");
        
        String outputPath = System.getProperty("user.home") + File.separator + "Desktop" + 
                           File.separator + "ConnectionSummary.html";
        
        System.out.println("  Saving to: " + outputPath);
        Files.write(Paths.get(outputPath), html.toString().getBytes("UTF-8"));
        System.out.println("  Report saved successfully!");
        
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(new File(outputPath).toURI());
                System.out.println("  Opening in browser...");
            } else {
                System.out.println("  Desktop not supported, please open manually: " + outputPath);
            }
        } catch (Exception e) {
            System.out.println("  Could not open browser: " + e.getMessage());
            System.out.println("  Please open manually: " + outputPath);
        }
    } catch (Exception e) {
        System.out.println("  ERROR generating report: " + e.getMessage());
        e.printStackTrace();
    }
}

private void printSummary() {
    System.out.println("\n================================");
    System.out.println("SUMMARY:");
    System.out.println("================================");
    
    if (IsVariable) {
        System.out.println("Device Status: Adequate");
        System.out.println("Suspicious Activities: 0");
    } else {
        System.out.println("Suspicious Activities: " + suspiciousActivities.size() + 
            (suspiciousActivities.size() > 0 ? " [!]" : " [OK]"));
    }
    
    System.out.println("Operating System: " + osName);
    System.out.println("Device Name: " + deviceName);
    
    if (!IsVariable && fakerDetected) {
        System.out.println("\n[!!!] FAKER DETECTED! [!!!]");
        System.out.println("This device is connected to a hotspot!");
    }
    
    if (IsVariable) {
        System.out.println("\n[✓]");
        System.out.println("All sources appear clean");
    } else if (!suspiciousActivities.isEmpty()) {
        System.out.println("\nWARNINGS:");
        for (String activity : suspiciousActivities) {
            System.out.println("  - " + activity);
        }
    }
    
    System.out.println("\nReport saved to Desktop/ConnectionSummary.html");
    System.out.println("\nScript complete.");
}

// Inner classes
static class ConnectionInfo {
    String ssid;
    String bssid;
    String channel;
    String signal;
    boolean isHotspot = false;
    List<String> hotspotIndicators = new ArrayList<>();
    
    ConnectionInfo(String ssid, String bssid, String channel, String signal) {
        this.ssid = ssid;
        this.bssid = bssid;
        this.channel = channel;
        this.signal = signal;
    }
}

static class NetworkProfile {
    String ssid;
    boolean isHotspot;
    
    NetworkProfile(String ssid, boolean isHotspot) {
        this.ssid = ssid;
        this.isHotspot = isHotspot;
    }
}

static class HostedNetworkInfo {
    boolean isActive;
    String ssid;
    int clients;
    
    HostedNetworkInfo(boolean isActive, String ssid, int clients) {
        this.isActive = isActive;
        this.ssid = ssid;
        this.clients = clients;
    }
}

static class ConnectedDevice {
    String ip;
    String mac;
    String type;
    
    ConnectedDevice(String ip, String mac, String type) {
        this.ip = ip;
        this.mac = mac;
        this.type = type;
    }
}
}
