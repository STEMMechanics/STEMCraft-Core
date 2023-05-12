import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ResourcePack extends JavaPlugin {

    private static final String RESOURCE_PACK_NAME = "CustomResourcePack";
    private static final String RESOURCE_PACK_DESCRIPTION = "Custom resource pack for the server.";
    private static final int RESOURCE_PACK_PORT = 8000;

    private File resourcePackFile;
    private String resourcePackHash;
    private File tempFolder;
    private File serverDirectory;
    private File serverResourcePacksDirectory;

    @Override
    public void onEnable() {
        serverDirectory = getDataFolder().getParentFile();
        serverResourcePacksDirectory = new File(serverDirectory, "resourcepacks");
        tempFolder = new File(serverDirectory, "resourcepack_temp");

        if (!serverResourcePacksDirectory.exists()) {
            serverResourcePacksDirectory.mkdirs();
        }

        generateResourcePack();
        setupHttpServer();
    }

    @Override
    public void onDisable() {
        cleanup();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("resourcepack")) {
            sender.sendMessage("Downloading custom resource pack...");
            sendResourcePack(sender);
            return true;
        }
        return false;
    }

    private void cleanup() {
        if (tempFolder != null && tempFolder.exists()) {
            deleteFolder(tempFolder);
        }
    }

    private void generateResourcePack() {
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    // Create temporary folder
                    tempFolder.mkdir();

                    // Generate resource pack files in the temporary folder
                    // Add any custom files or modify this section as needed
                    // For example, you can copy custom textures, sounds, or other assets

                    // Compress the temporary folder into a ZIP file
                    Path zipFilePath = tempFolder.toPath().resolve(RESOURCE_PACK_NAME + ".zip");
                    zipFolder(tempFolder, zipFilePath);

                    // Set the resource pack file
                    resourcePackFile = zipFilePath.toFile();

                    // Calculate the resource pack hash
                    resourcePackHash = calculateResourcePackHash(resourcePackFile);

                    // Move the resource pack file to the server resource packs directory
                    File targetFile = new File(serverResourcePacksDirectory, resourcePackFile.getName());
                    Files.move(resourcePackFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                    // Set the resource pack file with the new path
                    resourcePackFile = targetFile;

                    getLogger().info("Resource pack generated successfully.");
                } catch (IOException e) {
                    getLogger().warning("Failed to generate resource pack: " + e.getMessage());
                }
            }
        }.runTaskAsynchronously(this);
    }

    private void zipFolder(File sourceFolder, Path zipFilePath) throws IOException {
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(zipFilePath.toFile()))) {
            zipFile(sourceFolder, sourceFolder.getName(), zipOutputStream);
        }
    }

    private void zipFile(File file, String fileName, ZipOutputStream zipOutputStream) throws 
    IOException {
    if (file.isDirectory()) {
    File[] files = file.listFiles();
    if (files != null) {
    for (File childFile : files) {
    zipFile(childFile, fileName + File.separator + childFile.getName(), zipOutputStream);
    }
    }
    } else {
    byte[] buffer = new byte[1024];
    try (FileInputStream fileInputStream = new FileInputStream(file)) {
    zipOutputStream.putNextEntry(new ZipEntry(fileName));
    int length;
    while ((length = fileInputStream.read(buffer)) > 0) {
    zipOutputStream.write(buffer, 0, length);
    }
    zipOutputStream.closeEntry();
    }
    }
    }

    private String calculateResourcePackHash(File resourcePackFile) throws IOException {
    try (FileInputStream fis = new FileInputStream(resourcePackFile)) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            getLogger().warning("Failed to calculate resource pack hash: " + e.getMessage());
            return null;
        }

        byte[] buffer = new byte[8192];
        int bytesRead;
        while ((bytesRead = fis.read(buffer)) != -1) {
            digest.update(buffer, 0, bytesRead);
        }

        byte[] hashBytes = digest.digest();
        StringBuilder sb = new StringBuilder();
        for (byte hashByte : hashBytes) {
            sb.append(Integer.toString((hashByte & 0xff) + 0x100, 16).substring(1));
        }

        return sb.toString();
    }
}

private void deleteFolder(File folder) {
    if (folder.isDirectory()) {
        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                deleteFolder(file);
            }
        }
    }
    folder.delete();
}

private void sendResourcePack(CommandSender sender) {
    if (sender instanceof Player) {
        Player player = (Player) sender;
        if (resourcePackFile != null && resourcePackFile.exists()) {
            String resourcePackURL = "http://" + Bukkit.getServer().getIp() + ":" + RESOURCE_PACK_PORT + "/" + resourcePackFile.getName();
            player.setResourcePack(resourcePackURL, resourcePackHash);
        } else {
            player.sendMessage("The resource pack is not available at the moment. Please try again later.");
        }
    } else {
        sender.sendMessage("This command can only be executed by players.");
    }
}

private void setupHttpServer() {
    try {
        com.sun.net.httpserver.HttpServer server = com.sun.net.httpserver.HttpServer.create(new InetSocketAddress(RESOURCE_PACK_PORT), 0);
        server.createContext("/", exchange -> {
            String filePath = exchange.getRequestURI().getPath().substring(1); // Remove leading slash
            File file = new File(serverResourcePacksDirectory, filePath);
            if (file.exists() && file.isFile()) {
                exchange.sendResponseHeaders(200, file.length());
                try (InputStream inputStream = new FileInputStream(file);
                     OutputStream outputStream = exchange.getResponseBody()) {
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                }
            } else {
                exchange.sendResponseHeaders(404, 0);
            }
            exchange.close();
        });
        server.setExecutor(null);
        server.start();
    } catch (IOException e) {
        getLogger().warning("Failed to start HTTP server: " + e.getMessage());
    }
}
}
