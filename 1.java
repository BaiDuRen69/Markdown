import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.regex.Pattern;

/**
 * 自动生成仓库目录结构树并更新到 README.md 中。
 * 用法: java GenerateTree [仓库路径，默认为当前目录]
 */
public class GenerateTree {

    // ============================================================
    // 🔧 在这里添加你希望额外忽略的文件/目录（遵循 .gitignore 语法）
    // ============================================================
    private static final String[] EXTRA_IGNORES = {
            "/.git/",          // 忽略仓库根目录下的 .git 文件夹
            // "*.log",        // 示例：忽略所有 .log 文件
            // "!/keep.log",   // 示例：但不忽略 keep.log
            // "/build/",      // 示例：忽略根目录下的 build 目录
            "/.obsidian/",
    };

    private final Path repoRoot;
    private final List<IgnoreRule> ignoreRules;
    private static final String START_MARK = "<!-- TREE START -->";
    private static final String END_MARK = "<!-- TREE END -->";

    public static void main(String[] args) throws IOException {
        Path root = Paths.get(args.length > 0 ? args[0] : "").toAbsolutePath().normalize();
        new GenerateTree(root).execute();
    }

    public GenerateTree(Path repoRoot) throws IOException {
        this.repoRoot = repoRoot;
        this.ignoreRules = loadIgnoreRules();
    }

    // 加载所有忽略规则：先 .gitignore，再自定义
    private List<IgnoreRule> loadIgnoreRules() throws IOException {
        List<IgnoreRule> rules = new ArrayList<>();

        // 1. 读取仓库下的 .gitignore 文件
        Path gitignoreFile = repoRoot.resolve(".gitignore");
        if (Files.exists(gitignoreFile)) {
            for (String line : Files.readAllLines(gitignoreFile)) {
                addRuleFromLine(rules, line);
            }
        }

        // 2. 加入额外自定义的忽略规则
        for (String line : EXTRA_IGNORES) {
            addRuleFromLine(rules, line);
        }
        return rules;
    }

    // 将一行文本解析为 IgnoreRule 并加入列表
    private void addRuleFromLine(List<IgnoreRule> rules, String line) {
        String trimmed = line.trim();
        if (trimmed.isEmpty() || trimmed.startsWith("#")) return;

        boolean negate = false;
        if (trimmed.startsWith("!")) {
            negate = true;
            trimmed = trimmed.substring(1).trim();
        }
        if (trimmed.isEmpty()) return;

        boolean dirOnly = trimmed.endsWith("/");
        if (dirOnly) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        rules.add(new IgnoreRule(trimmed, dirOnly, negate));
    }

    // 判断某个绝对路径是否应该被忽略
    private boolean isIgnored(Path absolutePath) {
        if (!absolutePath.startsWith(repoRoot)) return true;
        Path relative = repoRoot.relativize(absolutePath);
        String pathStr = relative.toString().replace('\\', '/');
        boolean isDir = Files.isDirectory(absolutePath);   // 关键修复：不再给路径加 /

        boolean ignored = false;
        for (IgnoreRule rule : ignoreRules) {
            if (rule.matches(pathStr, isDir)) {
                ignored = !rule.negate;
            }
        }
        return ignored;
    }

    // 表示一条 .gitignore 风格的忽略规则
    private static class IgnoreRule {
        final Pattern regex;
        final boolean dirOnly;
        final boolean negate;

        IgnoreRule(String pattern, boolean dirOnly, boolean negate) {
            this.dirOnly = dirOnly;
            this.negate = negate;
            this.regex = buildRegex(pattern);
        }

        boolean matches(String relativePath, boolean isDir) {
            if (dirOnly && !isDir) return false;
            return regex.matcher(relativePath).matches();
        }

        // 将 .gitignore 模式转换为正则表达式（支持 **, *, ? 等）
        private static Pattern buildRegex(String pattern) {
            StringBuilder sb = new StringBuilder();
            boolean hasSlash = pattern.contains("/");
            if (pattern.startsWith("/")) {
                pattern = pattern.substring(1);
                sb.append("^");
            } else if (!hasSlash) {
                sb.append("^(.*/)?");
            } else {
                sb.append("^");
            }

            String[] parts = pattern.split("\\*\\*", -1);
            for (int i = 0; i < parts.length; i++) {
                if (i > 0) sb.append(".*");
                sb.append(Pattern.quote(parts[i]).replace("\\*", "[^/]*").replace("\\?", "[^/]"));
            }
            sb.append("$");
            return Pattern.compile(sb.toString());
        }
    }

    // 开始生成树形结构
    public void execute() throws IOException {
        StringBuilder tree = new StringBuilder();
        tree.append(repoRoot.getFileName().toString()).append("/\n");
        List<Path> children = getSortedChildren(repoRoot);
        for (int i = 0; i < children.size(); i++) {
            boolean last = (i == children.size() - 1);
            appendTree(children.get(i), "", last, tree);
        }
        updateReadme(tree.toString());
        System.out.println("目录结构已更新到 README.md");
    }

    // 递归生成树形文本
    private void appendTree(Path node, String prefix, boolean isLast, StringBuilder sb) throws IOException {
        if (isIgnored(node)) return;

        sb.append(prefix).append(isLast ? "└── " : "├── ").append(node.getFileName().toString());
        if (Files.isDirectory(node)) {
            sb.append("/\n");
            List<Path> children = getSortedChildren(node);
            for (int i = 0; i < children.size(); i++) {
                boolean last = (i == children.size() - 1);
                String childPrefix = prefix + (isLast ? "    " : "│   ");
                appendTree(children.get(i), childPrefix, last, sb);
            }
        } else {
            sb.append("\n");
        }
    }

    // 获取排序后的子项（目录优先，字母不区分大小写）
    private List<Path> getSortedChildren(Path dir) throws IOException {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            List<Path> children = new ArrayList<>();
            for (Path p : stream) children.add(p);
            children.sort((a, b) -> {
                boolean aDir = Files.isDirectory(a);
                boolean bDir = Files.isDirectory(b);
                if (aDir && !bDir) return -1;
                if (!aDir && bDir) return 1;
                return a.getFileName().toString().compareToIgnoreCase(b.getFileName().toString());
            });
            return children;
        }
    }

    // 更新 README.md，将树形结构插入标记之间
    private void updateReadme(String tree) throws IOException {
        Path readmePath = repoRoot.resolve("README.md");
        List<String> lines = Files.exists(readmePath) ?
                new ArrayList<>(Files.readAllLines(readmePath)) : new ArrayList<>();

        int startIdx = -1, endIdx = -1;
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).trim().equals(START_MARK)) startIdx = i;
            if (lines.get(i).trim().equals(END_MARK)) endIdx = i;
        }

        String codeBlock = "```\n" + tree + "```";
        if (startIdx >= 0 && endIdx > startIdx) {
            lines.subList(startIdx + 1, endIdx).clear();
            lines.add(startIdx + 1, codeBlock);
        } else {
            lines.add("");
            lines.add(START_MARK);
            lines.add(codeBlock);
            lines.add(END_MARK);
        }
        Files.write(readmePath, lines);
    }
}