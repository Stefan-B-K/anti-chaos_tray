package space.istef.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.NetworkInterface;
import java.security.MessageDigest;
import java.util.Enumeration;

public class DeviceIdGenerator {
    private static final Logger logger = LoggerFactory.getLogger(DeviceIdGenerator.class);

    public static String generateDeviceId() {
        try {
            String macAddress = getMacAddress();
            String hardwareId = getHardwareId();
            
            String fingerprint = macAddress + "|" + hardwareId;
            
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(fingerprint.getBytes("UTF-8"));
            
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            String deviceId = hexString.toString().substring(0, 32);
            logger.info("Generated Device ID: {}", deviceId);
            logger.info("Fingerprint components - MAC: {}, HardwareID: {}", macAddress, hardwareId);
            
            return deviceId;
        } catch (Exception e) {
            logger.error("Failed to generate device ID", e);
            throw new RuntimeException("Failed to generate device ID", e);
        }
    }

    private static String getMacAddress() throws Exception {
        StringBuilder macAddress = new StringBuilder();
        Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
        
        while (networkInterfaces.hasMoreElements()) {
            NetworkInterface networkInterface = networkInterfaces.nextElement();
            if (networkInterface.isUp() && !networkInterface.isLoopback() && !networkInterface.isVirtual()) {
                byte[] mac = networkInterface.getHardwareAddress();
                if (mac != null) {
                    for (int i = 0; i < mac.length; i++) {
                        macAddress.append(String.format("%02X", mac[i]));
                        if (i < mac.length - 1) {
                            macAddress.append(":");
                        }
                    }
                    break;
                }
            }
        }
        
        if (macAddress.length() == 0) {
            throw new RuntimeException("No valid MAC address found");
        }
        
        return macAddress.toString();
    }

    private static String getHardwareId() throws Exception {
        String os = System.getProperty("os.name").toLowerCase();
        
        if (os.contains("win")) {
            return getWindowsHardwareId();
        } else if (os.contains("linux")) {
            return getLinuxHardwareId();
        } else if (os.contains("mac")) {
            return getMacHardwareId();
        } else {
            return "unknown-" + os;
        }
    }

    private static String getWindowsHardwareId() throws Exception {
        try {
            Process process = Runtime.getRuntime().exec(
                new String[]{"wmic", "baseboard", "get", "serialnumber"}
            );
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream())
            );
            
            String line;
            String serialNumber = "";
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty() && !line.equalsIgnoreCase("SerialNumber")) {
                    serialNumber = line;
                    break;
                }
            }
            
            process.waitFor();
            
            if (serialNumber.isEmpty()) {
                // Fallback to motherboard product
                process = Runtime.getRuntime().exec(
                    new String[]{"wmic", "baseboard", "get", "product"}
                );
                reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (!line.isEmpty() && !line.equalsIgnoreCase("Product")) {
                        serialNumber = line;
                        break;
                    }
                }
                process.waitFor();
            }
            
            return serialNumber.isEmpty() ? "unknown-windows" : serialNumber;
        } catch (Exception e) {
            logger.warn("Failed to get Windows hardware ID via WMI, using fallback", e);
            return "unknown-windows";
        }
    }

    private static String getLinuxHardwareId() throws Exception {
        try {
            Process process = Runtime.getRuntime().exec(new String[]{"cat", "/etc/machine-id"});
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream())
            );
            
            String machineId = reader.readLine();
            process.waitFor();
            
            if (machineId != null && !machineId.trim().isEmpty()) {
                return machineId.trim();
            }
            
            // Fallback to /var/lib/dbus/machine-id
            process = Runtime.getRuntime().exec(new String[]{"cat", "/var/lib/dbus/machine-id"});
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            machineId = reader.readLine();
            process.waitFor();
            
            if (machineId != null && !machineId.trim().isEmpty()) {
                return machineId.trim();
            }
            
            return "unknown-linux";
        } catch (Exception e) {
            logger.warn("Failed to get Linux hardware ID, using fallback", e);
            return "unknown-linux";
        }
    }

    private static String getMacHardwareId() throws Exception {
        try {
            Process process = Runtime.getRuntime().exec(new String[]{
                "ioreg", "-rd1", "-c", "IOPlatformExpertDevice"
            });
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream())
            );
            
            String line;
            String uuid = "";
            while ((line = reader.readLine()) != null) {
                if (line.contains("IOPlatformUUID")) {
                    String[] parts = line.split("=");
                    if (parts.length > 1) {
                        uuid = parts[1].trim().replaceAll("\"", "");
                        break;
                    }
                }
            }
            
            process.waitFor();
            
            return uuid.isEmpty() ? "unknown-macos" : uuid;
        } catch (Exception e) {
            logger.warn("Failed to get macOS hardware ID, using fallback", e);
            return "unknown-macos";
        }
    }
}
