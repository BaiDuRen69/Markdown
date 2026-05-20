/*
 * ============================================================
 * 📖 使用说明
 * ============================================================
 *
 * 1. 传统方式（先编译，再运行）
 *    javac GenerateTree.java
 *    java GenerateTree [项目根目录路径]
 *
 * 2. Java 11+ 直接运行源文件（自动在内存中编译并执行）
 *    java GenerateTree.java [项目根目录路径]
 *
 * [项目根目录路径] 可选，默认为当前目录。
 * 程序会自动在指定根目录下寻找 README.md 并更新其中的目录树。
 *
 * 示例：
 *    java GenerateTree.java /home/user/myproject
 *
 * 3. 一键运行（Windows 批处理）
 *    新建一个 .bat 文件（如 run.bat），内容如下：
 *      @echo off
 *      cd /d "%~dp0"
 *      java GenerateTree.java
 *      pause
 *    双击该文件即可扫描批处理所在目录。
 *
 * 自定义配置（修改类中常量即可）：
 *   EXTRA_IGNORES  - 额外忽略规则（支持 ! 取反）
 *   COLLAPSE_DIRS  - 不展开的文件夹（相对路径）
 *   START_MARK / END_MARK - README 中目录树的占位标记
 *
 * README.md 要求：
 *   需包含以下两行标记（若不存在则自动追加到文件末尾）：
 *     <!-- TREE START -->
 *     <!-- TREE END -->
 *   生成的目录树会填充在二者之间。
 *
 * ============================================================
 */

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class GenerateTree {

    // ============================================================
    // 🔧 自定义额外忽略的文件/目录（遵循 .gitignore 语法）
    // ============================================================
    //注：取反规则会让匹配到的文件/目录“不被忽略”，从而在最终目录树中显示出来。
    private static final String[] EXTRA_IGNORES = {
            "/.git/",   // 忽略仓库根目录下的 .git 文件夹
            // "*.log",        // 示例：忽略所有 .log 文件
            // "!/keep.log",   // 示例：但不忽略 keep.log
            // "/build/",      // 示例：忽略根目录下的 build 目录
            ".gitignore",
            "/.obsidian/",
            "一键运行Generate.java.bat",
            "GenerateTree.java"
    };

    // ============================================================
    // 📁 不想展开内部内容的文件夹（名称或相对路径，用 / 分隔）
    // ============================================================
    private static final String[] COLLAPSE_DIRS = {
            "assets",
            // "build/output",   // 示例：不展开 build/output 目录
    };
    private static final String START_MARK = "<!-- TREE START -->";
    private static final String END_MARK = "<!-- TREE END -->";
    private final Path repoRoot;
    private final List<IgnoreRule> ignoreRules;

    public GenerateTree(Path repoRoot) throws IOException {
        //this.repoRoot（要扫描的根目录）
        this.repoRoot = repoRoot;

        //this.ignoreRules（解析好的忽略规则列表）
        this.ignoreRules = loadIgnoreRules();
    }

    static void main(String[] args) {
        try {
            //normalize() 是 java.nio.file.Path 接口提供的一个方法，
            // 用来清理路径中的冗余部分，返回一个更简洁、标准的路径表示。
            //toAbsolutePath() 也是 Path 的一个方法，作用是把一个相对路径转换为绝对路径。
            //args[0] 是 Java main方法接收到的第一个命令行参数。
            //先检查 args.length > 0，也就是用户有没有传参数。
            //如果有参数（args.length > 0），就用 args[0] 作为要扫描的目录路径。
            //如果没有参数，就用空字符串 ""（代表当前目录）。
            //然后把得到的路径转换成绝对路径并规范化
            //""（空字符串）能代表当前目录，是因为 Java 的 Path 在解析相对路径时，都是以JVM 进程的当前工作目录为基准的
            Path root = Paths.get(args.length > 0 ? args[0] : "").toAbsolutePath().normalize();

            new GenerateTree(root).execute();
        } catch (IOException e) {
            System.err.println("错误：生成目录树失败 - " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ---------- 忽略规则加载 ----------
    private List<IgnoreRule> loadIgnoreRules() throws IOException {
        List<IgnoreRule> rules = new ArrayList<>();
        //Path的resolve方法，它会把一个路径拼接到另一个路径后面，得到一个新的Path对象。
        //获取.gitignore文件路径
        Path gitignoreFile = repoRoot.resolve(".gitignore");
        //如果.gitignore文件存在
        if (Files.exists(gitignoreFile)) {
            //读取.gitignore文件
            List<String> lines = Files.readAllLines(gitignoreFile, StandardCharsets.UTF_8);
            for (String line : lines) {
                //解析每一行
                addRuleFromLine(rules, line);
            }
        }
        //添加自定义的额外的忽略规则
        for (String line : EXTRA_IGNORES) {
            addRuleFromLine(rules, line);
        }
        return rules;
    }

    // 从每一行中解析忽略规则，添加到规则列表中
    private void addRuleFromLine(List<IgnoreRule> rules, String line) {
        // 去掉行尾注释（# 之前如果不是反斜杠则认为注释开始）
        //trim() 是 Java 字符串的一个方法，作用是把字符串开头和结
        //尾的空白字符（空格、制表符 \t、换行等）全部去掉，中间的空格不受影响
        String trimmed = removeComment(line).trim();

        //跳过经过处理后变成空白的行，不把它们当成规则
        if (trimmed.isEmpty()) return;

        //处理取反（negation）语法——也就是以感叹号 ! 开头的规则。
        boolean negate = false;
        if (trimmed.startsWith("!")) {
            negate = true;
            trimmed = trimmed.substring(1).trim();
        }
        if (trimmed.isEmpty()) return;

        //处理目录语法——也就是以斜杠 / 结尾的规则。
        boolean dirOnly = trimmed.endsWith("/");
        if (dirOnly) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        rules.add(new IgnoreRule(trimmed, dirOnly, negate));
    }

    // 移除 # 及其之后的内容，但保留转义的 \#
    private String removeComment(String line) {
        // 遍历行中的每个字符
        int length = line.length();
        for (int i = 0; i < length; i++) {
            char c = line.charAt(i);
            if (c == '#') {
                // 检查前一个字符是否为反斜杠（转义）
                // 如果前一个字符不是反斜杠，则认为 # 是注释的开始，截断该行
                if (i == 0 || line.charAt(i - 1) != '\\') {
                    return line.substring(0, i);
                }
            }
        }
        return line;
    }

    // ---------- 忽略判断 ----------
    private boolean isIgnored(Path absolutePath) {
        //判断这个绝对路径是不是在 repoRoot 目录之内
        // （比如 repoRoot 是 /home/user/project，那 /home/user/project/src/Main.java 就以它开头）
        //这个路径不在项目根目录下, 则忽略
        if (!absolutePath.startsWith(repoRoot)) return true;

        //repoRoot.relativize(absolutePath) 是 Java Path 接口提供的一个方法，
        // 它的作用是计算一个路径相对于另一个路径的相对路径。
        // repoRoot = /home/me/project
        // absolutePath = /home/me/project/src/Main.java
        // 从 project 出发，要到 Main.java，需要经过 src 目录，所以相对路径是 src/Main.java。
        Path relative = repoRoot.relativize(absolutePath);

        //为了后续用正则表达式去匹配路径字符串，而且 gitignore 规则里的路径分隔符总是 /。
        //为了统一比较，我们必须把 Windows 下生成的带 \ 的路径，全部转成 / 风格。
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

    // ---------- 核心执行入口 ----------
    public void execute() {
        try {
            // 初始化树结构,tree 用于保存生成的树结构
            StringBuilder tree = new StringBuilder();
            tree.append(repoRoot.getFileName().toString()).append("/\n");
            List<Path> children = getSortedChildren(repoRoot);
            for (int i = 0; i < children.size(); i++) {
                boolean last = (i == children.size() - 1);
                appendTree(children.get(i), "", last, tree);
            }
            updateReadme(tree.toString());
            System.out.println("目录结构已更新到 README.md");
        } catch (IOException e) {
            System.err.println("更新 README.md 失败：" + e.getMessage());
            System.err.println("请检查文件是否被其他程序占用，或磁盘是否可写。");
        }
    }

    // ---------- 递归生成树 ----------
    private void appendTree(Path node, String prefix, boolean isLast, StringBuilder sb) throws IOException {
        if (isIgnored(node)) return;

        String nodeName = node.getFileName().toString();
        sb.append(prefix).append(isLast ? "└── " : "├── ").append(nodeName);
        if (Files.isDirectory(node)) {
            sb.append("/\n");

            // 如果是被设置为“不展开”的目录，则停止递归
            if (shouldCollapse(node)) {
                return;
            }

            // 跳过符号链接：符号链接可能指向祖先目录，递归进去会造成无限循环或重复遍历
            if (Files.isSymbolicLink(node)) {
                // 仍然显示为目录，但不进入内部
                return;
            }

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

    // ---------- 判断目录是否折叠 ----------
    private boolean shouldCollapse(Path dir) {
        Path relative = repoRoot.relativize(dir);
        for (String pattern : COLLAPSE_DIRS) {
            // 使用 Path.endsWith 逐段匹配，避免字符串包含误判
            if (relative.endsWith(Paths.get(pattern))) {
                return true;
            }
        }
        return false;
    }

    // ---------- 排序子项（目录优先，字母不区分大小写） ----------
    private List<Path> getSortedChildren(Path dir) throws IOException {
        //Files.newDirectoryStream(dir) 是 Java NIO 提供的一个方法，用来打开一个目录，
        // 并逐个读取目录里的条目（文件和子目录）,按需加载（懒惰流），遍历时才获取下一个条目，内存占用小
        //File.listFiles() 一次性返回整个数组，如果目录下有几万个文件，会消耗较大内存
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            List<Path> children = new ArrayList<>();
            for (Path p : stream) {
                // 过滤掉被忽略的文件/目录，确保符号判断基于实际会展示的条目
                if (!isIgnored(p)) {
                    children.add(p);
                }
            }
            children.sort((a, b) -> {
                boolean aDir = Files.isDirectory(a);
                boolean bDir = Files.isDirectory(b);
                if (aDir && !bDir) return -1;
                if (!aDir && bDir) return 1;
                //都是文件夹时忽略大小写按字母顺序排序
                return a.getFileName().toString().compareToIgnoreCase(b.getFileName().toString());
            });
            return children;
        }
    }

    // ---------- 更新 README.md ----------
    private void updateReadme(String tree) throws IOException {
        Path readmePath = repoRoot.resolve("README.md");
        List<String> lines;
        if (Files.exists(readmePath)) {
            lines = new ArrayList<>(Files.readAllLines(readmePath, StandardCharsets.UTF_8));
        } else {
            lines = new ArrayList<>();
        }

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

        // 强制 UTF-8 写入，杜绝乱码
        Files.write(readmePath, lines, StandardCharsets.UTF_8);
    }

    // ---------- 忽略规则内部类 ----------
    private static class IgnoreRule {
        //Pattern 是 java.util.regex.Pattern，一个编译好的正则表达式
        final Pattern regex;
        //是否只匹配目录
        final boolean dirOnly;
        //是否取反（! 规则）
        final boolean negate;

        IgnoreRule(String pattern, boolean dirOnly, boolean negate) {
            this.dirOnly = dirOnly;
            this.negate = negate;
            this.regex = buildRegex(pattern);
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
            //在 Java 字符串中，反斜杠本身又要转义，所以写成 \\*\\*，
            // 实际传给正则引擎的表达式就是 \*\*，也就是匹配两个字面星号 **。
            //split(String regex, int limit) 的第二个参数控制结果数组的长度和尾部空字符串的保留：
            //limit > 0：最多分割成 limit 个部分，超出的部分不再分割。
            //limit = 0：正常分割，但丢弃末尾所有空字符串（默认行为）。
            //limit < 0：不限制长度，且保留末尾的空字符串。
            String[] parts = pattern.split("\\*\\*", -1);
            for (int i = 0; i < parts.length; i++) {
                if (i > 0) sb.append(".*");
                //Pattern.quote()会自动给字符串里的所有正则特殊字符（如 . + * ? $ [ ( \ 等）加上 \Q...\E 转义，
                // 保证这些字符不再有正则特殊含义，变成普通字符。
                sb.append(Pattern.quote(parts[i])
                        .replace("\\*", "[^/]*")
                        .replace("\\?", "[^/]"));
            }
            sb.append("$");
            //Pattern.compile() 是 Java 把字符串形式的正则表达式编译成一个 Pattern 对象的方法
            return Pattern.compile(sb.toString());
        }

        boolean matches(String relativePath, boolean isDir) {
            if (dirOnly && !isDir) return false;
            return regex.matcher(relativePath).matches();
        }
    }
}