package com.mtab.aventofcode.year2022.day7;

import com.google.common.base.Preconditions;
import com.mtab.aventofcode.Application;
import com.mtab.aventofcode.utils.CustomCollectors;
import com.mtab.aventofcode.utils.InputUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Day7 implements Function<List<Day7.File>, Long> {
    private static final Pattern COMMAND = Pattern.compile("^\\$\\s+(\\w+)\\s+(.*)$");
    private static final Pattern FILE = Pattern.compile("^(\\d+)\\s+(.*)$");

    private static final int THRESHOLD = 100000;
    private static final long MAX_SPACE = 70000000;
    private static final long REQUIRED_SPACE = 30000000;

    private static List<File> getInput() throws IOException {
        final Stack<String> tags = new Stack<>();
        final List<File> files = new ArrayList<>();

        Files.lines(
                Path.of(InputUtils.getInputPath("2022/day7/input.txt")))
                .forEach(line -> {
                    final Matcher commandMatcher = COMMAND.matcher(line);
                    final Matcher fileMatcher = FILE.matcher(line);

                    if (commandMatcher.matches()) {
                        final String command = commandMatcher.group(1);
                        final String dir = commandMatcher.group(2);

                        if (StringUtils.equals("cd", command)) {
                            // TODO: what if nested path
                            // e.g. cd a/e
                            if (StringUtils.equals(dir, "..")) {
                                tags.pop();
                            } else {
                                tags.push(dir);
                            }
                        }

                        return;
                    }

                    if (fileMatcher.matches()) {
                        final long size = Long.parseLong(fileMatcher.group(1));
                        final String name = fileMatcher.group(2);

                        files.add(File.of(name, size, List.copyOf(tags)));
                    }
                });
        return files;
    }

    public static void main(final String[] args) throws Exception {
        final var result = Application.challenge(
                "2022/day7",
                () -> new Day7().apply(Day7.getInput()));

        Preconditions.checkArgument(result == 3866390);
    }

    private long part1(final List<File> files) {
        return files.stream()
                .collect(CustomCollectors.collectFileTagGroups(File::tags))
                .values()
                .stream()
                .mapToLong(group -> group
                        .stream()
                        .mapToLong(File::size)
                        .sum())
                .filter(size -> size <= THRESHOLD)
                .sum();
    }

    private long part2(final List<File> files) {
        final long[] sizes = files.stream()
                .collect(CustomCollectors.collectFileTagGroups(File::tags))
                .values()
                .stream()
                .mapToLong((f) -> f.stream()
                        .mapToLong(File::size)
                        .sum())
                .toArray();

        final long unusedSpace = MAX_SPACE - Arrays.stream(sizes)
                .max()
                .orElseThrow(RuntimeException::new);

        return Arrays.stream(sizes)
                .sorted()
                .filter(t -> t + unusedSpace > REQUIRED_SPACE)
                .findFirst()
                .orElseThrow(RuntimeException::new);
    }

    @Override
    public Long apply(final List<File> files) {
        return this.part2(files);
    }

    record File(String name, long size, Set<String> tags) {
        public static File of(
                final String name,
                final long size,
                final List<String> tags) {

            final StringBuilder sb = new StringBuilder();
            final Set<String> fileTags = new HashSet<>();
            for (final String tag: tags) {
                sb.append(tag)
                        .append("/");

                fileTags.add(sb.toString());
            }

            return new File(name, size, Set.copyOf(fileTags));
        }
    }
}