package cc.unitmesh.mcp.example;

import cc.unitmesh.mcp.annotation.MCPTool;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Example service demonstrating the new MCP SDK usage.
 * Shows how to use @MCPTool annotation with various configurations.
 * 
 * @author AutoDev MCP Team
 */
@Service
public class FileService {
    
    @MCPTool(
        name = "read_file",
        description = "Read the contents of a text file",
        category = "file",
        version = "1.0",
        tags = {"file", "read", "io"},
        timeoutMs = 5000,
        cacheable = true,
        cacheTtlSeconds = 60
    )
    @Tool(description = "Read the contents of a text file")
    public String readFile(String filePath) {
        try {
            Path path = Paths.get(filePath);
            if (!Files.exists(path)) {
                return "File not found: " + filePath;
            }
            if (!Files.isReadable(path)) {
                return "File is not readable: " + filePath;
            }
            return Files.readString(path);
        } catch (IOException e) {
            return "Error reading file: " + e.getMessage();
        }
    }
    
    @MCPTool(
        name = "list_directory",
        description = "List files and directories in the specified path",
        category = "file",
        version = "1.0",
        tags = {"file", "directory", "list", "io"},
        timeoutMs = 3000,
        cacheable = true,
        cacheTtlSeconds = 30
    )
    @Tool(description = "List files and directories in the specified path")
    public List<String> listDirectory(String directoryPath) {
        try {
            Path path = Paths.get(directoryPath);
            if (!Files.exists(path)) {
                return List.of("Directory not found: " + directoryPath);
            }
            if (!Files.isDirectory(path)) {
                return List.of("Path is not a directory: " + directoryPath);
            }
            
            return Files.list(path)
                .map(p -> p.getFileName().toString())
                .sorted()
                .collect(Collectors.toList());
        } catch (IOException e) {
            return List.of("Error listing directory: " + e.getMessage());
        }
    }
    
    @MCPTool(
        name = "get_file_info",
        description = "Get information about a file or directory (size, last modified, etc.)",
        category = "file",
        version = "1.0",
        tags = {"file", "info", "metadata", "io"},
        priority = 1,
        cacheable = true,
        cacheTtlSeconds = 120
    )
    @Tool(description = "Get information about a file or directory (size, last modified, etc.)")
    public String getFileInfo(String filePath) {
        try {
            Path path = Paths.get(filePath);
            if (!Files.exists(path)) {
                return "File or directory not found: " + filePath;
            }
            
            StringBuilder info = new StringBuilder();
            info.append("Path: ").append(filePath).append("\n");
            info.append("Type: ").append(Files.isDirectory(path) ? "Directory" : "File").append("\n");
            info.append("Size: ").append(Files.size(path)).append(" bytes\n");
            info.append("Last Modified: ").append(Files.getLastModifiedTime(path)).append("\n");
            info.append("Readable: ").append(Files.isReadable(path)).append("\n");
            info.append("Writable: ").append(Files.isWritable(path)).append("\n");
            info.append("Executable: ").append(Files.isExecutable(path)).append("\n");
            
            return info.toString();
        } catch (IOException e) {
            return "Error getting file info: " + e.getMessage();
        }
    }
    
    @MCPTool(
        name = "write_file",
        description = "Write content to a text file (creates or overwrites)",
        category = "file",
        version = "1.0",
        tags = {"file", "write", "io", "create"},
        requiresAuth = true,
        timeoutMs = 10000,
        priority = 2
    )
    @Tool(description = "Write content to a text file (creates or overwrites)")
    public String writeFile(String filePath, String content) {
        try {
            Path path = Paths.get(filePath);
            Files.writeString(path, content);
            return "Successfully wrote to file: " + filePath;
        } catch (IOException e) {
            return "Error writing file: " + e.getMessage();
        }
    }
}
