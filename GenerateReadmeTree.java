import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;

/**
 * 自动生成 Markdown 仓库的目录结构树，并更新 README.md。
 * 只遍历一级目录和文件，不深入子目录内部（保持简洁）。
 *
 * 使用方法：
 *     cd D:/programme/GitHub/Markdown
 *     javac GenerateReadmeTree.java
 *     java GenerateReadmeTree
 */
public class GenerateReadmeTree {

    // 要排除的文件/目录
    private static final Set<String> EXCLUDE = Set.of(
            "assets", ".git", ".DS_Store",
            "README.md",
            "generate_readme_tree.py",
            "GenerateReadmeTree.java", "GenerateReadmeTree.class",
            ".obsidian", ".gitignore", "Thumbs.db"
    );

    public static void main(String[] args) throws Exception {
        Path root = Paths.get("").toAbsolutePath();
        Path readmePath = root.resolve("README.md");

        String treeBlock = buildTreeBlock(root);

        if (Files.exists(readmePath)) {
            String content = Files.readString(readmePath, StandardCharsets.UTF_8);
            // 替换 ## 目录结构 到下一个 ## 或 --- 之间的 ```text ... ``` 块
            Pattern pattern = Pattern.compile("(## 目录结构\\s*\\n)```text[\\s\\S]*?```");
            Matcher matcher = pattern.matcher(content);
            String newContent = matcher.replaceFirst("$1" + treeBlock);
            Files.writeString(readmePath, newContent, StandardCharsets.UTF_8);
            System.out.println("README.md 已更新目录结构。");
        } else {
            System.out.println("未找到 README.md，请先创建。");
        }
    }

    private static String buildTreeBlock(Path root) throws IOException {
        List<Path> entries = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(root)) {
            for (Path p : stream) {
                if (!EXCLUDE.contains(p.getFileName().toString())) {
                    entries.add(p);
                }
            }
        }

        // 排序：目录在前，文件在后；同类型按名称忽略大小写排序
        entries.sort((a, b) -> {
            boolean aIsDir = Files.isDirectory(a);
            boolean bIsDir = Files.isDirectory(b);
            if (aIsDir && !bIsDir) return -1;
            if (!aIsDir && bIsDir) return 1;
            return a.getFileName().toString().compareToIgnoreCase(b.getFileName().toString());
        });

        StringBuilder sb = new StringBuilder();
        sb.append("```text\nMarkdown/\n");
        for (int i = 0; i < entries.size(); i++) {
            boolean isLast = (i == entries.size() - 1);
            String prefix = isLast ? "└── " : "├── ";
            String name = entries.get(i).getFileName().toString();
            if (Files.isDirectory(entries.get(i))) {
                name += "/";
            }
            sb.append(prefix).append(name).append("\n");
        }
        sb.append("```");
        return sb.toString();
    }
}
