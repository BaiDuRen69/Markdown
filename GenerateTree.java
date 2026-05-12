import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.regex.Pattern;

public class GenerateTree {
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

    private List<IgnoreRule> loadIgnoreRules() throws IOException {
        Path gitignoreFile = repoRoot.resolve(".gitignore");
        List<IgnoreRule> rules = new ArrayList<>();
        rules.add(new IgnoreRule(".git", true, false)); // 始终忽略 .git
        if (Files.exists(gitignoreFile)) {
            for (String line : Files.readAllLines(gitignoreFile)) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) continue;
                boolean negate = false;
                if (trimmed.startsWith("!")) {
                    negate = true;
                    trimmed = trimmed.substring(1).trim();
                }
                if (trimmed.isEmpty()) continue;
                boolean dirOnly = trimmed.endsWith("/");
                if (dirOnly) trimmed = trimmed.substring(0, trimmed.length() - 1);
                rules.add(new IgnoreRule(trimmed, dirOnly, negate));
            }
        }
        return rules;
    }

    private boolean isIgnored(Path absolutePath) {
        if (!absolutePath.startsWith(repoRoot)) return true;
        Path relative = repoRoot.relativize(absolutePath);
        String pathStr = relative.toString().replace('\\', '/');
        boolean isDir = Files.isDirectory(absolutePath);

        boolean ignored = false;
        for (IgnoreRule rule : ignoreRules) {
            if (rule.matches(pathStr, isDir)) {
                ignored = !rule.negate;
            }
        }
        return ignored;
    }

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