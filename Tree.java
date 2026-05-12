import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;

/**
 * 自动生成 Markdown 仓库的目录结构树，并更新 README.md。
 * 自动读取 .gitignore 中的规则，被忽略的文件/目录不会出现在目录树中。
 *
 * 使用方法：
 *     cd D:/programme/GitHub/Markdown
 *     javac GenerateReadmeTree.java
 *     java GenerateReadmeTree
 */
public class GenerateReadmeTree {

    // 硬编码工作目录（在 IDEA 中运行也不会出错）
    private static final String BASE_DIR = "D:/programme/GitHub/Markdown";

    public static void main(String[] args) throws Exception {
        //把这BASE_DIR个路径转成绝对路径
        Path root = Paths.get(BASE_DIR).toAbsolutePath();

        //把括号内的相对路径拼接到 root 后面，形成一个新的完整路径返回。
        Path readmePath = root.resolve("README.md");
        Path gitignorePath = root.resolve(".gitignore");
        
        //读取 .gitignore，返回需要排除的纯文件名集合（快速匹配用）
        Set<String> excludeNames = loadGitignoreNames(gitignorePath);
        System.out.println(excludeNames);

        List<IgnoreRule> ignoreRules = loadGitignoreRules(root, gitignorePath);
//        System.out.println(ignoreRules);

        String treeBlock = buildTreeBlock(root, excludeNames, ignoreRules);

        if (Files.exists(readmePath)) {
            String content = Files.readString(readmePath, StandardCharsets.UTF_8);
            Pattern pattern = Pattern.compile("(## 目录结构\\s*\\n)```text[\\s\\S]*?```");
            Matcher matcher = pattern.matcher(content);
            String newContent = matcher.replaceFirst("$1" + treeBlock);
            Files.writeString(readmePath, newContent, StandardCharsets.UTF_8);
            System.out.println("README.md 已更新目录结构。");
        } else {
            System.out.println("未找到 README.md，请先创建。");
        }
    }

    /** 读取 .gitignore，返回需要排除的纯文件名集合（快速匹配用） */
    private static Set<String> loadGitignoreNames(Path gitignorePath) throws IOException {
        Set<String> names = new HashSet<>();
        // 如果.gitignore不存在则返回空集合
        if (!Files.exists(gitignorePath)) return names;

        // 逐行读取.gitignore
        for (String line : Files.readAllLines(gitignorePath, StandardCharsets.UTF_8)) {
            // 去除当前字符串首尾的空白字符（空格、制表符、全角空格等不可见字符）
            line = line.trim();

            // 跳过空行和注释行
            //在 .gitignore 文件里，以 # 开头的行是注释，这是 Git 官方的语法规定。
            if (line.isEmpty() || line.startsWith("#")) continue;

            // 如果以 / 结尾，则表示目录；则需要去除末尾的 /
            if (line.endsWith("/")) line = line.substring(0, line.length() - 1);
            names.add(line);
        }
        return names;
    }

    /**
     * 读取 .gitignore，返回 IgnoreRule 列表。
     * 支持 * / ** 通配符；支持尾部 / 表示仅目录。
     */
    private static List<IgnoreRule> loadGitignoreRules(Path root, Path gitignorePath) throws IOException {
        List<IgnoreRule> rules = new ArrayList<>();
        // 如果.gitignore不存在则返回空列表
        if (!Files.exists(gitignorePath)) return rules;

        // 逐行读取.gitignore
        for (String line : Files.readAllLines(gitignorePath, StandardCharsets.UTF_8)) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) continue;

            // 如果以 / 结尾，则表示目录
            boolean dirOnly = line.endsWith("/");
            if (dirOnly) line = line.substring(0, line.length() - 1);

            String glob = gitignoreToGlob(line);
            try {
                PathMatcher matcher = root.getFileSystem().getPathMatcher("glob:" + glob);
                rules.add(new IgnoreRule(matcher, dirOnly));
            } catch (Exception e) {
                // 忽略无法解析的模式
            }
        }
        return rules;
    }

    /** 将 .gitignore 通配符转换为 Java glob 语法 */
    private static String gitignoreToGlob(String pattern) {
        // 开头的 / 表示从根目录开始，去除第一个 /
        if (pattern.startsWith("/")) return pattern.substring(1);

        //如果包含 / 则表示文件
        if (pattern.contains("/")) return pattern;
        return "**/" + pattern;
    }

    private static boolean isExcluded(Path entry, Set<String> excludeNames,
                                      List<IgnoreRule> ignoreRules) {
        String name = entry.getFileName().toString();

        // 1. 精确名称匹配
        if (excludeNames.contains(name)) return true;

        // 2. 自身永远排除
        if (name.equals("GenerateReadmeTree.java") || name.endsWith(".class")) return true;

        // 3. 硬编码排除（这些不在 .gitignore 里但绝不应显示）
        if (name.equals(".git") || name.equals(".gitignore") || name.equals(".obsidian")) return true;

        // 4. glob 规则匹配
        boolean isDir = Files.isDirectory(entry);
        for (IgnoreRule rule : ignoreRules) {
            if (rule.dirOnly && !isDir) continue;
            if (rule.matcher.matches(entry) || rule.matcher.matches(entry.getFileName())) {
                return true;
            }
        }
        return false;
    }

    private static String buildTreeBlock(Path root, Set<String> excludeNames,
                                        List<IgnoreRule> ignoreRules) throws IOException {
        List<Path> entries = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(root)) {
            for (Path p : stream) {
                if (!isExcluded(p, excludeNames, ignoreRules)) {
                    System.out.println(p);
                    entries.add(p);
                }
            }
        }

        // 排序：目录在前，文件在后；同类型按名称忽略大小写排序
        entries.sort((a, b) -> {
            boolean ad = Files.isDirectory(a);
            boolean bd = Files.isDirectory(b);
            if (ad && !bd) return -1;
            if (!ad && bd) return 1;
            return a.getFileName().toString().compareToIgnoreCase(b.getFileName().toString());
        });

        StringBuilder sb = new StringBuilder();
        sb.append("```text\nMarkdown/\n");
        for (int i = 0; i < entries.size(); i++) {
            boolean isLast = (i == entries.size() - 1);
            String prefix = isLast ? "└── " : "├── ";
            String name = entries.get(i).getFileName().toString();
            if (Files.isDirectory(entries.get(i))) name += "/";
            sb.append(prefix).append(name).append("\n");
        }
        sb.append("```");
        return sb.toString();
    }

    /** 封装一条 .gitignore 规则 */
    static class IgnoreRule {
        final PathMatcher matcher;
        final boolean dirOnly;
        IgnoreRule(PathMatcher matcher, boolean dirOnly) {
            this.matcher = matcher;
            this.dirOnly = dirOnly;
        }
    }
}
